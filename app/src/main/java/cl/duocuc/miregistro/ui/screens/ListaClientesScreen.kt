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
import kotlinx.coroutines.delay
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.FondoFooter
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.utils.Validaciones

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaClientesScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onEditarCliente: (Usuario) -> Unit
) {
    val context = LocalContext.current
    val api = LocalApiTerreno.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }
    val operacion = recordarOperacion()


    var query by remember { mutableStateOf("") }
    var clientes by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var offset by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 20
    val listState = rememberLazyListState()


    var clienteAEliminar by remember { mutableStateOf<Usuario?>(null) }
    var errorEliminar by remember { mutableStateOf<String?>(null) }


    val puedeVer = usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L
    if (!puedeVer) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Acceso denegado.", color = MaterialTheme.colorScheme.error)
        }
        return
    }


    var errorCarga by remember { mutableStateOf<String?>(null) }
    suspend fun cargarClientes(reset: Boolean = false) {
        if (isLoading) return
        if (reset) {
            offset = 0
            hasMore = true
            clientes = emptyList()
        }
        if (!hasMore) return

        isLoading = true
        errorCarga = null
        try {
            val nuevos = if (api == null) checkNotNull(dbHelper).listarClientesPaginado(query, limit, offset)
                else api.clientes(query, limit, offset).map { it.aUsuario() }
            clientes += nuevos
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

    LaunchedEffect(query) {
        if (api != null && query.isNotEmpty()) delay(250)
        cargarClientes(reset = true)
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= totalItems - 3
        }.collect { shouldLoadMore ->
            if (shouldLoadMore) cargarClientes()
        }
    }

    Scaffold(
        topBar = { AppTopBar(titulo = "Directorio de Empresas", onVolver = onVolver) }
    ) { innerPadding ->
        FondoFooter(
            modifier = Modifier.padding(innerPadding)
        ) {
            if (clientes.isEmpty() && !isLoading && query.isBlank()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay empresas registradas.")
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(Dimens.espacioMedio),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {

                    item {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("Buscar empresa por nombre, RUT...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(Dimens.espacioMedio))
                    }


                    items(clientes) { cliente ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(Dimens.espacioMedio)) {
                                Text(cliente.nombre, style = MaterialTheme.typography.titleMedium)
                                Text("RUT: ${Validaciones.formatearRut(cliente.rut)} | Giro: ${cliente.giro ?: "N/A"}")
                                Text("Estado: " + if (cliente.activo) "ACTIVO" else "INACTIVO",
                                    color = if (cliente.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                                    OutlinedButton(
                                        onClick = { onEditarCliente(cliente) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Editar")
                                    }
                                    Button(
                                        onClick = { clienteAEliminar = cliente; errorEliminar = null },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Eliminar")
                                    }
                                }
                            }
                        }
                    }


                    if (isLoading) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    if (clientes.isEmpty() && !isLoading && query.isNotBlank()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No se encontraron empresas con ese criterio.")
                            }
                        }
                    }
                }
            }
        }
    }


    if (clienteAEliminar != null) {
        AlertDialog(
            onDismissRequest = { clienteAEliminar = null; errorEliminar = null },
            title = { Text("¿Eliminar Empresa Cliente?") },
            text = {
                Column {
                    if (errorEliminar == null) {
                        Text("Se eliminará la empresa '${clienteAEliminar?.nombre}'. Esta acción no se puede deshacer.")
                    } else {
                        Text(errorEliminar ?: checkNotNull(operacion.error), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = checkNotNull(clienteAEliminar).id
                        operacion.ejecutar {
                            if (api == null) checkNotNull(dbHelper).eliminarCliente(id)
                            else api.eliminarCliente(id).verificar()
                            cargarClientes(reset = true)
                            clienteAEliminar = null
                            errorEliminar = null
                        }
                    },
                    enabled = errorEliminar == null
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { clienteAEliminar = null; errorEliminar = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}