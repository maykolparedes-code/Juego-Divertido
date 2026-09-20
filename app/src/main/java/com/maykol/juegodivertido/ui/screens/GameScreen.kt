package com.maykol.juegodivertido.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.maykol.juegodivertido.R
import com.maykol.juegodivertido.core.CoinPickup
import com.maykol.juegodivertido.core.GameConfig
import com.maykol.juegodivertido.core.Obstacle
import com.maykol.juegodivertido.game.AppViewModel
import com.maykol.juegodivertido.ui.theme.CoinColor
import com.maykol.juegodivertido.ui.theme.GroundColor
import com.maykol.juegodivertido.ui.theme.ObstacleColor
import com.maykol.juegodivertido.ui.theme.PlayerColor
import com.maykol.juegodivertido.ui.theme.SkyBottom
import com.maykol.juegodivertido.ui.theme.SkyTop
import kotlinx.coroutines.isActive

/** Cuántos píxeles de pantalla representa 1 unidad de mundo del gamecore. */
private const val WORLD_UNIT_PX = 90f

/** Duración máxima de un frame simulado, para evitar saltos grandes tras una pausa (p. ej. cambio de app). */
private const val MAX_FRAME_SECONDS = 0.05f

@Composable
fun GameScreen(viewModel: AppViewModel) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(viewModel.engine) {
        var lastFrameNanos = 0L
        while (isActive) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos != 0L) {
                    val dtSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f)
                        .coerceAtMost(MAX_FRAME_SECONDS)
                    viewModel.tick(dtSeconds)
                }
                lastFrameNanos = frameNanos
            }
        }
    }

    val snapshot = viewModel.snapshot ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyTop, SkyBottom)))
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.jump()
                })
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val groundY = size.height * 0.8f

            drawRect(
                color = GroundColor,
                topLeft = Offset(0f, groundY),
                size = Size(size.width, size.height - groundY)
            )

            fun worldXToPx(worldX: Float) = size.width * 0.18f + worldX * WORLD_UNIT_PX
            fun worldYToPx(worldY: Float) = groundY - worldY * WORLD_UNIT_PX

            val playerLeft = worldXToPx(0f)
            val playerBottom = worldYToPx(snapshot.player.y)
            val playerSizePx = GameConfig.PLAYER_WIDTH * WORLD_UNIT_PX
            drawRoundRect(
                color = PlayerColor,
                topLeft = Offset(playerLeft, playerBottom - playerSizePx),
                size = Size(playerSizePx, playerSizePx),
                cornerRadius = CornerRadius(12f, 12f)
            )

            snapshot.obstacles.forEach { obstacle: Obstacle ->
                val left = worldXToPx(obstacle.x)
                val widthPx = obstacle.width * WORLD_UNIT_PX
                val heightPx = obstacle.height * WORLD_UNIT_PX
                drawRoundRect(
                    color = ObstacleColor,
                    topLeft = Offset(left, groundY - heightPx),
                    size = Size(widthPx, heightPx),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }

            snapshot.coins.forEach { coin: CoinPickup ->
                drawCircle(
                    color = CoinColor,
                    radius = coin.radius * WORLD_UNIT_PX,
                    center = Offset(worldXToPx(coin.x), worldYToPx(coin.y))
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.game_score, snapshot.distanceScore),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(text = stringResource(R.string.game_coins, snapshot.coinsCollected), color = Color.White)
            Text(text = stringResource(R.string.game_lives, snapshot.lives), color = Color.White)
        }

        Text(
            text = stringResource(R.string.game_tap_hint),
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
        )
    }
}
