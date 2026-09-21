package com.maykol.controlfamiliar.child.network

import com.maykol.controlfamiliar.child.screentime.ScreenTimeRule

class ScreenTimeApi {
    suspend fun listRules(): List<ScreenTimeRule> {
        val array = HttpClient.getJsonArray("screen-time/rules/device/${DeviceSession.requireDeviceId()}")
        return (0 until array.length()).mapNotNull { i ->
            val json = array.getJSONObject(i)
            if (json.isNull("appPackage")) return@mapNotNull null // regla de familia/categoría, no de una app específica
            ScreenTimeRule(
                appPackage = json.getString("appPackage"),
                dailyLimitMinutes = json.getLong("dailyLimitMinutes"),
            )
        }
    }
}
