package com.maykol.controlfamiliar.child.network

import org.json.JSONObject

class LocationApi {
    suspend fun ping(request: LocationPingRequest) {
        HttpClient.postJson(
            "location/ping",
            JSONObject()
                .put("deviceId", DeviceSession.requireDeviceId())
                .put("familyId", DeviceSession.requireFamilyId())
                .put("lat", request.lat)
                .put("lng", request.lng)
                .put("accuracyMeters", request.accuracyMeters)
                .put("capturedAt", request.capturedAt),
        )
    }
}
