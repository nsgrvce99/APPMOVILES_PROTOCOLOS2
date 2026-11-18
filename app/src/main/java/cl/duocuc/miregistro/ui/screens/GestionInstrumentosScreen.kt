package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Instrumento
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionInstrumentosScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    if (usuarioActual?.rolId != 3L) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Acceso denegado. Solo el Jefe Técnico puede gestionar instrumentos (RN-12).", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    // --- ESTADOS PARA PAGINACIÓN Y BÚSQUEDA ---
    var query by remember { mutableStateOf("") }
    var instrumentos by remember { mutableStateOf<List<Instrumento>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var offset by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 20
    val listState = rememberLazyListState()

    // --- ESTADOS PARA CREACIÓN ---
    var nombre by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var serie by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorMarca by remember { mutableStateOf<String?>(null) }
    var errorSerie by remember { mutableStateOf<String?>(null) }
    var errorFecha by remember { mutableStateOf<String?>(null) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }

    // --- ESTADOS PARA EDICIÓN ---
    var instrumentoAEditar by remember { mutableStateOf<Instrumento?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editMarca by remember { mutableStateOf("") }
    var editSerie by remember { mutableStateOf("") }
    var editVencimiento by remember { mutableStateOf("") }

    var editErrorNombre by remember { mutableStateOf<String?>(null) }
    var editErrorMarca by remember { mutableStateOf<String?>(null) }
    var editErrorSerie by remember { mutableStateOf<String?>(null) }
    var editErrorFecha by remember { mutableStateOf<String?>(null) }

    // --- ESTADOS PARA ELIMINACIÓN ---
    var instrumentoAEliminar by remember { mutableStateOf<Instrumento?>(null) }
    var errorEliminar by remember { mutableStateOf<String?>(null) }

    // --- LÓGICA DE PAGINACIÓN ---
    fun cargarInstrumentos(reset: Boolean = false) {
        if (isLoading) return
        if (reset) {
            offset = 0
            hasMore = true
            instrumentos = emptyList()
        }
        if (!hasMore) return

        isLoading = true
        val nuevos = dbHelper.listarInstrumentosPaginado(query, limit, offset)

        instrumentos = instrumentos + nuevos
        offset += limit
        hasMore = nuevos.size == limit
        isLoading = false
    }

    LaunchedEffect(query) {
        cargarInstrumentos(reset = true)
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= totalItems - 3
        }.collect { shouldLoadMore ->
            if (shouldLoadMore) cargarInstrumentos()
        }
    }

    fun validarFecha(fecha: String): Boolean {
        return try {
            val partes = fecha.split("-")
            if (partes.size != 3) return false
            if (partes[0].length != 2 || partes[1].length != 2 || partes[2].length != 4) return false

            val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(fecha) ?: return false

            val calendar = java.util.Calendar.getInstance()
            calendar.time = date
            val anio = calendar.get(java.util.Calendar.YEAR)

            anio in 2000..2100
        } catch (e: Exception) {
            false
        }
    }

    Scaffold(
        topBar = { AppTopBar(titulo = "Gestionar Instrumentos", onVolver = onVolver) }
    ) { innerPadding ->
        LazyColumn(
            state = listState, // 👈 Vinculamos el estado del scroll
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            // --- FORMULARIO DE CREACIÓN ---
            item {
                Text("Registrar Nuevo Instrumento", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Dimens.espacioChico))

                CampoValidado(
                    valor = nombre,
                    onValorCambia = { nombre = it; errorNombre = null; mensajeExito = null },
                    etiqueta = "Nombre (ej. Megóhmetro)",
                    icono = R.drawable.ic_persona,
                    error = errorNombre
                )

                CampoValidado(
                    valor = marca,
                    onValorCambia = { marca = it; errorMarca = null; mensajeExito = null },
                    etiqueta = "Marca",
                    icono = R.drawable.ic_persona,
                    error = errorMarca
                )

                CampoValidado(
                    valor = serie,
                    onValorCambia = { serie = it; errorSerie = null; mensajeExito = null },
                    etiqueta = "Número de Serie",
                    icono = R.drawable.ic_persona,
                    error = errorSerie
                )

                CampoValidado(
                    valor = fechaVencimiento,
                    onValorCambia = { fechaVencimiento = it; errorFecha = null; mensajeExito = null },
                    etiqueta = "Vencimiento (dd-MM-yyyy)",
                    icono = R.drawable.ic_persona,
                    error = errorFecha
                )

                Spacer(Modifier.height(Dimens.espacioChico))

                BotonPrincipal(
                    texto = "Guardar Instrumento",
                    onClick = {
                        errorNombre = null
                        errorMarca = null
                        errorSerie = null
                        errorFecha = null
                        mensajeExito = null

                        when {
                            nombre.isBlank() -> errorNombre = "El nombre es obligatorio"
                            marca.isBlank() -> errorMarca = "La marca es obligatoria"
                            serie.isBlank() -> errorSerie = "El número de serie es obligatorio"
                            fechaVencimiento.isBlank() -> errorFecha = "La fecha es obligatoria"
                            !validarFecha(fechaVencimiento) -> errorFecha = "Formato inválido. Use dd-MM-yyyy"
                            else -> {
                                dbHelper.insertarInstrumento(nombre, marca, serie, fechaVencimiento)
                                cargarInstrumentos(reset = true) // 👈 Recargamos la lista paginada
                                nombre = ""; marca = ""; serie = ""; fechaVencimiento = ""
                                mensajeExito = "Instrumento guardado exitosamente"
                            }
                        }
                    }
                )

                if (mensajeExito != null) {
                    Text(mensajeExito!!, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(Modifier.height(Dimens.espacioMedio))
                HorizontalDivider()
                Spacer(Modifier.height(Dimens.espacioMedio))
                Text("Instrumentos Registrados:", style = MaterialTheme.typography.titleMedium)
            }

            // 👇 BUSCADOR
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar por nombre o marca...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.espacioMedio),
                    singleLine = true
                )
            }

            // --- LISTA DE INSTRUMENTOS (PAGINADA) ---
            items(instrumentos) { inst ->
                val porVencer = dbHelper.estaPorVencer(inst.fechaVencimiento)
                val vencido = dbHelper.estaVencido(inst.fechaVencimiento)

                val colorFondo = when {
                    vencido -> Color(0xFFFFCDD2)
                    porVencer -> Color(0xFFFFE0B2)
                    else -> MaterialTheme.colorScheme.surface
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorFondo)
                ) {
                    Column(Modifier.padding(Dimens.espacioMedio)) {
                        FilaInfo("Nombre", inst.nombre)
                        FilaInfo("Marca", inst.marca)
                        FilaInfo("Serie", inst.serie)
                        FilaInfo("Vence", inst.fechaVencimiento)

                        if (vencido) {
                            Text("❌ INSTRUMENTO VENCIDO", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 4.dp))
                        } else if (porVencer) {
                            Text("⚠️ PRÓXIMO A VENCER (Menos de 30 días)", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 4.dp))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    instrumentoAEditar = inst
                                    editNombre = inst.nombre
                                    editMarca = inst.marca
                                    editSerie = inst.serie
                                    editVencimiento = inst.fechaVencimiento
                                    editErrorNombre = null; editErrorMarca = null; editErrorSerie = null; editErrorFecha = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Editar")
                            }
                            Button(
                                onClick = { instrumentoAEliminar = inst; errorEliminar = null },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }

            // 👇 INDICADOR DE CARGA Y MENSAJE DE LISTA VACÍA
            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            if (instrumentos.isEmpty() && !isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron instrumentos.")
                    }
                }
            }
        }
    }

    // --- DIÁLOGO PARA EDITAR ---
    if (instrumentoAEditar != null) {
        AlertDialog(
            onDismissRequest = { instrumentoAEditar = null },
            title = { Text("Editar Instrumento") },
            text = {
                Column {
                    CampoValidado(
                        valor = editNombre,
                        onValorCambia = { editNombre = it; editErrorNombre = null },
                        etiqueta = "Nombre",
                        icono = R.drawable.ic_persona,
                        error = editErrorNombre
                    )
                    CampoValidado(
                        valor = editMarca,
                        onValorCambia = { editMarca = it; editErrorMarca = null },
                        etiqueta = "Marca",
                        icono = R.drawable.ic_persona,
                        error = editErrorMarca
                    )
                    CampoValidado(
                        valor = editSerie,
                        onValorCambia = { editSerie = it; editErrorSerie = null },
                        etiqueta = "Serie",
                        icono = R.drawable.ic_persona,
                        error = editErrorSerie
                    )
                    CampoValidado(
                        valor = editVencimiento,
                        onValorCambia = { editVencimiento = it; editErrorFecha = null },
                        etiqueta = "Vencimiento (dd-MM-yyyy)",
                        icono = R.drawable.ic_persona,
                        error = editErrorFecha
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editErrorNombre = null
                    editErrorMarca = null
                    editErrorSerie = null
                    editErrorFecha = null

                    when {
                        editNombre.isBlank() -> editErrorNombre = "El nombre es obligatorio"
                        editMarca.isBlank() -> editErrorMarca = "La marca es obligatoria"
                        editSerie.isBlank() -> editErrorSerie = "El número de serie es obligatorio"
                        editVencimiento.isBlank() -> editErrorFecha = "La fecha es obligatoria"
                        !validarFecha(editVencimiento) -> editErrorFecha = "Formato inválido. Use dd-MM-yyyy"
                        else -> {
                            dbHelper.actualizarInstrumento(instrumentoAEditar!!.id, editNombre, editMarca, editSerie, editVencimiento)
                            cargarInstrumentos(reset = true) // 👈 Recargamos la lista paginada
                            instrumentoAEditar = null
                        }
                    }
                }) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { instrumentoAEditar = null }) { Text("Cancelar") }
            }
        )
    }

    // --- DIÁLOGO PARA ELIMINAR ---
    if (instrumentoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { instrumentoAEliminar = null; errorEliminar = null },
            title = { Text("¿Eliminar Instrumento?") },
            text = {
                Column {
                    if (errorEliminar == null) {
                        Text("Se eliminará el instrumento '${instrumentoAEliminar?.nombre}'. Esta acción no se puede deshacer.")
                    } else {
                        Text(errorEliminar!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val tieneUso = dbHelper.instrumentoTieneMediciones(instrumentoAEliminar!!.id)
                        if (tieneUso) {
                            errorEliminar = "No se puede eliminar: este instrumento está siendo usado en pruebas registradas."
                        } else {
                            dbHelper.eliminarInstrumento(instrumentoAEliminar!!.id)
                            cargarInstrumentos(reset = true) // 👈 Recargamos la lista paginada
                            instrumentoAEliminar = null
                            errorEliminar = null
                        }
                    },
                    enabled = errorEliminar == null
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { instrumentoAEliminar = null; errorEliminar = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}