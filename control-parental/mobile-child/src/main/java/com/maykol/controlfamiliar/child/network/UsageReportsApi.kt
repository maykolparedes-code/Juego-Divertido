package com.maykol.controlfamiliar.child.network

import org.json.JSONObject
import java.time.Instant

class UsageReportsApi {
    suspend fun recordSession(appPackage: String, startedAt: Long, endedAt: Long) {
        HttpClient.postJson(
            "usage-reports/sessions",
            JSONObject()
                .put("deviceId", DeviceSession.requireDeviceId())
                .put("appPackage", appPackage)
                .put("category", AppCategoryMapper.categoryFor(appPackage))
                .put("startedAt", Instant.ofEpochMilli(startedAt).toString())
                .put("endedAt", Instant.ofEpochMilli(endedAt).toString()),
        )
    }
}
