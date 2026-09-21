package com.maykol.controlfamiliar.parent.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maykol.controlfamiliar.parent.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardState(
    val childDevices: List<ChildDeviceSummary> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

data class ChildDeviceSummary(
    val deviceId: String,
    val childName: String,
    val lastSeenAt: String?,
    val minutesUsedToday: Int,
    val topCategory: String,
    val insideSafeZone: Boolean,
)

class DashboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun loadDashboard(familyId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                ApiClient.dashboardApi.getFamilySummary(familyId)
            }.onSuccess { devices ->
                _state.value = DashboardState(childDevices = devices, loading = false)
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }
}
