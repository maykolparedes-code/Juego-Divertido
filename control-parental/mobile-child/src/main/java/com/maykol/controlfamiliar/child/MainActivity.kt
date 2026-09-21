package com.maykol.controlfamiliar.child

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maykol.controlfamiliar.child.geofencing.GeofenceManager
import com.maykol.controlfamiliar.child.location.LocationForegroundService
import com.maykol.controlfamiliar.child.network.ApiClient
import com.maykol.controlfamiliar.child.network.DeviceSession
import com.maykol.controlfamiliar.child.onboarding.PermissionOnboardingActivity
import com.maykol.controlfamiliar.child.screentime.ScreenTimeRulesCache
import com.maykol.controlfamiliar.child.sos.SosPanicButton
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

/**
 * Pantalla principal de la app del menor. En un producto terminado, el
 * emparejamiento (canjear el código de invitación) sería el primer paso
 * de un onboarding guiado; aquí se deja como un campo simple para poder
 * probar la app completa usando los IDs que imprime
 * `backend/prisma/seed.ts` al correrlo.
 */
class MainActivity : ComponentActivity() {

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { /* revisar resultados si se necesita feedback en pantalla */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        onRequestCorePermissions = { requestCorePermissions() },
                        onOpenAccessibilitySettings = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        onOpenUsageAccessSettings = {
                            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                        onOpenPrivacyScreen = {
                            startActivity(Intent(this, PermissionOnboardingActivity::class.java))
                        },
                        onStartMonitoring = { startMonitoringServices() },
                    )
                }
            }
        }
    }

    private fun requestCorePermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        requestPermissions.launch(permissions.toTypedArray())
        // ACCESS_BACKGROUND_LOCATION debe pedirse en una segunda solicitud
        // aparte, después de que el usuario conceda la ubicación en
        // primer plano (requisito de Android 11+).
    }

    private fun startMonitoringServices() {
        if (!DeviceSession.isConfigured) return

        val serviceIntent = Intent(this, LocationForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        lifecycleScope.launch {
            runCatching { GeofenceManager(this@MainActivity).syncGeofences(ApiClient.geofencingApi.listSafeZones()) }
            runCatching { ScreenTimeRulesCache.update(ApiClient.screenTimeApi.listRules()) }
        }
    }
}

@Composable
private fun HomeScreen(
    onRequestCorePermissions: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenPrivacyScreen: () -> Unit,
    onStartMonitoring: () -> Unit,
) {
    var deviceId by remember { mutableStateOf(DeviceSession.deviceId.orEmpty()) }
    var familyId by remember { mutableStateOf(DeviceSession.familyId.orEmpty()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Control Familiar", style = MaterialTheme.typography.headlineMedium)
        Text("Esta app comparte tu ubicación y tiempo de uso con tu familia.")

        OutlinedTextField(
            value = deviceId,
            onValueChange = { deviceId = it },
            label = { Text("Device ID (prueba)") },
        )
        OutlinedTextField(
            value = familyId,
            onValueChange = { familyId = it },
            label = { Text("Family ID (prueba)") },
        )
        Button(onClick = { DeviceSession.configureForTesting(deviceId, familyId) }) {
            Text("Vincular (solo para pruebas)")
        }

        Button(onClick = onRequestCorePermissions) { Text("Dar permiso de ubicación") }
        Button(onClick = onOpenAccessibilitySettings) { Text("Activar accesibilidad (límites de tiempo)") }
        Button(onClick = onOpenUsageAccessSettings) { Text("Activar acceso a uso de apps") }
        Button(onClick = onStartMonitoring) { Text("Iniciar monitoreo") }
        Button(onClick = onOpenPrivacyScreen) { Text("Qué comparte esta app") }

        SosPanicButton()
    }
}
