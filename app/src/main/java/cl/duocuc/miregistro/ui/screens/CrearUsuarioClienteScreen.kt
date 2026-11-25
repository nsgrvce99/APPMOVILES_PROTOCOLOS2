package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.network.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.utils.Validaciones

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUsuarioClienteScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onEditarCliente: (Usuario) -> Unit,

    onGestionarPersonalInterno: () -> Unit = {}
) {
    val context = LocalContext.current
    val api = LocalApiTerreno.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }
    val operacion = recordarOperacion()

    if (usuarioActual?.rolId != 3L) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Acceso denegado. Solo el Jefe Técnico puede gestionar clientes.",
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    var credenciales by remember { mutableStateOf<Pair<String, String>?>(null) }
    var errorCargaPersonal by remember { mutableStateOf<String?>(null) }
    var errorCargaClientes by remember { mutableStateOf<String?>(null) }
    var expandirPersonalInterno by remember { mutableStateOf(false) }
    var expandirClientes by remember { mutableStateOf(false) }

    // ==================== ESTADOS: PERSONAL INTERNO ====================
    var queryPersonal by remember { mutableStateOf("") }
    var personalInterno by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var isLoadingPersonal by remember { mutableStateOf(false) }
    var offsetPersonal by remember { mutableIntStateOf(0) }
    var hasMorePersonal by remember { mutableStateOf(true) }
    val limitPersonal = 20
    val listStatePersonal = rememberLazyListState()


    var mostrarDialogoCrearPersonal by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoRut by remember { mutableStateOf("") }
    var nuevoCorreo by remember { mutableStateOf("") }
    var nuevoRolId by remember { mutableStateOf(1L) }
    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorRut by remember { mutableStateOf<String?>(null) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }


    var usuarioAEditar by remember { mutableStateOf<Usuario?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editRut by remember { mutableStateOf("") }
    var editCorreo by remember { mutableStateOf("") }
    var editRolId by remember { mutableStateOf(1L) }
    var editErrorNombre by remember { mutableStateOf<String?>(null) }
    var editErrorRut by remember { mutableStateOf<String?>(null) }
    var editErrorCorreo by remember { mutableStateOf<String?>(null) }
    var editErrorGeneral by remember { mutableStateOf<String?>(null) }


    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }
    var errorEliminarPersonal by remember { mutableStateOf<String?>(null) }

    val rolesDisponibles = listOf(
        Pair(1L, "Técnico de terreno"),
        Pair(2L, "Ingeniero de pruebas"),
        Pair(3L, "Jefe Técnico")
    )

    suspend fun cargarPersonal(reset: Boolean = false) {
        if (isLoadingPersonal) return
        if (reset) {
            offsetPersonal = 0
            hasMorePersonal = true
            personalInterno = emptyList()
        }
        if (!hasMorePersonal) return

        isLoadingPersonal = true
        errorCargaPersonal = null
        try {
            val nuevos = if (api == null) checkNotNull(dbHelper).listarPersonalInternoPaginado(queryPersonal, limitPersonal, offsetPersonal)
                else api.personal(queryPersonal, limitPersonal, offsetPersonal).map { it.aUsuario() }
            personalInterno += nuevos
            offsetPersonal += limitPersonal
            hasMorePersonal = nuevos.size == limitPersonal
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            errorCargaPersonal = mensajeRed(error)
        } finally {
            isLoadingPersonal = false
        }
    }

    LaunchedEffect(queryPersonal) {
        if (api != null && queryPersonal.isNotEmpty()) delay(250)
        cargarPersonal(reset = true)
    }

    // ==================== ESTADOS: CLIENTES ====================
    var query by remember { mutableStateOf("") }
    var clientes by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var offset by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 20
    val listState = rememberLazyListState()

    var clienteAEliminar by remember { mutableStateOf<Usuario?>(null) }
    var errorEliminar by remember { mutableStateOf<String?>(null) }

    suspend fun cargarClientes(reset: Boolean = false) {
        if (isLoading) return
        if (reset) {
            offset = 0
            hasMore = true
            clientes = emptyList()
        }
        if (!hasMore) return

        isLoading = true
        errorCargaClientes = null
        try {
            val nuevos = if (api == null) checkNotNull(dbHelper).listarClientesPaginado(query, limit, offset)
                else api.clientes(query, limit, offset).map { it.aUsuario() }
            clientes += nuevos
            offset += limit
            hasMore = nuevos.size == limit
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            errorCargaClientes = mensajeRed(error)
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

    // ==================== UI ====================
    Scaffold(
        topBar = { AppTopBar(titulo = "Gestión de Clientes", onVolver = onVolver) }
    ) { innerPadding ->
        FondoFooter(
            modifier = Modifier.padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                // ==================== ACORDEÓN: PERSONAL INTERNO ====================
                item {
                    AcordeonSeccion(
                        titulo = "Personal Interno",
                        icono = Icons.Default.People,
                        expandido = expandirPersonalInterno,
                        onToggle = { expandirPersonalInterno = !expandirPersonalInterno }
                    ) {
                        Column {
                            OutlinedTextField(
                                value = queryPersonal,
                                onValueChange = { queryPersonal = it },
                                label = { Text("Buscar por nombre, RUT o correo...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(Modifier.height(Dimens.espacioMedio))

                            Button(
                                onClick = { mostrarDialogoCrearPersonal = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("+ Crear Nuevo Usuario Interno")
                            }
                            Spacer(Modifier.height(Dimens.espacioMedio))

                            personalInterno.forEach { user ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = Dimens.espacioChico)
                                ) {
                                    Column(Modifier.padding(Dimens.espacioMedio)) {
                                        Text(user.nombre, style = MaterialTheme.typography.titleMedium)
                                        Text("RUT: ${Validaciones.formatearRut(user.rut)} | Rol: ${user.rolNombre ?: "N/A"}")
                                        Text("Correo: ${user.correo}", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            "Estado: " + if (user.activo) "ACTIVO" else "INACTIVO",
                                            color = if (user.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    usuarioAEditar = user
                                                    editNombre = user.nombre
                                                    editRut = user.rut
                                                    editCorreo = user.correo
                                                    editRolId = user.rolId
                                                    editErrorNombre = null; editErrorRut = null; editErrorCorreo = null; editErrorGeneral = null
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Editar")
                                            }
                                            Button(
                                                onClick = { usuarioAEliminar = user; errorEliminarPersonal = null },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Eliminar")
                                            }
                                        }
                                    }
                                }
                            }

                            errorCargaPersonal?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            if (isLoadingPersonal) {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            if (personalInterno.isEmpty() && !isLoadingPersonal) {
                                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No se encontraron usuarios.")
                                }
                            }
                        }
                    }
                }

                // ==================== ACORDEÓN: CLIENTES ====================
                item {
                    AcordeonSeccion(
                        titulo = "Clientes",
                        icono = Icons.Default.Person,
                        expandido = expandirClientes,
                        onToggle = { expandirClientes = !expandirClientes }
                    ) {
                        Column {
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                label = { Text("Buscar por nombre, RUT o correo...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = Dimens.espacioMedio),
                                singleLine = true
                            )

                            clientes.forEach { cliente ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = Dimens.espacioChico)
                                ) {
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
                                                    operacion.ejecutar {
                                                        if (api == null) {
                                                            checkNotNull(dbHelper).activarCliente(cliente.id, Validaciones.CLAVE_ACTIVACION_CLIENTE)
                                                        } else {
                                                            val clave = Validaciones.CLAVE_ACTIVACION_CLIENTE
                                                            api.activarCliente(cliente.id, ActivacionSolicitud(clave))
                                                            credenciales = cliente.correo to clave
                                                        }
                                                        cargarClientes(reset = true)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                            ) {
                                                Text(if (api == null) "Activar Cuenta (Clave local)" else "Activar cuenta")
                                            }
                                        }
                                    }
                                }
                            }

                            errorCargaClientes?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            if (isLoading) {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            if (operacion.error != null && clienteAEliminar == null) MensajeError(texto = operacion.error)
                            if (clientes.isEmpty() && !isLoading) {
                                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No se encontraron clientes.")
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }

    // ==================== DIÁLOGOS PERSONAL INTERNO ====================

    if (mostrarDialogoCrearPersonal) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoCrearPersonal = false
                errorNombre = null; errorRut = null; errorCorreo = null; errorGeneral = null
                nuevoNombre = ""; nuevoRut = ""; nuevoCorreo = ""; nuevoRolId = 1L
            },
            title = { Text("Crear Nuevo Usuario Interno") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (errorGeneral != null || operacion.error != null) Text(errorGeneral ?: operacion.error.orEmpty(), color = MaterialTheme.colorScheme.error)

                    CampoValidado(valor = nuevoNombre, onValorCambia = { nuevoNombre = it; errorNombre = null; errorGeneral = null }, etiqueta = "Nombre Completo", icono = R.drawable.ic_persona, error = errorNombre)
                    CampoValidado(valor = nuevoRut, onValorCambia = { nuevoRut = it; errorRut = null; errorGeneral = null }, etiqueta = "RUT (sin puntos ni guion)", icono = R.drawable.ic_persona, error = errorRut)
                    CampoValidado(valor = nuevoCorreo, onValorCambia = { nuevoCorreo = it; errorCorreo = null; errorGeneral = null }, etiqueta = "Correo electrónico", icono = R.drawable.ic_persona, tipoTeclado = KeyboardType.Email, error = errorCorreo)

                    Text("Rol del nuevo usuario:", style = MaterialTheme.typography.labelMedium)
                    rolesDisponibles.forEach { (id, nombreRol) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = nuevoRolId == id, onClick = { nuevoRolId = id })
                            Text(nombreRol)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        errorNombre = null; errorRut = null; errorCorreo = null; errorGeneral = null
                        when {
                            nuevoNombre.isBlank() -> errorNombre = "El nombre es obligatorio"
                            !Validaciones.esTextoValido(nuevoNombre) -> errorNombre = "Caracteres inválidos"
                            nuevoRut.isBlank() -> errorRut = "El RUT es obligatorio"
                            !Validaciones.esRutValido(nuevoRut) -> errorRut = "RUT inválido"
                            dbHelper?.existeRut(nuevoRut) == true -> errorRut = "El RUT ya está registrado"
                            nuevoCorreo.isBlank() -> errorCorreo = "El correo es obligatorio"
                            !Validaciones.esCorreoValido(nuevoCorreo) -> errorCorreo = "Formato de correo inválido"
                            dbHelper?.existeCorreo(nuevoCorreo) == true -> errorCorreo = "El correo ya está registrado"
                            else -> {
                                operacion.ejecutar {
                                    if (api != null) {
                                        val creado = api.crearPersonal(PersonalSolicitud(nuevoNombre.trim(), nuevoRut.trim(), nuevoCorreo.trim(), nuevoRolId))
                                        credenciales = creado.usuario.correo to creado.contrasenaTemporal
                                    } else {
                                        val usuario = Usuario(nombre = nuevoNombre.trim(), rut = nuevoRut.trim(),
                                            correo = nuevoCorreo.trim(), contrasena = "Induztek2026", rolId = nuevoRolId, activo = true)
                                        checkNotNull(dbHelper).insertarUsuario(usuario)
                                    }
                                    cargarPersonal(reset = true)
                                    mostrarDialogoCrearPersonal = false
                                    nuevoNombre = ""; nuevoRut = ""; nuevoCorreo = ""; nuevoRolId = 1L
                                }
                            }
                        }
                    }
                ) { Text("Crear Usuario") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoCrearPersonal = false
                    errorNombre = null; errorRut = null; errorCorreo = null; errorGeneral = null
                    nuevoNombre = ""; nuevoRut = ""; nuevoCorreo = ""; nuevoRolId = 1L
                }) { Text("Cancelar") }
            }
        )
    }

    if (usuarioAEditar != null) {
        AlertDialog(
            onDismissRequest = { usuarioAEditar = null },
            title = { Text("Editar Usuario Interno") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (editErrorGeneral != null || operacion.error != null) Text(editErrorGeneral ?: operacion.error.orEmpty(), color = MaterialTheme.colorScheme.error)

                    CampoValidado(valor = editNombre, onValorCambia = { editNombre = it; editErrorNombre = null }, etiqueta = "Nombre Completo", icono = R.drawable.ic_persona, error = editErrorNombre)
                    CampoValidado(valor = editRut, onValorCambia = { editRut = it; editErrorRut = null }, etiqueta = "RUT", icono = R.drawable.ic_persona, error = editErrorRut)
                    CampoValidado(valor = editCorreo, onValorCambia = { editCorreo = it; editErrorCorreo = null }, etiqueta = "Correo electrónico", icono = R.drawable.ic_persona, tipoTeclado = KeyboardType.Email, error = editErrorCorreo)

                    Text("Rol del usuario:", style = MaterialTheme.typography.labelMedium)
                    rolesDisponibles.forEach { (id, nombreRol) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = editRolId == id, onClick = { editRolId = id })
                            Text(nombreRol)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        editErrorNombre = null; editErrorRut = null; editErrorCorreo = null; editErrorGeneral = null
                        when {
                            editNombre.isBlank() -> editErrorNombre = "El nombre es obligatorio"
                            !Validaciones.esTextoValido(editNombre) -> editErrorNombre = "Caracteres inválidos"
                            editRut.isBlank() -> editErrorRut = "El RUT es obligatorio"
                            !Validaciones.esRutValido(editRut) -> editErrorRut = "RUT inválido"
                            editRut != usuarioAEditar!!.rut && dbHelper?.existeRut(editRut) == true -> editErrorRut = "El RUT ya está en uso"
                            editCorreo.isBlank() -> editErrorCorreo = "El correo es obligatorio"
                            !Validaciones.esCorreoValido(editCorreo) -> editErrorCorreo = "Formato de correo inválido"
                            editCorreo != usuarioAEditar!!.correo && dbHelper?.existeCorreo(editCorreo) == true -> editErrorCorreo = "El correo ya está en uso"
                            else -> {
                                val id = checkNotNull(usuarioAEditar).id
                                operacion.ejecutar {
                                    if (api == null) checkNotNull(dbHelper).actualizarPersonalInterno(id, editNombre.trim(), editRut.trim(), editCorreo.trim(), editRolId)
                                    else api.editarPersonal(id, PersonalSolicitud(editNombre.trim(), editRut.trim(), editCorreo.trim(), editRolId))
                                    cargarPersonal(reset = true)
                                    usuarioAEditar = null
                                }
                            }
                        }
                    }
                ) { Text("Guardar Cambios") }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEditar = null }) { Text("Cancelar") }
            }
        )
    }

    if (usuarioAEliminar != null) {
        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null; errorEliminarPersonal = null },
            title = { Text("¿Eliminar usuario?") },
            text = {
                Column {
                    if (errorEliminarPersonal == null && operacion.error == null) {
                        Text("Se eliminará a '${usuarioAEliminar?.nombre}'. Esta acción no se puede deshacer.")
                    } else {
                        Text(errorEliminarPersonal ?: operacion.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = checkNotNull(usuarioAEliminar).id
                        operacion.ejecutar {
                            if (api == null) checkNotNull(dbHelper).eliminarUsuario(id)
                            else api.eliminarPersonal(id).verificar()
                            cargarPersonal(reset = true)
                            usuarioAEliminar = null
                        }
                    },
                    enabled = errorEliminarPersonal == null
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEliminar = null; errorEliminarPersonal = null }) { Text("Cancelar") }
            }
        )
    }

    // ==================== DIÁLOGO ELIMINAR CLIENTE ====================
    if (clienteAEliminar != null) {
        AlertDialog(
            onDismissRequest = { clienteAEliminar = null; errorEliminar = null },
            title = { Text("¿Eliminar cliente?") },
            text = {
                Column {
                    if (errorEliminar == null && operacion.error == null) {
                        Text("Se eliminará la empresa '${clienteAEliminar?.nombre}'. Esta acción no se puede deshacer.")
                    } else {
                        Text(errorEliminar ?: operacion.error.orEmpty(), color = MaterialTheme.colorScheme.error)
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

    credenciales?.let { (correo, clave) ->
        AlertDialog(
            onDismissRequest = { credenciales = null },
            title = { Text("Contraseña temporal") },
            text = { Text("$correo\n$clave\nLa cuenta debe cambiarla al ingresar.") },
            confirmButton = { TextButton(onClick = { credenciales = null }) { Text("Listo") } }
        )
    }
}