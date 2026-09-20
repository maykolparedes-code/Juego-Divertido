package com.maykol.juegodivertido.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.maykol.juegodivertido.R
import com.maykol.juegodivertido.core.StoreCatalog
import com.maykol.juegodivertido.core.UpgradeCatalog
import com.maykol.juegodivertido.core.UpgradeDefinition
import com.maykol.juegodivertido.game.AppViewModel

@Composable
fun ShopScreen(viewModel: AppViewModel) {
    val activity = LocalContext.current as Activity
    var selectedTab by remember { mutableIntStateOf(0) }
    val progress = viewModel.progress

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.shop_title), style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(R.string.menu_coins, progress.coins))
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(stringResource(R.string.shop_tab_upgrades)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(stringResource(R.string.shop_tab_store)) }
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            if (selectedTab == 0) {
                UpgradesTab(viewModel)
            } else {
                RealMoneyStoreTab(viewModel, activity)
            }
        }

        OutlinedButton(
            onClick = { viewModel.goToMenu() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.shop_back))
        }
    }
}

@Composable
private fun UpgradesTab(viewModel: AppViewModel) {
    val progress = viewModel.progress
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(UpgradeCatalog.ALL) { upgrade: UpgradeDefinition ->
            val owned = upgrade.id in progress.ownedUpgrades
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = upgrade.displayNameEs, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = upgrade.descriptionEs, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (owned) {
                        Text(text = stringResource(R.string.shop_owned), color = MaterialTheme.colorScheme.secondary)
                    } else {
                        val canAfford = progress.coins >= upgrade.cost
                        Button(
                            onClick = { viewModel.buyUpgrade(upgrade.id) },
                            enabled = canAfford
                        ) {
                            Text(
                                text = if (canAfford) {
                                    stringResource(R.string.shop_buy, upgrade.cost)
                                } else {
                                    stringResource(R.string.shop_not_enough_coins)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class StoreDisplay(val titleRes: Int, val descRes: Int?)

private val storeDisplayByProductId: Map<String, StoreDisplay> = mapOf(
    StoreCatalog.REMOVE_ADS to StoreDisplay(R.string.store_remove_ads_title, R.string.store_remove_ads_desc),
    StoreCatalog.COINS_SMALL to StoreDisplay(R.string.store_coins_small_title, null),
    StoreCatalog.COINS_MEDIUM to StoreDisplay(R.string.store_coins_medium_title, null),
    StoreCatalog.COINS_LARGE to StoreDisplay(R.string.store_coins_large_title, null),
    StoreCatalog.STARTER_PACK to StoreDisplay(R.string.store_starter_pack_title, R.string.store_starter_pack_desc)
)

@Composable
private fun RealMoneyStoreTab(viewModel: AppViewModel, activity: Activity) {
    val progress = viewModel.progress
    val productDetails = viewModel.billingManager.productDetails

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(StoreCatalog.PRODUCTS) { product ->
            val display = storeDisplayByProductId[product.productId] ?: return@items
            val alreadyOwned = product.grantsRemoveAds && progress.removeAdsOwned
            val details = productDetails[product.productId]
            val price = details?.oneTimePurchaseOfferDetails?.formattedPrice

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(display.titleRes), style = MaterialTheme.typography.headlineMedium)
                    display.descRes?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stringResource(it), style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    when {
                        alreadyOwned -> Text(
                            text = stringResource(R.string.store_purchased),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        details == null -> Text(text = stringResource(R.string.store_loading))
                        else -> Button(onClick = { viewModel.buyRealMoneyProduct(activity, product.productId) }) {
                            Text(text = price ?: stringResource(R.string.store_price_unavailable))
                        }
                    }
                }
            }
        }
    }
}
