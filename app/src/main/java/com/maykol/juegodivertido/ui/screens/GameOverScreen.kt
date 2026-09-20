package com.maykol.juegodivertido.ui.screens

import android.app.Activity
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.maykol.juegodivertido.R
import com.maykol.juegodivertido.ads.BannerAdView
import com.maykol.juegodivertido.game.AppViewModel

@Composable
fun GameOverScreen(viewModel: AppViewModel) {
    val activity = LocalContext.current as Activity
    val snapshot = viewModel.snapshot

    LaunchedEffect(Unit) {
        viewModel.showInterstitialIfDue(activity)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.gameover_title), style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.gameover_score, snapshot?.distanceScore ?: 0))
            if (viewModel.lastRunWasNewRecord) {
                Text(
                    text = stringResource(R.string.gameover_new_record),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(text = stringResource(R.string.gameover_coins_earned, viewModel.lastRunCoins))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (viewModel.adManager.isRewardedAdReady) {
                Button(
                    onClick = { viewModel.reviveWithAd(activity) },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(stringResource(R.string.gameover_revive))
                }

                if (!viewModel.doubleCoinsUsedThisRun) {
                    OutlinedButton(
                        onClick = { viewModel.doubleCoinsWithAd(activity) },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(stringResource(R.string.gameover_double_coins))
                    }
                }
            }

            if (viewModel.doubleCoinsUsedThisRun) {
                Text(stringResource(R.string.gameover_double_coins_done))
            }

            Button(onClick = { viewModel.startGame() }, modifier = Modifier.fillMaxWidth(0.8f)) {
                Text(stringResource(R.string.gameover_retry))
            }
            OutlinedButton(onClick = { viewModel.goToMenu() }, modifier = Modifier.fillMaxWidth(0.8f)) {
                Text(stringResource(R.string.gameover_menu))
            }
        }

        if (!viewModel.progress.removeAdsOwned) {
            BannerAdView()
        } else {
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
