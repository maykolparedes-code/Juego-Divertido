package com.maykol.juegodivertido.core

import kotlin.random.Random

/**
 * Motor determinista del endless runner. No depende de Android para poder
 * probarlo con JUnit puro y para poder reutilizarlo (por ejemplo en un
 * preview de escritorio) sin arrastrar el framework de UI.
 *
 * Convención de coordenadas: el eje X avanza hacia la izquierda a medida que
 * los obstáculos "se acercan" al jugador (el jugador está fijo en x=0..PLAYER_WIDTH).
 * El eje Y es la altura sobre el suelo (0 = apoyado en el suelo).
 */
class GameEngine(
    private val upgrades: RunUpgrades = RunUpgrades(),
    private val random: Random = Random.Default
) {
    var status: GameStatus = GameStatus.RUNNING
        private set

    private var player = PlayerState()
    private val obstacles = mutableListOf<Obstacle>()
    private val coins = mutableListOf<CoinPickup>()

    private var elapsed = 0f
    private var speed = GameConfig.BASE_SPEED
    private var distance = 0f
    private var coinsCollected = 0
    private var lives = 1 + upgrades.extraLives
    private var nextSpawnIn = randomGap()
    private var nextId = 0L
    private var invulnerableFor = 0f

    init {
        distance = upgrades.headStartScore.toFloat()
    }

    private val maxJumps: Int get() = if (upgrades.doubleJump) 2 else 1

    fun jump() {
        if (status != GameStatus.RUNNING) return
        if (player.jumpsUsed < maxJumps) {
            player = player.copy(velocityY = GameConfig.JUMP_VELOCITY, jumpsUsed = player.jumpsUsed + 1)
        }
    }

    fun update(dt: Float) {
        if (status != GameStatus.RUNNING) return

        elapsed += dt
        speed = (GameConfig.BASE_SPEED + elapsed * GameConfig.SPEED_RAMP_PER_SECOND)
            .coerceAtMost(GameConfig.MAX_SPEED)
        distance += speed * dt

        if (invulnerableFor > 0f) invulnerableFor = (invulnerableFor - dt).coerceAtLeast(0f)

        applyPlayerPhysics(dt)
        advanceObstacles(dt)
        advanceCoins(dt)
        handleSpawning(dt)

        if (upgrades.coinMagnet) applyCoinMagnet()

        checkCoinCollisions()
        checkObstacleCollisions()
    }

    fun revive() {
        if (status != GameStatus.GAME_OVER) return
        lives = 1
        obstacles.clear()
        coins.clear()
        player = PlayerState()
        invulnerableFor = GameConfig.REVIVE_INVULNERABILITY_SECONDS
        status = GameStatus.RUNNING
    }

    fun snapshot(): GameSnapshot = GameSnapshot(
        status = status,
        player = player,
        obstacles = obstacles.toList(),
        coins = coins.toList(),
        distanceScore = distance.toInt(),
        coinsCollected = coinsCollected,
        lives = lives,
        speed = speed
    )

    private fun applyPlayerPhysics(dt: Float) {
        val newVelocity = player.velocityY - GameConfig.GRAVITY * dt
        val newY = (player.y + player.velocityY * dt).coerceAtLeast(0f)
        val landed = newY <= 0f && player.velocityY <= 0f
        player = player.copy(
            y = newY,
            velocityY = if (landed) 0f else newVelocity,
            jumpsUsed = if (landed) 0 else player.jumpsUsed
        )
    }

    private fun advanceObstacles(dt: Float) {
        for (i in obstacles.indices) {
            obstacles[i] = obstacles[i].copy(x = obstacles[i].x - speed * dt)
        }
        obstacles.removeAll { it.x + it.width < GameConfig.DESPAWN_X }
    }

    private fun advanceCoins(dt: Float) {
        for (i in coins.indices) {
            coins[i] = coins[i].copy(x = coins[i].x - speed * dt)
        }
        coins.removeAll { it.x < GameConfig.DESPAWN_X }
    }

    private fun handleSpawning(dt: Float) {
        nextSpawnIn -= dt
        if (nextSpawnIn <= 0f) {
            spawnObstacle()
            nextSpawnIn = randomGap()
        }
    }

    private fun applyCoinMagnet() {
        for (i in coins.indices) {
            val coin = coins[i]
            if (coin.x in 0f..GameConfig.COIN_MAGNET_RANGE) {
                val targetY = player.y + GameConfig.PLAYER_HEIGHT / 2f
                coins[i] = coin.copy(y = lerp(coin.y, targetY, GameConfig.COIN_MAGNET_PULL))
            }
        }
    }

    private fun checkObstacleCollisions() {
        if (invulnerableFor > 0f) return
        val pr = playerRect()
        val hit = obstacles.any { pr.intersects(it.toRect()) }
        if (!hit) return

        lives -= 1
        if (lives <= 0) {
            status = GameStatus.GAME_OVER
        } else {
            invulnerableFor = GameConfig.HIT_INVULNERABILITY_SECONDS
            obstacles.removeAll { pr.intersects(it.toRect()) }
        }
    }

    private fun checkCoinCollisions() {
        val pr = playerRect()
        val iterator = coins.iterator()
        while (iterator.hasNext()) {
            val coin = iterator.next()
            val coinRect = Rect(coin.x - coin.radius, coin.y - coin.radius, coin.radius * 2, coin.radius * 2)
            if (pr.intersects(coinRect)) {
                coinsCollected += 1
                iterator.remove()
            }
        }
    }

    private fun spawnObstacle() {
        val height = GameConfig.OBSTACLE_MIN_HEIGHT +
            random.nextFloat() * (GameConfig.OBSTACLE_MAX_HEIGHT - GameConfig.OBSTACLE_MIN_HEIGHT)
        obstacles.add(Obstacle(nextId++, GameConfig.SPAWN_X, GameConfig.OBSTACLE_WIDTH, height))

        if (random.nextFloat() < GameConfig.COIN_SPAWN_CHANCE) {
            val coinY = height + 0.6f + random.nextFloat() * 0.8f
            val coinX = GameConfig.SPAWN_X + GameConfig.OBSTACLE_WIDTH / 2f
            coins.add(CoinPickup(nextId++, coinX, coinY))
        }
    }

    private fun randomGap(): Float {
        return GameConfig.MIN_OBSTACLE_GAP_SECONDS +
            random.nextFloat() * (GameConfig.MAX_OBSTACLE_GAP_SECONDS - GameConfig.MIN_OBSTACLE_GAP_SECONDS)
    }

    private fun playerRect() = Rect(0f, player.y, GameConfig.PLAYER_WIDTH, GameConfig.PLAYER_HEIGHT)

    private fun Obstacle.toRect() = Rect(x, 0f, width, height)

    private fun lerp(start: Float, end: Float, t: Float) = start + (end - start) * t
}
