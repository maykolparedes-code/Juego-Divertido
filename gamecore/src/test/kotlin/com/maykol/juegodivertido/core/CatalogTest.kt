package com.maykol.juegodivertido.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogTest {

    @Test
    fun `all upgrade costs are positive`() {
        assertTrue(UpgradeCatalog.ALL.all { it.cost > 0 })
    }

    @Test
    fun `every upgrade id has exactly one definition`() {
        val ids = UpgradeCatalog.ALL.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(UpgradeId.entries.toSet(), ids.toSet())
    }

    @Test
    fun `all store product ids are unique`() {
        val ids = StoreCatalog.PRODUCTS.map { it.productId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `remove ads product grants the remove ads flag`() {
        assertTrue(StoreCatalog.find(StoreCatalog.REMOVE_ADS).grantsRemoveAds)
    }

    @Test
    fun `consumable coin products all reward coins`() {
        StoreCatalog.consumableIds.forEach { id ->
            assertTrue(StoreCatalog.find(id).coinReward > 0)
        }
    }

    @Test
    fun `starter pack bundles ads removal, coins and a skin`() {
        val pack = StoreCatalog.find(StoreCatalog.STARTER_PACK)
        assertTrue(pack.grantsRemoveAds)
        assertTrue(pack.coinReward > 0)
        assertTrue(pack.grantsSkinId != null)
    }
}
