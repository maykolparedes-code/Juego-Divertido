package com.maykol.juegodivertido.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.maykol.juegodivertido.core.UpgradeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "game_progress")

/** Snapshot inmutable del progreso persistido del jugador. */
data class GameProgress(
    val coins: Int = 0,
    val highScore: Int = 0,
    val removeAdsOwned: Boolean = false,
    val ownedUpgradeIds: Set<String> = emptySet(),
    val ownedSkinIds: Set<String> = setOf("default"),
    val selectedSkinId: String = "default"
) {
    val ownedUpgrades: Set<UpgradeId>
        get() = ownedUpgradeIds.mapNotNull { id ->
            runCatching { UpgradeId.valueOf(id) }.getOrNull()
        }.toSet()
}

/**
 * Fuente única de verdad para el progreso del jugador: monedas, mejor
 * puntaje, mejoras compradas con monedas, compras reales (remove_ads,
 * skins) y skin seleccionada. Todo se guarda localmente con DataStore.
 *
 * Nota de seguridad: esto es almacenamiento local de conveniencia, no un
 * mecanismo anti-fraude. Para producción, la fuente de verdad de las
 * compras con dinero real debería validarse contra el recibo de Google
 * Play (ver README, sección "Verificación de compras").
 */
class GameProgressRepository(context: Context) {
    private val dataStore = context.applicationContext.dataStore

    private object Keys {
        val COINS = intPreferencesKey("coins")
        val HIGH_SCORE = intPreferencesKey("high_score")
        val REMOVE_ADS = booleanPreferencesKey("remove_ads_owned")
        val OWNED_UPGRADES = stringSetPreferencesKey("owned_upgrades")
        val OWNED_SKINS = stringSetPreferencesKey("owned_skins")
        val SELECTED_SKIN = stringPreferencesKey("selected_skin")
    }

    val progressFlow: Flow<GameProgress> = dataStore.data.map { prefs ->
        GameProgress(
            coins = prefs[Keys.COINS] ?: 0,
            highScore = prefs[Keys.HIGH_SCORE] ?: 0,
            removeAdsOwned = prefs[Keys.REMOVE_ADS] ?: false,
            ownedUpgradeIds = prefs[Keys.OWNED_UPGRADES] ?: emptySet(),
            ownedSkinIds = prefs[Keys.OWNED_SKINS] ?: setOf("default"),
            selectedSkinId = prefs[Keys.SELECTED_SKIN] ?: "default"
        )
    }

    suspend fun addCoins(amount: Int) {
        if (amount <= 0) return
        dataStore.edit { prefs ->
            val current = prefs[Keys.COINS] ?: 0
            prefs[Keys.COINS] = current + amount
        }
    }

    /** Devuelve true si había monedas suficientes y se descontaron. */
    suspend fun spendCoins(amount: Int): Boolean {
        var success = false
        dataStore.edit { prefs ->
            val current = prefs[Keys.COINS] ?: 0
            if (current >= amount) {
                prefs[Keys.COINS] = current - amount
                success = true
            }
        }
        return success
    }

    suspend fun reportRunScore(score: Int) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.HIGH_SCORE] ?: 0
            if (score > current) prefs[Keys.HIGH_SCORE] = score
        }
    }

    suspend fun unlockUpgrade(id: UpgradeId) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.OWNED_UPGRADES] ?: emptySet()
            prefs[Keys.OWNED_UPGRADES] = current + id.name
        }
    }

    suspend fun setRemoveAdsOwned() {
        dataStore.edit { prefs -> prefs[Keys.REMOVE_ADS] = true }
    }

    suspend fun unlockSkin(skinId: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.OWNED_SKINS] ?: setOf("default")
            prefs[Keys.OWNED_SKINS] = current + skinId
        }
    }

    suspend fun selectSkin(skinId: String) {
        dataStore.edit { prefs -> prefs[Keys.SELECTED_SKIN] = skinId }
    }
}
