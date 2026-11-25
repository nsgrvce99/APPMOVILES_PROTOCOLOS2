package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.network.*
import kotlinx.coroutines.CancellationException
import cl.duocuc.miregistro.model.ProtocoloDetallado
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.FondoFooter
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
    val api = LocalApiTerreno.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }
    val operacion = recordarOperacion()


    var misProtocolos by remember { mutableStateOf<List<ProtocoloDetallado>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var offset by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 20
    val listState = rememberLazyListState()


    var protocoloAEliminar by remember { mutableStateOf<ProtocoloDetallado?>(null) }
    var errorEliminar by remember { mutableStateOf<String?>(null) }


    var errorCarga by remember { mutableStateOf<String?>(null) }
    suspend fun cargarProtocolos(reset: Boolean = false) {
        if (isLoading) return
        if (reset) {
            offset = 0
            hasMore = true
            misProtocolos = emptyList()
        }
        if (!hasMore) return

        isLoading = true
        errorCarga = null
        try {
            val nuevos = if (api == null) checkNotNull(dbHelper)
                .listarProtocolosPorTecnicoDetalladosPaginado(usuarioActual?.id ?: 0L, limit, offset)
            else api.misProtocolos(offset / limit, limit).content.map { api.protocolo(it.id).aTarjeta() }
            misProtocolos += nuevos
            offset += limit
            hasMore = nuevos.size == limit
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            errorCarga = mensajeRed(error)
        } finally {
            isLoading = false
        }
    }


    LaunchedEffect(Unit) {
        cargarProtocolos(reset = true)
    }


    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= totalItems - 3
        }.collect { shouldLoadMore ->
            if (shouldLoadMore) cargarProtocolos()
        }
    }

    Scaffold(
        topBar = { AppTopBar(titulo = "Mis Pruebas en Terreno", onVolver = onVolver) }
    ) { innerPadding ->
        FondoFooter(
            modifier = Modifier.padding(innerPadding)
        ) {
            if (misProtocolos.isEmpty() && !isLoading) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aún no has registrado pruebas.")
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(Dimens.espacioMedio),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    items(misProtocolos) { proto ->
                        TarjetaMisProtocolos(
                            protocolo = proto,
                            enServidor = api != null,
                            onEditar = { onEditarProtocolo(proto.id) },
                            onEliminar = { protocoloAEliminar = proto; errorEliminar = null },
                            onVerDetalle = { onVerDetalleProtocolo(proto.id) },
                            onCerrarBorrador = {
                                operacion.ejecutar {
                                    if (api == null) checkNotNull(dbHelper).cerrarBorrador(proto.id)
                                    else api.cerrarProtocolo(proto.id)
                                    cargarProtocolos(reset = true)
                                }
                            }
                        )
                    }


                    if (errorCarga != null || (operacion.error != null && protocoloAEliminar == null)) {
                        item {
                            Text(errorCarga ?: operacion.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = { operacion.ejecutar { cargarProtocolos(reset = true) } }) { Text("Reintentar") }
                        }
                    }

                    if (isLoading) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }


    val anula = api != null && protocoloAEliminar?.estado != "BORRADOR"
    if (protocoloAEliminar != null) {
        AlertDialog(
            onDismissRequest = {
                protocoloAEliminar = null
                errorEliminar = null
            },
            title = { Text("¿Eliminar protocolo?") },
            text = {
                Column {
                    if (errorEliminar == null && operacion.error == null) {
                        Text("Esta acción borrará la prueba del equipo ${protocoloAEliminar?.equipoTag}. No se puede deshacer.")
                    } else {
                        Text(errorEliminar ?: checkNotNull(operacion.error), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val seleccionado = checkNotNull(protocoloAEliminar)
                        operacion.ejecutar {
                            if (api == null) checkNotNull(dbHelper).eliminarProtocolo(seleccionado.id)
                            else if (seleccionado.estado == "BORRADOR") api.eliminarProtocolo(seleccionado.id).verificar()
                            else api.anularProtocolo(seleccionado.id)
                            protocoloAEliminar = null
                            errorEliminar = null
                            cargarProtocolos(reset = true)
                        }
                    },
                    enabled = errorEliminar == null
                ) { Text(if (anula) "Anular" else "Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = {
                    protocoloAEliminar = null
                    errorEliminar = null
                }) { Text("Cancelar") }
            }
        )
    }
}