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
import cl.duocuc.miregistro.network.*
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.FondoFooter
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
    val api = LocalApiTerreno.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }
    val operacion = recordarOperacion()
    var revision by remember { mutableIntStateOf(0) }
    val carga by cargar(instalacionId, revision, api) {
        if (api == null) checkNotNull(dbHelper).listarEquiposPorInstalacion(nombreInstalacion)
        else todasLasPaginas { api.equipos(instalacionId, it) }
    }


    var equipoAEliminar by remember { mutableStateOf<Equipo?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Equipos - $nombreInstalacion",
                onVolver = onVolver
            )
        }
    ) { innerPadding ->
        FondoFooter(
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.espacioPantalla)
            ) {
                Button(
                    onClick = onRegistrarEquipoNuevo,
                    modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.espacioMedio)
                ) {
                    Text("Registrar Equipo Nuevo")
                }

                if (operacion.error != null) {
                    Text(checkNotNull(operacion.error), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
                }

                ContenidoRemoto(carga, { revision++ }) { equipos ->
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
    }


    }
    if (equipoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { equipoAEliminar = null },
            title = { Text("¿Eliminar Equipo?") },
            text = { Text("Se eliminará el equipo ${equipoAEliminar?.tag}. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    val seleccionado = checkNotNull(equipoAEliminar)
                    equipoAEliminar = null
                    operacion.ejecutar {
                        if (api == null) checkNotNull(dbHelper).eliminarEquipo(seleccionado.id.toLong())
                        else api.eliminarEquipo(seleccionado.id.toLong()).verificar()
                        revision++
                    }
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { equipoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}