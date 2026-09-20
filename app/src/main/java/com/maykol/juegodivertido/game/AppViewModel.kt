package com.maykol.juegodivertido.game

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maykol.juegodivertido.ads.AdManager
import com.maykol.juegodivertido.billing.BillingManager
import com.maykol.juegodivertido.core.GameEngine
import com.maykol.juegodivertido.core.GameSnapshot
import com.maykol.juegodivertido.core.GameStatus
import com.maykol.juegodivertido.core.HEAD_START_BONUS_SCORE
import com.maykol.juegodivertido.core.RunUpgrades
import com.maykol.juegodivertido.core.StoreCatalog
import com.maykol.juegodivertido.core.UpgradeCatalog
import com.maykol.juegodivertido.core.UpgradeId
import com.maykol.juegodivertido.data.GameProgress
import com.maykol.juegodivertido.data.GameProgressRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class Screen { MENU, PLAYING, GAME_OVER, SHOP }

/**
 * Orquesta toda la app: qué pantalla se muestra, el progreso persistido, la
 * partida en curso y los managers de anuncios/compras. Vive tan solo en
 * memoria del proceso; el estado que debe sobrevivir se delega en
 * [GameProgressRepository] (DataStore).
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameProgressRepository(application)

    val adManager = AdManager(application)
    val billingManager = BillingManager(
        context = application,
        scope = viewModelScope,
        onPurchaseGranted = ::grantPurchase
    )

    var screen by mutableStateOf(Screen.MENU)
        private set

    var progress by mutableStateOf(GameProgress())
        private set

    var engine: GameEngine? by mutableStateOf(null)
        private set

    var snapshot: GameSnapshot? by mutableStateOf(null)
        private set

    var lastRunCoins by mutableStateOf(0)
        private set

    var lastRunWasNewRecord by mutableStateOf(false)
        private set

    var doubleCoinsUsedThisRun by mutableStateOf(false)
        private set

    init {
        repository.progressFlow.onEach { newProgress ->
            progress = newProgress
            adManager.removeAdsOwned = newProgress.removeAdsOwned
        }.launchIn(viewModelScope)

        adManager.initialize()
        billingManager.start()
    }

    private suspend fun grantPurchase(productId: String) {
        val product = StoreCatalog.find(productId)
        if (product.coinReward > 0) repository.addCoins(product.coinReward)
        if (product.grantsRemoveAds) repository.setRemoveAdsOwned()
        product.grantsSkinId?.let { repository.unlockSkin(it) }
    }

    fun startGame() {
        val upgrades = RunUpgrades(
            doubleJump = UpgradeId.DOUBLE_JUMP in progress.ownedUpgrades,
            coinMagnet = UpgradeId.COIN_MAGNET in progress.ownedUpgrades,
            extraLives = if (UpgradeId.EXTRA_LIFE in progress.ownedUpgrades) 1 else 0,
            headStartScore = if (UpgradeId.HEAD_START in progress.ownedUpgrades) HEAD_START_BONUS_SCORE else 0
        )
        val newEngine = GameEngine(upgrades = upgrades)
        engine = newEngine
        snapshot = newEngine.snapshot()
        doubleCoinsUsedThisRun = false
        screen = Screen.PLAYING
    }

    /** Avanza la simulación un frame; llamado desde el loop de juego en GameScreen. */
    fun tick(dt: Float) {
        val currentEngine = engine ?: return
        currentEngine.update(dt)
        val newSnapshot = currentEngine.snapshot()
        snapshot = newSnapshot
        if (newSnapshot.status == GameStatus.GAME_OVER && screen == Screen.PLAYING) {
            onGameOver(newSnapshot)
        }
    }

    fun jump() {
        engine?.jump()
    }

    private fun onGameOver(finalSnapshot: GameSnapshot) {
        lastRunCoins = finalSnapshot.coinsCollected
        lastRunWasNewRecord = finalSnapshot.distanceScore > progress.highScore
        viewModelScope.launch {
            repository.reportRunScore(finalSnapshot.distanceScore)
            repository.addCoins(finalSnapshot.coinsCollected)
        }
        screen = Screen.GAME_OVER
    }

    fun showInterstitialIfDue(activity: Activity) {
        adManager.maybeShowInterstitialOnGameOver(activity)
    }

    fun reviveWithAd(activity: Activity) {
        adManager.showRewarded(
            activity = activity,
            onReward = {
                engine?.revive()
                snapshot = engine?.snapshot()
                screen = Screen.PLAYING
            },
            onUnavailable = {}
        )
    }

    fun doubleCoinsWithAd(activity: Activity) {
        if (doubleCoinsUsedThisRun || lastRunCoins <= 0) return
        adManager.showRewarded(
            activity = activity,
            onReward = {
                doubleCoinsUsedThisRun = true
                viewModelScope.launch { repository.addCoins(lastRunCoins) }
            },
            onUnavailable = {}
        )
    }

    fun goToMenu() {
        screen = Screen.MENU
    }

    fun goToShop() {
        screen = Screen.SHOP
    }

    fun buyUpgrade(id: UpgradeId) {
        if (id in progress.ownedUpgrades) return
        val definition = UpgradeCatalog.find(id)
        viewModelScope.launch {
            val success = repository.spendCoins(definition.cost)
            if (success) repository.unlockUpgrade(id)
        }
    }

    fun buyRealMoneyProduct(activity: Activity, productId: String) {
        billingManager.launchPurchase(activity, productId)
    }

    override fun onCleared() {
        billingManager.endConnection()
        super.onCleared()
    }
}
