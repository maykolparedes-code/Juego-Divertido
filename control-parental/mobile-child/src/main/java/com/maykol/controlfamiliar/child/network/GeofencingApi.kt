package com.maykol.controlfamiliar.child.network

import com.maykol.controlfamiliar.child.geofencing.SafeZone
import org.json.JSONObject

class GeofencingApi {
    suspend fun reportEvent(geofenceId: String, type: String) {
        HttpClient.postJson(
            "geofences/events",
            JSONObject()
                .put("geofenceId", geofenceId)
                .put("deviceId", DeviceSession.requireDeviceId())
                .put("familyId", DeviceSession.requireFamilyId())
                .put("type", type),
        )
    }

    suspend fun listSafeZones(): List<SafeZone> {
        val array = HttpClient.getJsonArray("geofences/family/${DeviceSession.requireFamilyId()}")
        return (0 until array.length()).map { i ->
            val json = array.getJSONObject(i)
            SafeZone(
                id = json.getString("id"),
                name = json.getString("name"),
                lat = json.getDouble("lat"),
                lng = json.getDouble("lng"),
                radiusMeters = json.getDouble("radiusMeters").toFloat(),
            )
        }
    }
}
