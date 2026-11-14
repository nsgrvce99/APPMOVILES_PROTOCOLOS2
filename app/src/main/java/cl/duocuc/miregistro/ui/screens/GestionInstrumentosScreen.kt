package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.BotonPrincipal
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.components.FilaInfo
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

    var instrumentos by remember { mutableStateOf(dbHelper.listarInstrumentos()) }


    var nombre by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var serie by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }


    var instrumentoAEditar by remember { mutableStateOf<Instrumento?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editMarca by remember { mutableStateOf("") }
    var editSerie by remember { mutableStateOf("") }
    var editVencimiento by remember { mutableStateOf("") }


    var instrumentoAEliminar by remember { mutableStateOf<Instrumento?>(null) }
    var errorEliminar by remember { mutableStateOf<String?>(null) }

    fun recargar() {
        instrumentos = dbHelper.listarInstrumentos()
    }

    Scaffold(
        topBar = { AppTopBar(titulo = "Gestionar Instrumentos", onVolver = onVolver) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {

            item {
                Text("Registrar Nuevo Instrumento", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Dimens.espacioChico))
                CampoTexto(valor = nombre, onValorCambia = { nombre = it }, etiqueta = "Nombre (ej. Megóhmetro)", icono = R.drawable.ic_persona)
                CampoTexto(valor = marca, onValorCambia = { marca = it }, etiqueta = "Marca", icono = R.drawable.ic_persona)
                CampoTexto(valor = serie, onValorCambia = { serie = it }, etiqueta = "Número de Serie", icono = R.drawable.ic_persona)
                CampoTexto(valor = fechaVencimiento, onValorCambia = { fechaVencimiento = it }, etiqueta = "Vencimiento (dd-MM-yyyy)", icono = R.drawable.ic_persona)

                Spacer(Modifier.height(Dimens.espacioChico))
                BotonPrincipal(
                    texto = "Guardar Instrumento",
                    onClick = {
                        if (nombre.isBlank() || fechaVencimiento.isBlank()) {
                            mensaje = "Nombre y fecha son obligatorios"
                        } else {
                            dbHelper.insertarInstrumento(nombre, marca, serie, fechaVencimiento)
                            recargar()
                            nombre = ""; marca = ""; serie = ""; fechaVencimiento = ""
                            mensaje = "Instrumento guardado exitosamente"
                        }
                    }
                )
                if (mensaje != null) {
                    Text(mensaje!!, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(Modifier.height(Dimens.espacioMedio))
                HorizontalDivider()
                Spacer(Modifier.height(Dimens.espacioMedio))
                Text("Instrumentos Registrados:", style = MaterialTheme.typography.titleMedium)
            }


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
                            Text(
                                "❌ INSTRUMENTO VENCIDO",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else if (porVencer) {
                            Text(
                                "⚠️ PRÓXIMO A VENCER (Menos de 30 días)",
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    instrumentoAEditar = inst
                                    editNombre = inst.nombre
                                    editMarca = inst.marca
                                    editSerie = inst.serie
                                    editVencimiento = inst.fechaVencimiento
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Editar")
                            }
                            Button(
                                onClick = {
                                    instrumentoAEliminar = inst
                                    errorEliminar = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }


    if (instrumentoAEditar != null) {
        AlertDialog(
            onDismissRequest = { instrumentoAEditar = null },
            title = { Text("Editar Instrumento") },
            text = {
                Column {
                    CampoTexto(valor = editNombre, onValorCambia = { editNombre = it }, etiqueta = "Nombre", icono = R.drawable.ic_persona)
                    CampoTexto(valor = editMarca, onValorCambia = { editMarca = it }, etiqueta = "Marca", icono = R.drawable.ic_persona)
                    CampoTexto(valor = editSerie, onValorCambia = { editSerie = it }, etiqueta = "Serie", icono = R.drawable.ic_persona)
                    CampoTexto(valor = editVencimiento, onValorCambia = { editVencimiento = it }, etiqueta = "Vencimiento", icono = R.drawable.ic_persona)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    dbHelper.actualizarInstrumento(instrumentoAEditar!!.id, editNombre, editMarca, editSerie, editVencimiento)
                    recargar()
                    instrumentoAEditar = null
                }) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { instrumentoAEditar = null }) { Text("Cancelar") }
            }
        )
    }


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
                            recargar()
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