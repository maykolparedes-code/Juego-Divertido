package com.maykol.controlfamiliar.child.screenshare

import com.maykol.controlfamiliar.child.BuildConfig
import com.maykol.controlfamiliar.child.network.DeviceSession
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Conexión en tiempo real con el backend para compartir pantalla BAJO
 * PEDIDO. El servidor solo reenvía mensajes entre el padre y el menor
 * (ver screenshare.gateway.ts) — nunca decide iniciar una captura por su
 * cuenta. El menor siempre ve el pedido y decide; si acepta, Android
 * además exige su propio diálogo de sistema antes de compartir un solo
 * cuadro.
 */
object ScreenShareSignaling {
    private var socket: Socket? = null

    private val _incomingRequest = MutableStateFlow(false)
    val incomingRequest: StateFlow<Boolean> = _incomingRequest.asStateFlow()

    fun connect() {
        if (socket?.connected() == true) return
        val s = IO.socket("${BuildConfig.WS_BASE_URL}/screen-share")
        socket = s

        s.on(Socket.EVENT_CONNECT) {
            s.emit("join-family", JSONObject().put("familyId", DeviceSession.requireFamilyId()))
        }
        s.on("screen-share:request") { args ->
            val payload = args.getOrNull(0) as? JSONObject ?: return@on
            if (payload.optString("deviceId") == DeviceSession.deviceId) {
                _incomingRequest.value = true
            }
        }
        s.connect()
    }

    fun respond(accepted: Boolean) {
        _incomingRequest.value = false
        socket?.emit(
            "screen-share:response",
            JSONObject()
                .put("familyId", DeviceSession.requireFamilyId())
                .put("deviceId", DeviceSession.requireDeviceId())
                .put("accepted", accepted),
        )
    }

    fun sendFrame(jpegBase64: String) {
        socket?.emit(
            "screen-share:frame",
            JSONObject()
                .put("familyId", DeviceSession.requireFamilyId())
                .put("deviceId", DeviceSession.requireDeviceId())
                .put("jpegBase64", jpegBase64),
        )
    }

    fun notifyStopped() {
        socket?.emit(
            "screen-share:stop",
            JSONObject()
                .put("familyId", DeviceSession.requireFamilyId())
                .put("deviceId", DeviceSession.requireDeviceId()),
        )
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}
