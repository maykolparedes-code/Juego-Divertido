package com.maykol.juegodivertido.core

/** Rectángulo simple en coordenadas de mundo, usado para colisiones AABB. */
data class Rect(val x: Float, val y: Float, val width: Float, val height: Float) {
    fun intersects(other: Rect): Boolean {
        return x < other.x + other.width &&
            x + width > other.x &&
            y < other.y + other.height &&
            y + height > other.y
    }
}

/** y = altura sobre el suelo (0 = apoyado en el suelo). */
data class PlayerState(
    val y: Float = 0f,
    val velocityY: Float = 0f,
    val jumpsUsed: Int = 0
) {
    val isGrounded: Boolean get() = y <= 0f
}

data class Obstacle(val id: Long, val x: Float, val width: Float, val height: Float)

data class CoinPickup(val id: Long, val x: Float, val y: Float, val radius: Float = GameConfig.COIN_RADIUS)

enum class GameStatus { RUNNING, GAME_OVER }

/** Mejoras permanentes que el jugador puede comprar en la tienda con monedas. */
data class RunUpgrades(
    val doubleJump: Boolean = false,
    val coinMagnet: Boolean = false,
    val extraLives: Int = 0,
    val headStartScore: Int = 0
)

/** Snapshot inmutable expuesto a la capa de UI en cada frame. */
data class GameSnapshot(
    val status: GameStatus,
    val player: PlayerState,
    val obstacles: List<Obstacle>,
    val coins: List<CoinPickup>,
    val distanceScore: Int,
    val coinsCollected: Int,
    val lives: Int,
    val speed: Float
)
