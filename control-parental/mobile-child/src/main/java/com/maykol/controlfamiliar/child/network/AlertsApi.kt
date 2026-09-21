package com.maykol.controlfamiliar.child.network

import org.json.JSONObject

class AlertsApi {
    suspend fun createAlert(type: String, lat: Double?, lng: Double?, message: String?) {
        val body = JSONObject()
            .put("familyId", DeviceSession.requireFamilyId())
            .put("deviceId", DeviceSession.requireDeviceId())
            .put("type", type)
        if (lat != null) body.put("lat", lat)
        if (lng != null) body.put("lng", lng)
        if (message != null) body.put("message", message)

        HttpClient.postJson("alerts", body)
    }
}
