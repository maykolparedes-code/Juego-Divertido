package com.maykol.juegodivertido.ui

import androidx.compose.runtime.Composable
import com.maykol.juegodivertido.game.AppViewModel
import com.maykol.juegodivertido.game.Screen
import com.maykol.juegodivertido.ui.screens.GameOverScreen
import com.maykol.juegodivertido.ui.screens.GameScreen
import com.maykol.juegodivertido.ui.screens.MenuScreen
import com.maykol.juegodivertido.ui.screens.ShopScreen

@Composable
fun AppRoot(viewModel: AppViewModel) {
    when (viewModel.screen) {
        Screen.MENU -> MenuScreen(viewModel)
        Screen.PLAYING -> GameScreen(viewModel)
        Screen.GAME_OVER -> GameOverScreen(viewModel)
        Screen.SHOP -> ShopScreen(viewModel)
    }
}
