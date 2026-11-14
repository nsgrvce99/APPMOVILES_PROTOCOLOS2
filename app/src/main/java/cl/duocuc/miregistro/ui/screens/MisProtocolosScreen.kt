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
import cl.duocuc.miregistro.model.ProtocoloDetallado
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.TarjetaMisProtocolos
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisProtocolosScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onEditarProtocolo: (Long) -> Unit,
    onVerDetalleProtocolo: (Long) -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var misProtocolos by remember { mutableStateOf<List<ProtocoloDetallado>>(emptyList()) }
    var protocoloAEliminar by remember { mutableStateOf<ProtocoloDetallado?>(null) }

    fun recargarProtocolos() {
        misProtocolos = dbHelper.listarProtocolosPorTecnicoDetallados(usuarioActual?.id ?: 0L)
    }

    LaunchedEffect(Unit) {
        recargarProtocolos()
    }

    Scaffold(
        topBar = { AppTopBar(titulo = "Mis Pruebas en Terreno", onVolver = onVolver) }
    ) { innerPadding ->
        if (misProtocolos.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no has registrado pruebas.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                items(misProtocolos) { proto ->
                    TarjetaMisProtocolos(
                        protocolo = proto,
                        onEditar = { onEditarProtocolo(proto.id) },
                        onEliminar = { protocoloAEliminar = proto },
                        onVerDetalle = { onVerDetalleProtocolo(proto.id) },
                        onCerrarBorrador = {
                            dbHelper.cerrarBorrador(proto.id)
                            recargarProtocolos()
                        }
                    )
                }
            }
        }
    }


    if (protocoloAEliminar != null) {
        AlertDialog(
            onDismissRequest = { protocoloAEliminar = null },
            title = { Text("¿Eliminar protocolo?") },
            text = { Text("Esta acción borrará la prueba del equipo ${protocoloAEliminar?.equipoTag}. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    dbHelper.eliminarProtocolo(protocoloAEliminar!!.id)
                    protocoloAEliminar = null
                    recargarProtocolos()
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { protocoloAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}