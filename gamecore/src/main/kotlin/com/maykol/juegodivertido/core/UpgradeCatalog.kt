package com.maykol.juegodivertido.core

/** Mejoras permanentes que se compran con la moneda del juego (ganada jugando). */
enum class UpgradeId { DOUBLE_JUMP, COIN_MAGNET, EXTRA_LIFE, HEAD_START }

data class UpgradeDefinition(
    val id: UpgradeId,
    val cost: Int,
    val displayNameEs: String,
    val descriptionEs: String
)

object UpgradeCatalog {
    val ALL: List<UpgradeDefinition> = listOf(
        UpgradeDefinition(
            id = UpgradeId.DOUBLE_JUMP,
            cost = 300,
            displayNameEs = "Doble Salto",
            descriptionEs = "Salta dos veces en el aire para esquivar obstáculos difíciles."
        ),
        UpgradeDefinition(
            id = UpgradeId.COIN_MAGNET,
            cost = 250,
            displayNameEs = "Imán de Monedas",
            descriptionEs = "Atrae automáticamente las monedas cercanas hacia ti."
        ),
        UpgradeDefinition(
            id = UpgradeId.EXTRA_LIFE,
            cost = 400,
            displayNameEs = "Vida Extra",
            descriptionEs = "Empieza cada partida con una vida adicional."
        ),
        UpgradeDefinition(
            id = UpgradeId.HEAD_START,
            cost = 200,
            displayNameEs = "Impulso Inicial",
            descriptionEs = "Empieza cada partida con 150 puntos de ventaja."
        )
    )

    fun find(id: UpgradeId): UpgradeDefinition = ALL.first { it.id == id }
}

/** Puntos de ventaja otorgados por HEAD_START, usado tanto por el catálogo como por quien arma RunUpgrades. */
const val HEAD_START_BONUS_SCORE = 150
