package com.maykol.controlfamiliar.child.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.maykol.controlfamiliar.child.network.ApiClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Botón de pánico, siempre visible en la pantalla principal de la app del
 * menor. Al pulsarlo, obtiene la ubicación de alta precisión inmediata y
 * envía la alerta SOS — no espera al próximo ping periódico del servicio
 * de ubicación en segundo plano.
 */
@Composable
fun SosPanicButton(modifier: Modifier = Modifier) {
    var sending by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            if (sending) return@Button
            sending = true
            scope.launch {
                sendSosAlert()
                sending = false
                sent = true
            }
        },
        modifier = modifier
            .padding(16.dp)
            .background(Color.Red, CircleShape),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
    ) {
        Text(if (sent) "Alerta enviada" else if (sending) "Enviando…" else "SOS")
    }
}

private suspend fun sendSosAlert() {
    val fusedLocationClient = ApiClient.appContext?.let {
        LocationServices.getFusedLocationProviderClient(it)
    } ?: return

    val location = runCatching {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
    }.getOrNull()

    ApiClient.alertsApi.createAlert(
        type = "SOS",
        lat = location?.latitude,
        lng = location?.longitude,
        message = "Botón de pánico activado por el menor",
    )
}
