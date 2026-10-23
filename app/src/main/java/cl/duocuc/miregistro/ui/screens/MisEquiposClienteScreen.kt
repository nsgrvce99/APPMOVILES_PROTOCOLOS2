package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisEquiposClienteScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onVerHistorialEquipo: (Long) -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    // 👇 Traemos SOLO los equipos que pertenecen a este cliente (RN-18)
    val misEquipos = remember(usuarioActual?.id) {
        if (usuarioActual != null) dbHelper.listarEquiposPorCliente(usuarioActual.id) else emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Equipos") },
                navigationIcon = { IconButton(onClick = onVolver) { Text("←") } }
            )
        }
    ) { innerPadding ->
        if (misEquipos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No tiene equipos registrados en la plataforma.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                item {
                    Text(
                        "Equipos de ${usuarioActual?.nombre}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(misEquipos) { equipo ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimens.espacioMedio)) {
                            Text("${equipo.tipoEquipo} - ${equipo.tag}", style = MaterialTheme.typography.titleLarge)
                            Text("Marca: ${equipo.marca} | Serie: ${equipo.serie}")
                            Text("Instalación: ${equipo.instalacion}")
                            Spacer(Modifier.height(Dimens.espacioChico))
                            Button(
                                onClick = { onVerHistorialEquipo(equipo.id.toLong()) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ver Historial de Pruebas")
                            }
                        }
                    }
                }
            }
        }
    }
}