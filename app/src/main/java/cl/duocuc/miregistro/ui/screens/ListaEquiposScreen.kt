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
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaEquiposScreen(
    instalacionId: Long, // 👈 1. Debe ser Long
    onVolver: () -> Unit,
    onSeleccionarEquipoParaPrueba: (Long) -> Unit, // 👈 2. Debe recibir Long
    onVerHistorialEquipo: (Long) -> Unit, // 👈 3. Debe recibir Long
    onRegistrarEquipoNuevo: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }
    val equipos = remember { dbHelper.listarEquipos() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equipos - Instalación ID: $instalacionId") },
                navigationIcon = { IconButton(onClick = onVolver) { Text("←") } }
            )
        }
    ) { innerPadding ->
        if (equipos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay equipos registrados en la base de datos.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                item {
                    Text("TAGs de Equipos Disponibles:", style = MaterialTheme.typography.titleMedium)
                }
                item {
                    Button(
                        onClick = onRegistrarEquipoNuevo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("➕ Registrar Equipo Nuevo (HU-01)")
                    }
                }
                items(equipos) { equipo ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimens.espacioMedio)) {
                            Text("${equipo.tipoEquipo} ${equipo.tag}", style = MaterialTheme.typography.titleMedium)
                            Text("Marca: ${equipo.marca} | Serie: ${equipo.serie}")
                            Text("Instalación: ${equipo.instalacion}")

                            Spacer(Modifier.height(Dimens.espacioChico))

                            // Aquí está la magia: equipo.id es Int, lo pasamos a Long
                            Button(
                                onClick = { onSeleccionarEquipoParaPrueba(equipo.id.toLong()) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Registrar Prueba")
                            }

                            Spacer(Modifier.height(4.dp))

                            OutlinedButton(
                                onClick = { onVerHistorialEquipo(equipo.id.toLong()) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ver Historial (RF-10)")
                            }
                        }
                    }
                }
            }
        }
    }
}