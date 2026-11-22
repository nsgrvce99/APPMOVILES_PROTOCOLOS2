package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Equipo
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


    var equipos by remember { mutableStateOf(dbHelper.listarEquiposPorInstalacion(nombreInstalacion)) }
    var equipoAEliminar by remember { mutableStateOf<Equipo?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    fun recargar() {
        equipos = dbHelper.listarEquiposPorInstalacion(nombreInstalacion)
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

            if (mensajeError != null) {
                Text(mensajeError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
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
                            onRegistrarPrueba = { onSeleccionarEquipoParaPrueba(equipo.id.toLong()) },
                            onEliminar = { equipoAEliminar = equipo }
                        )
                    }
                }
            }
        }
    }


    if (equipoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { equipoAEliminar = null },
            title = { Text("¿Eliminar Equipo?") },
            text = { Text("Se eliminará el equipo ${equipoAEliminar?.tag}. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        dbHelper.eliminarEquipo(equipoAEliminar!!.id.toLong())
                        recargar()
                        mensajeError = null
                    } catch (e: Exception) {
                        mensajeError = e.message
                    }
                    equipoAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { equipoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}