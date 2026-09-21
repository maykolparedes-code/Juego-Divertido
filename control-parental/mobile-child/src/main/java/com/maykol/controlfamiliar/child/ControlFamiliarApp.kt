package com.maykol.controlfamiliar.child

import android.app.Application
import com.maykol.controlfamiliar.child.network.ApiClient

class ControlFamiliarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}
