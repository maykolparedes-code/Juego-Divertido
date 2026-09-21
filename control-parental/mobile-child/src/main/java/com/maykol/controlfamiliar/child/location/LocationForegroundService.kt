package com.maykol.controlfamiliar.child.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.maykol.controlfamiliar.child.MainActivity
import com.maykol.controlfamiliar.child.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Servicio de ubicación en segundo plano.
 *
 * Requisito de Android (API 26+): cualquier acceso a ubicación en segundo
 * plano debe correr como foreground service con una notificación
 * persistente y no descartable mientras el servicio esté activo. No se
 * puede evitar ni ocultar — es la garantía del sistema operativo de que el
 * usuario del dispositivo siempre sabe cuándo se está compartiendo su
 * ubicación. Este servicio no lo intenta evitar: usa un canal de
 * notificación claro y una acción para abrir la pantalla de privacidad.
 *
 * La frecuencia de actualización es intencionalmente baja (cada 3 min) y
 * el cálculo de geocercas ocurre por separado, en segundo plano, vía la
 * Geofencing API del sistema (ver GeofenceBroadcastReceiver), que es
 * mucho más eficiente en batería que sondear la posición constantemente
 * desde aquí.
 */
class LocationForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            scope.launch {
                LocationRepository.reportPing(
                    lat = location.latitude,
                    lng = location.longitude,
                    accuracyMeters = location.accuracy,
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildPersistentNotification())
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            UPDATE_INTERVAL_MS,
        ).setMinUpdateIntervalMillis(UPDATE_INTERVAL_MS / 2).build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            mainLooper,
        )
    }

    /**
     * Notificación obligatoria mientras el servicio está activo. No usa
     * `setOngoing` para ocultarla del usuario (no se puede: Android la
     * fuerza en primer plano) sino para que no desaparezca por accidente
     * al deslizarla, igual que otras apps de ubicación compartida
     * legítimas (Life360, Find My).
     */
    private fun buildPersistentNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location_notification)
            .setContentTitle("Control Familiar · Ubicación activa")
            .setContentText("Compartiendo tu ubicación con tu familia. Toca para ver qué se comparte.")
            .setOngoing(true)
            .setContentIntent(openAppIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Ubicación compartida",
            NotificationManager.IMPORTANCE_LOW, // visible pero sin sonido
        ).apply {
            description = "Muestra cuándo se está compartiendo tu ubicación con tu familia"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "location_sharing"
        private const val NOTIFICATION_ID = 1001
        private const val UPDATE_INTERVAL_MS = 3 * 60 * 1000L // 3 minutos
    }
}
