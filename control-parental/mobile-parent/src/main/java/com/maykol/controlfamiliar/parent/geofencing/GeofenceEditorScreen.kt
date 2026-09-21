package com.maykol.controlfamiliar.parent.geofencing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maykol.controlfamiliar.parent.network.ApiClient
import kotlinx.coroutines.launch

data class SafeZoneDraft(
    val name: String,
    val lat: Double,
    val lng: Double,
    val radiusMeters: Float,
)

/**
 * El padre dibuja un círculo en el mapa (casa, colegio, etc.); al guardar,
 * se envía al backend y el próximo `syncGeofences` de la app del menor
 * (GeofenceManager.kt) la registra en la Geofencing API del sistema.
 */
class GeofenceEditorViewModel : ViewModel() {

    fun saveSafeZone(familyId: String, draft: SafeZoneDraft, onSaved: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                ApiClient.geofencingApi.createGeofence(
                    familyId = familyId,
                    name = draft.name,
                    lat = draft.lat,
                    lng = draft.lng,
                    radiusMeters = draft.radiusMeters,
                )
            }.onSuccess { onSaved() }
        }
    }
}
