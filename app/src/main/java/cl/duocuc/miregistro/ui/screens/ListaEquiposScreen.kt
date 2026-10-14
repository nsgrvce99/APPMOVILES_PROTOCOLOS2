package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaEquiposScreen(
    instalacionId: Int,
    onVolver: () -> Unit,
    onSeleccionarEquipoParaPrueba: (Int) -> Unit,
    onVerHistorialEquipo: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equipos - Instalación ID: $instalacionId") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Text("←")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Text("TAGs de Equipos Disponibles:", style = MaterialTheme.typography.titleMedium)

            Button(onClick = { onSeleccionarEquipoParaPrueba(101) }) {
                Text("Registrar Prueba: Transformador TR-01")
            }
            Button(onClick = { onVerHistorialEquipo(101) }) {
                Text("Ver Historial (RF-10): TR-01")
            }
        }
    }
}