package com.maykol.controlfamiliar.parent

import android.app.Application
import com.maykol.controlfamiliar.parent.network.ApiClient

class ControlFamiliarParentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}
