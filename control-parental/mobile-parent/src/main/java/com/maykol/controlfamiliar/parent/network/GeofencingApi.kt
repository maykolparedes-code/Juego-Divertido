package com.maykol.controlfamiliar.parent.network

import org.json.JSONObject

class GeofencingApi {
    suspend fun createGeofence(familyId: String, name: String, lat: Double, lng: Double, radiusMeters: Float) {
        HttpClient.postJson(
            "geofences",
            JSONObject()
                .put("familyId", familyId)
                .put("name", name)
                .put("lat", lat)
                .put("lng", lng)
                .put("radiusMeters", radiusMeters),
        )
    }
}
