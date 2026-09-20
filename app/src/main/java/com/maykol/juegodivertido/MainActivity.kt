package com.maykol.juegodivertido

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.maykol.juegodivertido.game.AppViewModel
import com.maykol.juegodivertido.ui.AppRoot
import com.maykol.juegodivertido.ui.theme.JuegoDivertidoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JuegoDivertidoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(viewModel = viewModel)
                }
            }
        }
    }
}
