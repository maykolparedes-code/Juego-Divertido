package com.maykol.juegodivertido.core

/**
 * Todas las constantes de balance del juego viven aquí para poder ajustarlas
 * sin tocar la lógica del motor, y para que los tests puedan razonar sobre
 * valores conocidos.
 */
object GameConfig {
    const val GRAVITY = 28f
    const val JUMP_VELOCITY = 11f

    const val PLAYER_WIDTH = 1f
    const val PLAYER_HEIGHT = 1f

    const val BASE_SPEED = 6f
    const val MAX_SPEED = 16f
    const val SPEED_RAMP_PER_SECOND = 0.05f

    const val MIN_OBSTACLE_GAP_SECONDS = 0.9f
    const val MAX_OBSTACLE_GAP_SECONDS = 1.8f

    const val OBSTACLE_WIDTH = 0.6f
    const val OBSTACLE_MIN_HEIGHT = 0.8f
    const val OBSTACLE_MAX_HEIGHT = 1.6f

    const val COIN_SPAWN_CHANCE = 0.5f
    const val COIN_RADIUS = 0.25f
    const val COIN_MAGNET_RANGE = 4f
    const val COIN_MAGNET_PULL = 0.15f

    const val SPAWN_X = 20f
    const val DESPAWN_X = -3f

    const val HIT_INVULNERABILITY_SECONDS = 1.2f
    const val REVIVE_INVULNERABILITY_SECONDS = 1.5f
}
