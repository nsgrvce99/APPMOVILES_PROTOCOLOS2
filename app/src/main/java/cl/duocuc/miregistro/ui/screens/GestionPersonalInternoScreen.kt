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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.utils.Validaciones

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPersonalInternoScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    if (usuarioActual?.rolId != 3L) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Acceso denegado. Solo el Jefe Técnico.", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    // --- ESTADOS PARA PAGINACIÓN ---
    var query by remember { mutableStateOf("") }
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var offset by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 20
    val listState = rememberLazyListState()

    // --- ESTADOS PARA CREAR USUARIO ---
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoRut by remember { mutableStateOf("") }
    var nuevoCorreo by remember { mutableStateOf("") }
    var nuevoRolId by remember { mutableStateOf(1L) }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorRut by remember { mutableStateOf<String?>(null) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    // --- 👇 ESTADOS PARA EDITAR USUARIO ---
    var usuarioAEditar by remember { mutableStateOf<Usuario?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editRut by remember { mutableStateOf("") }
    var editCorreo by remember { mutableStateOf("") }
    var editRolId by remember { mutableStateOf(1L) }

    var editErrorNombre by remember { mutableStateOf<String?>(null) }
    var editErrorRut by remember { mutableStateOf<String?>(null) }
    var editErrorCorreo by remember { mutableStateOf<String?>(null) }
    var editErrorGeneral by remember { mutableStateOf<String?>(null) }

    val rolesDisponibles = listOf(
        Pair(1L, "Técnico de terreno"),
        Pair(2L, "Ingeniero de pruebas"),
        Pair(3L, "Jefe Técnico")
    )

    // --- ESTADOS PARA ELIMINAR ---
    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }
    var errorEliminar by remember { mutableStateOf<String?>(null) }

    // --- LÓGICA DE PAGINACIÓN ---
    fun cargarUsuarios(reset: Boolean = false) {
        if (isLoading) return
        if (reset) {
            offset = 0
            hasMore = true
            usuarios = emptyList()
        }
        if (!hasMore) return

        isLoading = true
        val nuevos = dbHelper.listarPersonalInternoPaginado(query, limit, offset)

        usuarios = usuarios + nuevos
        offset += limit
        hasMore = nuevos.size == limit
        isLoading = false
    }

    LaunchedEffect(query) {
        cargarUsuarios(reset = true)
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= totalItems - 3
        }.collect { shouldLoadMore ->
            if (shouldLoadMore) cargarUsuarios()
        }
    }

    Scaffold(
        topBar = { AppTopBar(titulo = "Gestión de Personal Interno", onVolver = onVolver) },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoCrear = true }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar por nombre, RUT o correo...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(Dimens.espacioMedio))
            }

            items(usuarios) { user ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Dimens.espacioMedio)) {
                        Text(user.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("RUT: ${Validaciones.formatearRut(user.rut)} | Rol: ${user.rolNombre ?: "N/A"}")
                        Text("Correo: ${user.correo}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Estado: " + if (user.activo) "ACTIVO" else "INACTIVO",
                            color = if (user.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        // 👇 FILA CON BOTONES EDITAR Y ELIMINAR
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
                                onClick = { usuarioAEliminar = user; errorEliminar = null },
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
        }
    }

    // --- DIÁLOGO PARA CREAR NUEVO USUARIO INTERNO ---
    if (mostrarDialogoCrear) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoCrear = false
                errorNombre = null; errorRut = null; errorCorreo = null; errorGeneral = null
                nuevoNombre = ""; nuevoRut = ""; nuevoCorreo = ""; nuevoRolId = 1L
            },
            title = { Text("Crear Nuevo Usuario Interno") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (errorGeneral != null) Text(errorGeneral!!, color = MaterialTheme.colorScheme.error)

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
                            dbHelper.existeRut(nuevoRut) -> errorRut = "El RUT ya está registrado"
                            nuevoCorreo.isBlank() -> errorCorreo = "El correo es obligatorio"
                            !Validaciones.esCorreoValido(nuevoCorreo) -> errorCorreo = "Formato de correo inválido"
                            dbHelper.existeCorreo(nuevoCorreo) -> errorCorreo = "El correo ya está registrado"
                            else -> {
                                val nuevoUsuario = Usuario(nombre = nuevoNombre.trim(), rut = nuevoRut.trim(), correo = nuevoCorreo.trim(), contrasena = "Induztek2026", rolId = nuevoRolId, activo = true)
                                dbHelper.insertarUsuario(nuevoUsuario)
                                cargarUsuarios(reset = true)
                                mostrarDialogoCrear = false
                                nuevoNombre = ""; nuevoRut = ""; nuevoCorreo = ""; nuevoRolId = 1L
                            }
                        }
                    }
                ) { Text("Crear Usuario") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoCrear = false
                    errorNombre = null; errorRut = null; errorCorreo = null; errorGeneral = null
                    nuevoNombre = ""; nuevoRut = ""; nuevoCorreo = ""; nuevoRolId = 1L
                }) { Text("Cancelar") }
            }
        )
    }

    // --- 👇 DIÁLOGO PARA EDITAR USUARIO ---
    if (usuarioAEditar != null) {
        AlertDialog(
            onDismissRequest = { usuarioAEditar = null },
            title = { Text("Editar Usuario Interno") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (editErrorGeneral != null) Text(editErrorGeneral!!, color = MaterialTheme.colorScheme.error)

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
                            // Validamos si el RUT cambió y si ya existe en otro usuario
                            editRut != usuarioAEditar!!.rut && dbHelper.existeRut(editRut) -> editErrorRut = "El RUT ya está en uso"

                            editCorreo.isBlank() -> editErrorCorreo = "El correo es obligatorio"
                            !Validaciones.esCorreoValido(editCorreo) -> editErrorCorreo = "Formato de correo inválido"
                            // Validamos si el correo cambió y si ya existe
                            editCorreo != usuarioAEditar!!.correo && dbHelper.existeCorreo(editCorreo) -> editErrorCorreo = "El correo ya está en uso"

                            else -> {
                                dbHelper.actualizarPersonalInterno(usuarioAEditar!!.id, editNombre.trim(), editRut.trim(), editCorreo.trim(), editRolId)
                                cargarUsuarios(reset = true)
                                usuarioAEditar = null
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

    // --- DIÁLOGO PARA ELIMINAR ---
    if (usuarioAEliminar != null) {
        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null; errorEliminar = null },
            title = { Text("¿Eliminar usuario?") },
            text = {
                Column {
                    if (errorEliminar == null) {
                        Text("Se eliminará a '${usuarioAEliminar?.nombre}'. Esta acción no se puede deshacer.")
                    } else {
                        Text(errorEliminar!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            dbHelper.eliminarUsuario(usuarioAEliminar!!.id)
                            cargarUsuarios(reset = true)
                            usuarioAEliminar = null
                        } catch (e: Exception) {
                            errorEliminar = "No se puede eliminar: tiene pruebas o registros asociados."
                        }
                    },
                    enabled = errorEliminar == null
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEliminar = null; errorEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}