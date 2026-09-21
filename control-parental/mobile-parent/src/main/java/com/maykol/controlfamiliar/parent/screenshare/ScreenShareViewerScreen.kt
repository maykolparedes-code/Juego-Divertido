package com.maykol.controlfamiliar.parent.screenshare

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.maykol.controlfamiliar.parent.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

private enum class ScreenShareState { IDLE, WAITING_RESPONSE, DECLINED, SHARING }

/**
 * Pantalla del padre para pedir ver la pantalla del menor EN VIVO, bajo
 * pedido. No hay forma de activar esto sin que el menor lo vea y decida
 * en su propia app, y sin que Android le exija su propio diálogo de
 * confirmación — ver ARCHITECTURE.md y ScreenShareSignaling.kt en
 * mobile-child.
 *
 * Los cuadros llegan como capturas periódicas (~1/seg), no como video
 * continuo — suficiente para supervisión puntual, no para ver algo que
 * se mueve rápido. La ruta recomendada para video real de baja latencia
 * es WebRTC (documentado, no implementado en este scaffold).
 */
@Composable
fun ScreenShareViewerScreen(familyId: String, deviceId: String, childName: String) {
    var state by remember { mutableStateOf(ScreenShareState.IDLE) }
    var lastFrame by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var socket by remember { mutableStateOf<Socket?>(null) }

    DisposableEffect(familyId) {
        val s = IO.socket("${BuildConfig.WS_BASE_URL}/screen-share")
        socket = s

        s.on(Socket.EVENT_CONNECT) {
            s.emit("join-family", JSONObject().put("familyId", familyId))
        }
        s.on("screen-share:response") { args ->
            val payload = args.getOrNull(0) as? JSONObject ?: return@on
            if (payload.optString("deviceId") != deviceId) return@on
            state = if (payload.optBoolean("accepted")) ScreenShareState.SHARING else ScreenShareState.DECLINED
        }
        s.on("screen-share:frame") { args ->
            val payload = args.getOrNull(0) as? JSONObject ?: return@on
            if (payload.optString("deviceId") != deviceId) return@on
            val bytes = Base64.decode(payload.optString("jpegBase64"), Base64.NO_WRAP)
            lastFrame = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        s.on("screen-share:stop") { args ->
            val payload = args.getOrNull(0) as? JSONObject ?: return@on
            if (payload.optString("deviceId") != deviceId) return@on
            state = ScreenShareState.IDLE
            lastFrame = null
        }
        s.connect()

        onDispose { s.disconnect() }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Pantalla de $childName", style = MaterialTheme.typography.titleMedium)

        when (state) {
            ScreenShareState.IDLE -> {
                Button(onClick = {
                    state = ScreenShareState.WAITING_RESPONSE
                    socket?.emit(
                        "screen-share:request",
                        JSONObject().put("familyId", familyId).put("deviceId", deviceId),
                    )
                }) {
                    Text("Pedir ver pantalla")
                }
            }
            ScreenShareState.WAITING_RESPONSE -> {
                Text("Esperando que $childName acepte…")
            }
            ScreenShareState.DECLINED -> {
                Text("$childName rechazó la solicitud.")
            }
            ScreenShareState.SHARING -> {
                lastFrame?.let { bitmap ->
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Pantalla de $childName")
                } ?: Text("Compartiendo… esperando el primer cuadro")
                Button(onClick = {
                    socket?.emit(
                        "screen-share:stop",
                        JSONObject().put("familyId", familyId).put("deviceId", deviceId),
                    )
                    state = ScreenShareState.IDLE
                    lastFrame = null
                }) {
                    Text("Detener")
                }
            }
        }
    }
}
