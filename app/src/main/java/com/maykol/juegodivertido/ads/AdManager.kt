package com.maykol.juegodivertido.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.maykol.juegodivertido.AdUnitIds
import com.maykol.juegodivertido.INTERSTITIAL_FREQUENCY

private const val TAG = "AdManager"

/**
 * Punto único de integración con AdMob: banner, intersticial (entre
 * partidas, con límite de frecuencia) y recompensado (revivir / duplicar
 * monedas, siempre opcional).
 *
 * Los intersticiales y el banner respetan [removeAdsOwned]; el anuncio
 * recompensado sigue disponible aunque se haya comprado "Quitar anuncios",
 * porque es una elección explícita del jugador a cambio de una recompensa,
 * no publicidad forzada.
 */
class AdManager(context: Context) {

    private val appContext = context.applicationContext

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
        set(value) {
            field = value
            isRewardedAdReady = value != null
        }
    private var gameOverCount = 0

    /** Observable por Compose: controla si el botón de anuncio recompensado se muestra habilitado. */
    var isRewardedAdReady by mutableStateOf(false)
        private set

    var removeAdsOwned: Boolean = false
        set(value) {
            field = value
            if (value) interstitialAd = null
        }

    fun initialize() {
        MobileAds.initialize(appContext) {
            Log.d(TAG, "AdMob inicializado")
        }
        loadInterstitial()
        loadRewarded()
    }

    private fun loadInterstitial() {
        if (removeAdsOwned) return
        InterstitialAd.load(
            appContext,
            AdUnitIds.INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.d(TAG, "Intersticial no disponible: ${error.message}")
                    interstitialAd = null
                }
            }
        )
    }

    private fun loadRewarded() {
        RewardedAd.load(
            appContext,
            AdUnitIds.REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.d(TAG, "Recompensado no disponible: ${error.message}")
                    rewardedAd = null
                }
            }
        )
    }

    /**
     * Llamar cada vez que termina una partida. Muestra un intersticial como
     * máximo cada [INTERSTITIAL_FREQUENCY] partidas, para no interrumpir en
     * cada intento: mejor retención y cumple las políticas de anuncios de
     * Google Play sobre frecuencia razonable.
     */
    fun maybeShowInterstitialOnGameOver(activity: Activity) {
        if (removeAdsOwned) return
        gameOverCount += 1
        if (gameOverCount % INTERSTITIAL_FREQUENCY != 0) return

        val ad = interstitialAd ?: run {
            loadInterstitial()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadInterstitial()
            }
        }
        ad.show(activity)
    }

    /** Muestra un anuncio recompensado; [onReward] solo se llama si el usuario lo completa. */
    fun showRewarded(activity: Activity, onReward: () -> Unit, onUnavailable: () -> Unit) {
        val ad = rewardedAd ?: run {
            loadRewarded()
            onUnavailable()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadRewarded()
                onUnavailable()
            }
        }
        ad.show(activity) { onReward() }
    }
}

/** Banner de AdMob embebido en la UI de Compose (menú y tienda). */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdUnitIds.BANNER
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
