package com.maykol.controlfamiliar.parent.network

import android.content.Context

object ApiClient {
    var appContext: Context? = null
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
        FamilySession.init(context)
    }

    val dashboardApi = DashboardApi()
    val geofencingApi = GeofencingApi()
}
