package com.maykol.controlfamiliar.child

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.maykol.controlfamiliar.child.location.LocationForegroundService
import com.maykol.controlfamiliar.child.network.DeviceSession

/**
 * Reinicia el servicio de ubicación después de que el dispositivo arranca,
 * para que la supervisión no dependa de que el menor abra la app
 * manualmente cada vez. Solo actúa si el dispositivo ya está vinculado a
 * una familia.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        DeviceSession.init(context)
        if (!DeviceSession.isConfigured) return

        val serviceIntent = Intent(context, LocationForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
