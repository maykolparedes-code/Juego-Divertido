package com.maykol.controlfamiliar.parent.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

/**
 * Mapa en vivo: se conecta al gateway WebSocket del backend y actualiza la
 * posición del/los hijo(s) en tiempo real, sin necesidad de refrescar
 * manualmente. Los pings llegan con la frecuencia configurada en el
 * servicio de ubicación del menor (por defecto, cada 3 min) más los
 * eventos inmediatos de SOS y geocerca.
 */
@Composable
fun LiveLocationScreen(familyId: String, backendWsUrl: String) {
    var lastPositions by remember { mutableStateOf<Map<String, LatLng>>(emptyMap()) }

    DisposableEffect(familyId) {
        val socket: Socket = IO.socket("$backendWsUrl/live")
        socket.on(Socket.EVENT_CONNECT) {
            socket.emit("join-family", JSONObject().put("familyId", familyId))
        }
        socket.on("location:update") { args ->
            val payload = args[0] as JSONObject
            val deviceId = payload.getString("deviceId")
            val lat = payload.getDouble("lat")
            val lng = payload.getDouble("lng")
            lastPositions = lastPositions + (deviceId to LatLng(lat, lng))
        }
        socket.connect()

        onDispose { socket.disconnect() }
    }

    MapView(positions = lastPositions)
}

data class LatLng(val lat: Double, val lng: Double)

@Composable
private fun MapView(positions: Map<String, LatLng>) {
    // Integración real: Google Maps Compose (`com.google.maps.android:maps-compose`)
    // pintando un Marker por cada entrada de `positions`, omitido aquí por brevedad.
}
