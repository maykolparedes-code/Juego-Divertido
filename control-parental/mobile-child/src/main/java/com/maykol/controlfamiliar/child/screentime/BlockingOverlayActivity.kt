package com.maykol.controlfamiliar.child.screentime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Pantalla de bloqueo mostrada sobre cualquier app cuando se supera el
 * límite diario configurado por el padre. Explica claramente por qué se
 * bloqueó (nunca un bloqueo silencioso o un "crash" fingido) y ofrece la
 * opción de pedirle al padre tiempo extra.
 */
class BlockingOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE).orEmpty()
        setContent {
            BlockingScreen(
                blockedPackage = blockedPackage,
                onRequestExtraTime = { ExtraTimeRequester.request(blockedPackage) },
                onClose = { finish() },
            )
        }
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
    }
}

@Composable
private fun BlockingScreen(
    blockedPackage: String,
    onRequestExtraTime: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Se acabó el tiempo para hoy")
        Text("Límite alcanzado para: $blockedPackage")
        Button(onClick = onRequestExtraTime) { Text("Pedir más tiempo a mamá/papá") }
        Button(onClick = onClose) { Text("Entendido") }
    }
}

object ExtraTimeRequester {
    fun request(packageName: String) {
        // Implementación real: POST a un endpoint /screen-time/extra-request
        // que dispara una alerta push al padre con botones "Aprobar 15 min" / "Denegar".
    }
}
