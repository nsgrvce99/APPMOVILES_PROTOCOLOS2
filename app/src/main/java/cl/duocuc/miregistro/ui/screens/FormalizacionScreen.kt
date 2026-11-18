package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.ProtocoloDetallado
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.CampoMultilinea
import cl.duocuc.miregistro.ui.components.TarjetaProtocoloDetalle
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormalizacionScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var protocolos by remember { mutableStateOf<List<ProtocoloDetallado>>(emptyList()) }

    var mostrarDialogoObservar by remember { mutableStateOf(false) }
    var protocoloAObservar by remember { mutableStateOf<ProtocoloDetallado?>(null) }
    var motivoObservacion by remember { mutableStateOf("") }

    // 👇 ESTADOS PARA EL DIÁLOGO DE FORMALIZAR CON COMENTARIO (RN-09)
    var mostrarDialogoFormalizar by remember { mutableStateOf(false) }
    var protocoloAFormalizar by remember { mutableStateOf<ProtocoloDetallado?>(null) }
    var comentarioVariacion by remember { mutableStateOf("") }

    fun recargar() {
        protocolos = dbHelper.listarProtocolosPendientesDetallados()
    }

    LaunchedEffect(Unit) {
        recargar()
    }

    Scaffold(
        topBar = { AppTopBar(titulo = "Formalización de Protocolos", onVolver = onVolver) }
    ) { innerPadding ->
        val esIngeniero = usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L

        if (!esIngeniero) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Acceso denegado. Solo Ingenieros o Jefes Técnicos.", color = MaterialTheme.colorScheme.error)
            }
        } else {
            if (protocolos.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("No hay protocolos pendientes de formalización.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(Dimens.espacioMedio),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    items(protocolos) { proto -> val medicion = remember(proto.id) { dbHelper.obtenerMedicionDeProtocolo(proto.id) }
                        TarjetaProtocoloDetalle(
                            protocolo = proto,
                            medicion = medicion,
                            onFormalizar = {
                                // 👇 LÓGICA RN-09
                                val hayVariacion = dbHelper.tieneVariacionSignificativa(proto.id)
                                if (hayVariacion) {
                                    protocoloAFormalizar = proto
                                    comentarioVariacion = ""
                                    mostrarDialogoFormalizar = true
                                } else {
                                    dbHelper.actualizarEstadoProtocolo(proto.id, "FORMALIZADO")
                                    recargar()
                                }
                            },
                            onObservar = {
                                protocoloAObservar = proto
                                motivoObservacion = ""
                                mostrarDialogoObservar = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo para Observar
    if (mostrarDialogoObservar && protocoloAObservar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoObservar = false },
            title = { Text("Observar Protocolo de ${protocoloAObservar?.equipoTag}") },
            text = {
                Column {
                    Text("Indique el motivo de la observación:")
                    Spacer(Modifier.height(Dimens.espacioMedio))
                    CampoMultilinea(
                        valor = motivoObservacion,
                        onValorCambia = { motivoObservacion = it },
                        etiqueta = "Motivo",
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dbHelper.observarProtocolo(protocoloAObservar!!.id, motivoObservacion)
                        recargar()
                        mostrarDialogoObservar = false
                    },
                    enabled = motivoObservacion.isNotBlank()
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoObservar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 👇 DIÁLOGO PARA FORMALIZAR CON COMENTARIO (RN-09)
    if (mostrarDialogoFormalizar && protocoloAFormalizar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoFormalizar = false },
            title = { Text("Variación Significativa Detectada (RN-09)") },
            text = {
                Column {
                    Text("Este protocolo tiene una variación mayor al 25% respecto al anterior. Debe dejar un comentario justificando la decisión.")
                    Spacer(Modifier.height(Dimens.espacioMedio))
                    CampoMultilinea(
                        valor = comentarioVariacion,
                        onValorCambia = { comentarioVariacion = it },
                        etiqueta = "Comentario obligatorio",
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 👇 GUARDAMOS EL COMENTARIO EN LA BASE DE DATOS
                        dbHelper.formalizarProtocoloConComentario(protocoloAFormalizar!!.id, comentarioVariacion)
                        recargar()
                        mostrarDialogoFormalizar = false
                    },
                    enabled = comentarioVariacion.isNotBlank()
                ) { Text("Formalizar con Comentario") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoFormalizar = false }) { Text("Cancelar") }
            }
        )
    }
}