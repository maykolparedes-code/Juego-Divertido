package com.maykol.juegodivertido.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.maykol.juegodivertido.R
import com.maykol.juegodivertido.ads.BannerAdView
import com.maykol.juegodivertido.game.AppViewModel

@Composable
fun MenuScreen(viewModel: AppViewModel) {
    val progress = viewModel.progress

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.menu_title), style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.menu_high_score, progress.highScore))
            Text(text = stringResource(R.string.menu_coins, progress.coins))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { viewModel.startGame() }, modifier = Modifier.fillMaxWidth(0.7f)) {
                Text(stringResource(R.string.menu_play))
            }
            OutlinedButton(onClick = { viewModel.goToShop() }, modifier = Modifier.fillMaxWidth(0.7f)) {
                Text(stringResource(R.string.menu_shop))
            }
        }

        if (!progress.removeAdsOwned) {
            BannerAdView()
        } else {
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
