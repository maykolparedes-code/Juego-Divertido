package com.maykol.controlfamiliar.parent.network

import android.content.Context
import android.content.SharedPreferences

/** Persiste el familyId localmente para no tener que reingresarlo cada vez. */
object FamilySession {
    private const val PREFS_NAME = "family_session"
    private const val KEY_FAMILY_ID = "family_id"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var familyId: String
        get() = prefs.getString(KEY_FAMILY_ID, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_FAMILY_ID, value).apply()
}
