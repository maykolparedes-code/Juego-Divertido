package com.maykol.controlfamiliar.child.network

import android.content.Context
import android.content.SharedPreferences

/**
 * Identidad del dispositivo dentro de su familia, obtenida al canjear el
 * código de invitación (ver AuthController.redeemInvite en el backend).
 * Se persiste en SharedPreferences para no perderla al reiniciar la app.
 *
 * Nota: el flujo real de onboarding (pantalla para ingresar el código de
 * invitación) no está incluido en este scaffold — ver
 * PermissionOnboardingActivity.kt. Mientras tanto, `configureForTesting`
 * permite fijar manualmente el deviceId/familyId de la familia de prueba
 * creada por `backend/prisma/seed.ts`, para poder probar la app de punta
 * a punta sin haber construido esa pantalla todavía.
 */
object DeviceSession {
    private const val PREFS_NAME = "device_session"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_FAMILY_ID = "family_id"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var deviceId: String?
        get() = prefs.getString(KEY_DEVICE_ID, null)
        private set(value) = prefs.edit().putString(KEY_DEVICE_ID, value).apply()

    var familyId: String?
        get() = prefs.getString(KEY_FAMILY_ID, null)
        private set(value) = prefs.edit().putString(KEY_FAMILY_ID, value).apply()

    fun configureForTesting(deviceId: String, familyId: String) {
        this.deviceId = deviceId
        this.familyId = familyId
    }

    fun requireDeviceId(): String =
        deviceId ?: error("Dispositivo no vinculado a una familia todavía")

    fun requireFamilyId(): String =
        familyId ?: error("Dispositivo no vinculado a una familia todavía")

    val isConfigured: Boolean get() = deviceId != null && familyId != null
}
