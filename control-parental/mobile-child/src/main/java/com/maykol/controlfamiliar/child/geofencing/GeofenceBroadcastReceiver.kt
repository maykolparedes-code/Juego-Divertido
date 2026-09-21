package com.maykol.controlfamiliar.child.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.maykol.controlfamiliar.child.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            val error = GeofenceStatusCodes.getStatusCodeString(event.errorCode)
            android.util.Log.w("GeofenceReceiver", "Error de geocerca: $error")
            return
        }

        val transitionType = when (event.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTER"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "EXIT"
            else -> return
        }

        CoroutineScope(Dispatchers.IO).launch {
            event.triggeringGeofences?.forEach { geofence ->
                runCatching {
                    ApiClient.geofencingApi.reportEvent(
                        geofenceId = geofence.requestId,
                        type = transitionType,
                    )
                }
            }
        }
    }
}
