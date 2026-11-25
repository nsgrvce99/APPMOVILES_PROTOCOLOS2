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
import cl.duocuc.miregistro.network.*
import cl.duocuc.miregistro.model.ProtocoloDetallado
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.MensajeError
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.CampoMultilinea
import cl.duocuc.miregistro.ui.components.FondoFooter
import cl.duocuc.miregistro.ui.components.TarjetaProtocoloDetalle
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormalizacionScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val api = LocalApiTerreno.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }
    val operacion = recordarOperacion()


    var mostrarDialogoObservar by remember { mutableStateOf(false) }
    var protocoloAObservar by remember { mutableStateOf<ProtocoloDetallado?>(null) }
    var motivoObservacion by remember { mutableStateOf("") }

    var mostrarDialogoFormalizar by remember { mutableStateOf(false) }
    var protocoloAFormalizar by remember { mutableStateOf<ProtocoloDetallado?>(null) }
    var comentarioVariacion by remember { mutableStateOf("") }

    var revision by remember { mutableIntStateOf(0) }
    val esIngeniero = usuarioActual?.rolId in listOf(2L, 3L)
    val carga by cargar(revision, api, esIngeniero) {
        if (!esIngeniero) emptyList()
        else if (api == null) checkNotNull(dbHelper).listarProtocolosPendientesDetallados()
        else todasLasPaginas { api.pendientes(it, 20) }.mapNotNull {
            val detalle = api.protocolo(it.id)
            detalle.takeUnless { it.equipo?.archivado == true || it.cliente?.archivado == true }?.aTarjeta()
        }
    }
    fun recargar() { revision++ }

    Scaffold(
        topBar = { AppTopBar(titulo = "Formalización de Protocolos", onVolver = onVolver) }
    ) { innerPadding ->
        FondoFooter(
            modifier = Modifier.padding(innerPadding)
        ) {
            val esIngeniero = usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L

            if (!esIngeniero) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Acceso denegado. Solo Ingenieros o Jefes Técnicos.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else ContenidoRemoto(carga, { recargar() }) { protocolos ->
                if (protocolos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay protocolos pendientes de formalización.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Dimens.espacioMedio),
                        verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                    ) {
                        if (operacion.error != null && !mostrarDialogoObservar && !mostrarDialogoFormalizar) {
                            item { MensajeError(texto = operacion.error) }
                        }
                        items(protocolos) { proto ->
                            val cargaMedicion by cargar(proto.id, api) {
                                if (api == null) dbHelper?.obtenerMedicionDeProtocolo(proto.id)
                                else api.protocolo(proto.id).mediciones.firstOrNull()?.aMedicion(proto.id)
                            }
                            val medicion = (cargaMedicion as? EstadoCarga.Lista)?.valor
                            TarjetaProtocoloDetalle(
                                protocolo = proto,
                                medicion = medicion,
                                onFormalizar = {
                                    operacion.ejecutar {
                                        val hayVariacion = if (api != null) api.variacion(proto.id).significativa
                                            else dbHelper?.tieneVariacionSignificativa(proto.id) == true
                                        if (hayVariacion) {
                                            protocoloAFormalizar = proto
                                            comentarioVariacion = ""
                                            mostrarDialogoFormalizar = true
                                        } else {
                                            if (api == null) checkNotNull(dbHelper).actualizarEstadoProtocolo(proto.id, "FORMALIZADO")
                                            else api.formalizar(proto.id, ComentarioSolicitud(null))
                                            recargar()
                                        }
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
    }

    if (mostrarDialogoObservar && protocoloAObservar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoObservar = false },
            title = { Text("Observar Protocolo de ${protocoloAObservar?.equipoTag}") },
            text = {
                Column {
                    MensajeError(texto = operacion.error)
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
                        val id = checkNotNull(protocoloAObservar).id
                        operacion.ejecutar {
                            if (api == null) checkNotNull(dbHelper).observarProtocolo(id, motivoObservacion)
                            else api.observar(id, ObservacionSolicitud(motivoObservacion))
                            recargar()
                            mostrarDialogoObservar = false
                        }
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

    if (mostrarDialogoFormalizar && protocoloAFormalizar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoFormalizar = false },
            title = { Text("Variación Significativa Detectada.") },
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
                        val id = checkNotNull(protocoloAFormalizar).id
                        operacion.ejecutar {
                            if (api == null) checkNotNull(dbHelper).formalizarProtocoloConComentario(id, comentarioVariacion)
                            else api.formalizar(id, ComentarioSolicitud(comentarioVariacion))
                            recargar()
                            mostrarDialogoFormalizar = false
                        }
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