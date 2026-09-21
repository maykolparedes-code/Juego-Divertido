package com.maykol.controlfamiliar.child.screenshare

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Base64
import androidx.core.app.NotificationCompat
import com.maykol.controlfamiliar.child.MainActivity
import com.maykol.controlfamiliar.child.R
import java.io.ByteArrayOutputStream

/**
 * Captura periódica de pantalla — no video continuo — mientras dura una
 * sesión de "compartir pantalla" que el menor aceptó explícitamente (ver
 * ScreenShareSignaling y el diálogo de consentimiento en MainActivity).
 *
 * Corre como foreground service tipo `mediaProjection` con notificación
 * persistente OBLIGATORIA del sistema, con botón "Detener" — el mismo
 * principio que LocationForegroundService: nunca invisible, y el menor
 * puede cortarlo en cualquier momento.
 *
 * Frecuencia baja (1 cuadro/segundo) a propósito: es la versión simple de
 * este scaffold. Para video en vivo de baja latencia real, la ruta
 * recomendada es WebRTC (ver ARCHITECTURE.md) — requiere señalización más
 * completa y, en la práctica, un servidor TURN.
 */
class ScreenShareForegroundService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private lateinit var captureThread: HandlerThread
    private lateinit var captureHandler: Handler

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            stopSelf()
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        captureThread = HandlerThread("ScreenShareCapture").apply { start() }
        captureHandler = Handler(captureThread.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Debe llamarse dentro de los primeros milisegundos del arranque,
        // antes de tocar MediaProjection — es lo que fuerza la
        // notificación persistente a existir siempre mientras esto corre.
        startForeground(NOTIFICATION_ID, buildNotification())

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
        if (data == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val projection = projectionManager.getMediaProjection(resultCode, data)
        mediaProjection = projection
        // Debe registrarse antes de crear el VirtualDisplay (exigido desde Android 14).
        projection.registerCallback(mediaProjectionCallback, captureHandler)

        startCapturing(projection)
        return START_NOT_STICKY
    }

    private fun startCapturing(projection: MediaProjection) {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader = reader

        virtualDisplay = projection.createVirtualDisplay(
            "ControlFamiliarScreenShare",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            captureHandler,
        )

        captureHandler.post(object : Runnable {
            override fun run() {
                captureFrame(reader, width, height)
                captureHandler.postDelayed(this, FRAME_INTERVAL_MS)
            }
        })
    }

    private fun captureFrame(reader: ImageReader, width: Int, height: Int) {
        val image = runCatching { reader.acquireLatestImage() }.getOrNull() ?: return
        try {
            val plane = image.planes[0]
            val buffer = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * width

            val bitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride,
                height,
                Bitmap.Config.ARGB_8888,
            )
            bitmap.copyPixelsFromBuffer(buffer)

            val cropped = Bitmap.createBitmap(bitmap, 0, 0, width, height)
            val output = ByteArrayOutputStream()
            cropped.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            val base64 = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)

            ScreenShareSignaling.sendFrame(base64)

            bitmap.recycle()
            cropped.recycle()
        } finally {
            image.close()
        }
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, ScreenShareForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location_notification)
            .setContentTitle("Compartiendo pantalla en vivo")
            .setContentText("Tu familia está viendo tu pantalla ahora. Toca para detener.")
            .setOngoing(true)
            .setContentIntent(openAppIntent)
            .addAction(0, "Detener", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Compartir pantalla",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = "Aviso mientras tu pantalla se comparte en vivo" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        ScreenShareSignaling.notifyStopped()
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.unregisterCallback(mediaProjectionCallback)
        mediaProjection?.stop()
        captureThread.quitSafely()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "screen_share"
        private const val NOTIFICATION_ID = 1002
        private const val FRAME_INTERVAL_MS = 1000L
        private const val JPEG_QUALITY = 50

        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_RESULT_DATA = "resultData"
        const val ACTION_STOP = "com.maykol.controlfamiliar.child.ACTION_STOP_SCREEN_SHARE"
    }
}
