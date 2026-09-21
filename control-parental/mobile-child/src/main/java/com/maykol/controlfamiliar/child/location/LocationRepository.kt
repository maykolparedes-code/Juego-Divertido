package com.maykol.controlfamiliar.child.location

import com.maykol.controlfamiliar.child.network.ApiClient
import java.time.Instant

/**
 * Envía cada ping al backend. Si no hay red, se encola localmente
 * (Room + WorkManager) y se reintenta más tarde — no se pierde el punto,
 * pero tampoco se bloquea el hilo del servicio esperando la respuesta.
 */
object LocationRepository {

    suspend fun reportPing(lat: Double, lng: Double, accuracyMeters: Float) {
        runCatching {
            ApiClient.locationApi.ping(
                LocationPingRequest(
                    lat = lat,
                    lng = lng,
                    accuracyMeters = accuracyMeters,
                    capturedAt = Instant.now().toString(),
                ),
            )
        }.onFailure {
            PendingLocationQueue.enqueue(lat, lng, accuracyMeters)
        }
    }
}

data class LocationPingRequest(
    val lat: Double,
    val lng: Double,
    val accuracyMeters: Float,
    val capturedAt: String,
)
