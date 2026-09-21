package com.maykol.controlfamiliar.child.onboarding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Pantalla mostrada antes de solicitar cada permiso sensible y accesible
 * en cualquier momento desde el menú principal ("Qué comparte esta app").
 * Es texto adicional AL de los diálogos del sistema operativo, nunca un
 * reemplazo.
 */
class PermissionOnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PrivacyExplanationScreen()
                }
            }
        }
    }
}

@Composable
private fun PrivacyExplanationScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Qué comparte esta app", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Ubicación en segundo plano: tu familia puede ver dónde estás, " +
                "incluso con la app cerrada. Verás un aviso fijo en tu barra " +
                "de notificaciones mientras esto esté activo.",
        )
        Text(
            "Accesibilidad: permite que la app sepa qué aplicación tienes " +
                "abierta, para avisarte cuando se acabe el tiempo permitido. " +
                "No puede leer el contenido de tu pantalla.",
        )
        Text(
            "Uso de aplicaciones: permite generar el reporte de cuánto " +
                "tiempo pasas en cada app, agrupado por categoría.",
        )
        Text(
            "Botón SOS: si lo usas, se envía tu ubicación exacta y una " +
                "alerta inmediata a tu familia.",
        )
    }
}
