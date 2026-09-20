package com.maykol.juegodivertido.core

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEngineTest {

    private val frameDt = 0.016f

    @Test
    fun `jump lifts the player off the ground`() {
        val engine = GameEngine(random = Random(1))
        engine.jump()
        engine.update(0.05f)
        assertTrue(engine.snapshot().player.y > 0f)
    }

    @Test
    fun `without double jump upgrade a second jump is ignored`() {
        val engine = GameEngine(random = Random(5))
        engine.jump()
        engine.update(frameDt)
        engine.jump()
        assertEquals(1, engine.snapshot().player.jumpsUsed)
    }

    @Test
    fun `with double jump upgrade a second jump is allowed mid-air`() {
        val engine = GameEngine(upgrades = RunUpgrades(doubleJump = true), random = Random(5))
        engine.jump()
        engine.update(frameDt)
        engine.jump()
        assertEquals(2, engine.snapshot().player.jumpsUsed)
    }

    @Test
    fun `score increases over time`() {
        val engine = GameEngine(random = Random(3))
        engine.update(1f)
        assertTrue(engine.snapshot().distanceScore > 0)
    }

    @Test
    fun `head start upgrade grants a starting score bonus`() {
        val engine = GameEngine(upgrades = RunUpgrades(headStartScore = 150), random = Random(3))
        assertEquals(150, engine.snapshot().distanceScore)
    }

    @Test
    fun `player eventually collides with an obstacle if it never jumps`() {
        val engine = GameEngine(random = Random(42))
        var t = 0f
        while (t < 10f && engine.snapshot().status == GameStatus.RUNNING) {
            engine.update(frameDt)
            t += frameDt
        }
        assertEquals(GameStatus.GAME_OVER, engine.snapshot().status)
    }

    @Test
    fun `extra life upgrade lets the player survive one hit`() {
        val engine = GameEngine(upgrades = RunUpgrades(extraLives = 1), random = Random(7))
        var t = 0f
        var wasDownToOneLife = false
        while (t < 10f) {
            engine.update(frameDt)
            val snapshot = engine.snapshot()
            if (snapshot.lives == 1) wasDownToOneLife = true
            if (snapshot.status == GameStatus.GAME_OVER) break
            t += frameDt
        }
        assertTrue(wasDownToOneLife)
    }

    @Test
    fun `revive resets status to running after game over`() {
        val engine = GameEngine(random = Random(2))
        var t = 0f
        while (t < 10f && engine.snapshot().status == GameStatus.RUNNING) {
            engine.update(frameDt)
            t += frameDt
        }
        assertEquals(GameStatus.GAME_OVER, engine.snapshot().status)

        engine.revive()

        assertEquals(GameStatus.RUNNING, engine.snapshot().status)
        assertEquals(0f, engine.snapshot().player.y)
    }

    @Test
    fun `jump and update calls after game over are no-ops`() {
        val engine = GameEngine(random = Random(9))
        var t = 0f
        while (t < 10f && engine.snapshot().status == GameStatus.RUNNING) {
            engine.update(frameDt)
            t += frameDt
        }
        val scoreAtGameOver = engine.snapshot().distanceScore

        engine.update(1f)
        engine.jump()

        assertEquals(scoreAtGameOver, engine.snapshot().distanceScore)
    }
}
