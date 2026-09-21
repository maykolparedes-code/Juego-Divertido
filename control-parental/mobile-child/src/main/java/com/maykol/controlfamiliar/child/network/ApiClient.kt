package com.maykol.controlfamiliar.child.network

import android.content.Context

object ApiClient {
    var appContext: Context? = null
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
        DeviceSession.init(context)
    }

    val locationApi = LocationApi()
    val geofencingApi = GeofencingApi()
    val alertsApi = AlertsApi()
    val usageReportsApi = UsageReportsApi()
    val screenTimeApi = ScreenTimeApi()
}
