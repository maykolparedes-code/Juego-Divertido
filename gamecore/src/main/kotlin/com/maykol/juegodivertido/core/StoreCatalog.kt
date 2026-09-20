package com.maykol.juegodivertido.core

/**
 * Catálogo de productos de dinero real (Google Play Billing). Los IDs deben
 * coincidir exactamente con los productos creados en Play Console.
 */
enum class ProductKind { CONSUMABLE, NON_CONSUMABLE }

data class IapProduct(
    val productId: String,
    val kind: ProductKind,
    val coinReward: Int = 0,
    val grantsRemoveAds: Boolean = false,
    val grantsSkinId: String? = null
)

object StoreCatalog {
    const val REMOVE_ADS = "remove_ads"
    const val COINS_SMALL = "coins_small_500"
    const val COINS_MEDIUM = "coins_medium_1500"
    const val COINS_LARGE = "coins_large_5000"
    const val STARTER_PACK = "starter_pack"

    val PRODUCTS: List<IapProduct> = listOf(
        IapProduct(productId = REMOVE_ADS, kind = ProductKind.NON_CONSUMABLE, grantsRemoveAds = true),
        IapProduct(productId = COINS_SMALL, kind = ProductKind.CONSUMABLE, coinReward = 500),
        IapProduct(productId = COINS_MEDIUM, kind = ProductKind.CONSUMABLE, coinReward = 1500),
        IapProduct(productId = COINS_LARGE, kind = ProductKind.CONSUMABLE, coinReward = 5000),
        IapProduct(
            productId = STARTER_PACK,
            kind = ProductKind.NON_CONSUMABLE,
            coinReward = 1000,
            grantsRemoveAds = true,
            grantsSkinId = "founder"
        )
    )

    fun find(productId: String): IapProduct = PRODUCTS.first { it.productId == productId }

    val consumableIds: Set<String>
        get() = PRODUCTS.filter { it.kind == ProductKind.CONSUMABLE }.map { it.productId }.toSet()

    val nonConsumableIds: Set<String>
        get() = PRODUCTS.filter { it.kind == ProductKind.NON_CONSUMABLE }.map { it.productId }.toSet()

    val allProductIds: List<String>
        get() = PRODUCTS.map { it.productId }
}
