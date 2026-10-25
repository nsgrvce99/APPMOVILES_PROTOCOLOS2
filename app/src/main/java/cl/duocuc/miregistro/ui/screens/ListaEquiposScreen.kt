package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.TarjetaEquipo
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaEquiposScreen(
    instalacionId: Long,
    nombreInstalacion: String,
    onVolver: () -> Unit,
    onSeleccionarEquipoParaPrueba: (Long) -> Unit,
    onVerHistorialEquipo: (Long) -> Unit,
    onRegistrarEquipoNuevo: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    // Filtramos los equipos por el nombre de la planta
    val equipos = remember(nombreInstalacion) {
        dbHelper.listarEquiposPorInstalacion(nombreInstalacion)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Equipos - $nombreInstalacion",
                onVolver = onVolver
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioPantalla)
        ) {
            Button(
                onClick = onRegistrarEquipoNuevo,
                modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.espacioMedio)
            ) {
                Text("➕ Registrar Equipo Nuevo")
            }

            if (equipos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay equipos registrados en esta instalación.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(Dimens.espacioMedio),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    items(equipos) { equipo ->
                        TarjetaEquipo(
                            equipo = equipo,
                            onVerHistorial = { onVerHistorialEquipo(equipo.id.toLong()) },
                            onRegistrarPrueba = { onSeleccionarEquipoParaPrueba(equipo.id.toLong()) }
                        )
                    }
                }
            }
        }
    }
}