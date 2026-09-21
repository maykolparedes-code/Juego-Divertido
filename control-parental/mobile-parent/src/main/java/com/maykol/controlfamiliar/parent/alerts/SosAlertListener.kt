package com.maykol.controlfamiliar.parent.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.maykol.controlfamiliar.parent.R

/**
 * Recibe los pushes de alta prioridad (SOS, geocerca, límite de tiempo)
 * enviados desde AlertsService en el backend y los muestra con un canal
 * de notificación de alta importancia (sonido + vibración), incluso con
 * la app del padre cerrada.
 */
class SosAlertListener : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val type = message.data["type"] ?: return
        val title = message.notification?.title ?: "Alerta de Control Familiar"
        val body = message.notification?.body ?: ""

        ensureChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(if (type == "SOS") NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .build()

        getSystemService(NotificationManager::class.java)
            .notify(message.data["deviceId"].hashCode(), notification)
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alertas de la familia",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = "SOS, geocercas y límites de tiempo" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "family_alerts"
    }
}
