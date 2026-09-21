package com.maykol.controlfamiliar.parent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maykol.controlfamiliar.parent.dashboard.ChildDeviceSummary
import com.maykol.controlfamiliar.parent.dashboard.DashboardViewModel
import com.maykol.controlfamiliar.parent.network.FamilySession

/**
 * Pantalla principal del padre: ingresa el familyId (impreso por
 * `backend/prisma/seed.ts`), carga el resumen de cada hijo y permite abrir
 * el mapa en vivo. El emparejamiento real vendría de un login, omitido en
 * este scaffold — ver ARCHITECTURE.md.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DashboardScreen()
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    var familyId by remember { mutableStateOf(FamilySession.familyId) }
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Control Familiar", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = familyId,
            onValueChange = { familyId = it },
            label = { Text("Family ID") },
        )
        Button(onClick = {
            FamilySession.familyId = familyId
            viewModel.loadDashboard(familyId)
        }) {
            Text("Cargar familia")
        }

        state.error?.let { Text("Error: $it") }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.childDevices) { child -> ChildSummaryRow(child) }
        }
    }
}

@Composable
private fun ChildSummaryRow(child: ChildDeviceSummary) {
    Column {
        Text(child.childName, style = MaterialTheme.typography.titleMedium)
        Text("Uso hoy: ${child.minutesUsedToday} min · categoría principal: ${child.topCategory}")
        Text(if (child.insideSafeZone) "Dentro de una zona segura" else "Fuera de zonas seguras")
    }
}
