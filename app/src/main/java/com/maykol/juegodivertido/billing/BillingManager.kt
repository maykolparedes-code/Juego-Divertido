package com.maykol.juegodivertido.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.maykol.juegodivertido.core.StoreCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "BillingManager"

/**
 * Envoltorio sobre Google Play Billing Library. Consulta el catálogo de
 * [StoreCatalog], lanza el flujo de compra nativo de Play y, cuando Play
 * confirma una compra, reconoce (no consumibles) o consume (consumibles) y
 * avisa a [onPurchaseGranted] para que la capa de datos otorgue la
 * recompensa correspondiente.
 *
 * Nota de seguridad importante: Play Billing valida la compra contra los
 * servidores de Google, pero esta clase por sí sola no protege contra un
 * dispositivo con root que falsifique la respuesta local. Para una tienda
 * con productos de alto valor, añade verificación server-side del recibo
 * (Google Play Developer API) antes de otorgar la recompensa; ver README.
 */
class BillingManager(
    context: Context,
    private val scope: CoroutineScope,
    private val onPurchaseGranted: suspend (productId: String) -> Unit
) : PurchasesUpdatedListener {

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    /** Observable por Compose: la tienda se recompone sola cuando llegan los precios de Play. */
    var productDetails: Map<String, ProductDetails> by mutableStateOf(emptyMap())
        private set

    fun start() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    restorePendingPurchases()
                } else {
                    Log.w(TAG, "Fallo al conectar con Billing: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing desconectado; se reintentará en la próxima acción del usuario")
            }
        })
    }

    private fun queryProducts() {
        val products = StoreCatalog.allProductIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()

        scope.launch {
            val result = billingClient.queryProductDetails(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = result.productDetailsList.orEmpty().associateBy { it.productId }
            } else {
                Log.w(TAG, "No se pudieron cargar los productos: ${result.billingResult.debugMessage}")
            }
        }
    }

    fun launchPurchase(activity: Activity, productId: String) {
        val details = productDetails[productId] ?: run {
            Log.w(TAG, "Producto no disponible todavía: $productId")
            return
        }
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode != BillingClient.BillingResponseCode.OK || purchases == null) {
            if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                Log.w(TAG, "Compra no completada: ${result.debugMessage}")
            }
            return
        }
        purchases.forEach { handlePurchase(it) }
    }

    /** Reintenta compras que quedaron pendientes de reconocer/consumir (p. ej. tras un cierre inesperado). */
    private fun restorePendingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { handlePurchase(it) }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        scope.launch {
            purchase.products.forEach { productId ->
                if (runCatching { StoreCatalog.find(productId) }.isSuccess) {
                    onPurchaseGranted(productId)
                }
            }
            finishPurchase(purchase)
        }
    }

    private fun finishPurchase(purchase: Purchase) {
        val isConsumable = purchase.products.any { it in StoreCatalog.consumableIds }
        if (isConsumable) {
            val params = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.consumeAsync(params) { result, _ ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "No se pudo consumir la compra: ${result.debugMessage}")
                }
            }
        } else if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { result ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "No se pudo reconocer la compra: ${result.debugMessage}")
                }
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
