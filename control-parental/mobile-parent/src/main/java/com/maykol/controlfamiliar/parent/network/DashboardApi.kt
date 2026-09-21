package com.maykol.controlfamiliar.parent.network

import com.maykol.controlfamiliar.parent.dashboard.ChildDeviceSummary

class DashboardApi {
    suspend fun getFamilySummary(familyId: String): List<ChildDeviceSummary> {
        val array = HttpClient.getJsonArray("families/$familyId/summary")
        return (0 until array.length()).map { i ->
            val json = array.getJSONObject(i)
            ChildDeviceSummary(
                deviceId = json.getString("deviceId"),
                childName = json.getString("childName"),
                lastSeenAt = if (json.isNull("lastSeenAt")) null else json.getString("lastSeenAt"),
                minutesUsedToday = json.getInt("minutesUsedToday"),
                topCategory = json.getString("topCategory"),
                insideSafeZone = json.getBoolean("insideSafeZone"),
            )
        }
    }
}
