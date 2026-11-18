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
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUsuarioClienteScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onEditarCliente: (Usuario) -> Unit,
    onGestionarPersonalInterno: () -> Unit // 👈 NUEVO PARÁMETRO
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    if (usuarioActual?.rolId != 3L) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Acceso denegado. Solo el Jefe Técnico puede gestionar clientes (RN-20).", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    // --- ESTADOS PARA PAGINACIÓN Y BÚSQUEDA ---
    var query by remember { mutableStateOf("") }
    var clientes by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var offset by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 20
    val listState = rememberLazyListState()

    // --- ESTADOS PARA ELIMINACIÓN ---
    var clienteAEliminar by remember { mutableStateOf<Usuario?>(null) }
    var errorEliminar by remember { mutableStateOf<String?>(null) }

    // --- LÓGICA DE PAGINACIÓN ---
    fun cargarClientes(reset: Boolean = false) {
        if (isLoading) return
        if (reset) {
            offset = 0
            hasMore = true
            clientes = emptyList()
        }
        if (!hasMore) return

        isLoading = true
        val nuevos = dbHelper.listarClientesPaginado(query, limit, offset)

        clientes = clientes + nuevos
        offset += limit
        hasMore = nuevos.size == limit
        isLoading = false
    }

    LaunchedEffect(query) {
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
        topBar = { AppTopBar(titulo = "Gestión de Clientes", onVolver = onVolver) }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            // --- TÍTULO, BOTÓN DE PERSONAL INTERNO Y BUSCADOR ---
            item {
                Text("Clientes registrados en el sistema:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Dimens.espacioChico))

                // 👇 NUEVO BOTÓN PARA IR A PERSONAL INTERNO
                Button(
                    onClick = onGestionarPersonalInterno,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gestionar Personal Interno (Técnicos / Jefes)")
                }

                Spacer(Modifier.height(Dimens.espacioMedio))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar por nombre, RUT o correo...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(Dimens.espacioChico))
            }

            // --- LISTA DE CLIENTES ---
            items(clientes) { cliente ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Dimens.espacioMedio)) {
                        Text(cliente.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("RUT: ${cliente.rut} | Correo: ${cliente.correo}")
                        Text(
                            "Estado: " + if (cliente.activo) "ACTIVO" else "INACTIVO",
                            color = if (cliente.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { onEditarCliente(cliente) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Editar")
                            }
                            Button(
                                onClick = {
                                    clienteAEliminar = cliente
                                    errorEliminar = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Eliminar")
                            }
                        }

                        if (!cliente.activo) {
                            Button(
                                onClick = {
                                    dbHelper.activarCliente(cliente.id, "Induztek2026")
                                    cargarClientes(reset = true) // 👈 Recargamos la lista paginada
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Text("Activar Cuenta (Clave: Induztek2026)")
                            }
                        }
                    }
                }
            }

            // --- INDICADOR DE CARGA Y MENSAJE DE LISTA VACÍA ---
            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            if (clientes.isEmpty() && !isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron clientes.")
                    }
                }
            }
        }
    }

    // --- DIÁLOGO PARA ELIMINAR ---
    if (clienteAEliminar != null) {
        AlertDialog(
            onDismissRequest = { clienteAEliminar = null; errorEliminar = null },
            title = { Text("¿Eliminar cliente?") },
            text = {
                Column {
                    if (errorEliminar == null) {
                        Text("Se eliminará la empresa '${clienteAEliminar?.nombre}'. Esta acción no se puede deshacer.")
                    } else {
                        Text(errorEliminar!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            dbHelper.eliminarCliente(clienteAEliminar!!.id)
                            cargarClientes(reset = true) // 👈 Recargamos la lista paginada
                            clienteAEliminar = null
                        } catch (e: Exception) {
                            errorEliminar = e.message ?: "Error al eliminar el cliente."
                        }
                    },
                    enabled = errorEliminar == null
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { clienteAEliminar = null; errorEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}