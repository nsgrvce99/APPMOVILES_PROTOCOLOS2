package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.network.*
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CierreVisitaScreen(
    protocoloId: Long,
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onEditarProtocolo: () -> Unit,
    onActaEntregada: () -> Unit
) {
    val context = LocalContext.current
    val api = LocalApiTerreno.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }
    val operacion = recordarOperacion()

    val puedeFirmarActa = usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L

    var revision by remember { mutableIntStateOf(0) }
    val carga by cargar(protocoloId, puedeFirmarActa, api, revision) {
        if (api != null && puedeFirmarActa) {
            val detalle = api.protocolo(protocoloId)
            val equipo = api.equipo(detalle.protocolo.equipoId)
            val cliente = detalle.cliente?.let { api.usuario(it.id).aUsuario() }
            DatosActa(cliente, equipo, detalle.mediciones.firstOrNull()?.aMedicion(protocoloId), detalle.protocolo.aProtocolo(),
                detalle.acta != null)
        } else DatosActa(dbHelper?.obtenerClienteDeProtocolo(protocoloId), dbHelper?.obtenerEquipoDeProtocolo(protocoloId),
            dbHelper?.obtenerMedicionDeProtocolo(protocoloId), dbHelper?.obtenerProtocoloPorId(protocoloId), false)
    }
    var exito by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(titulo = "Acta de Terreno", onVolver = onVolver) }
    ) { innerPadding ->
        FondoFooter(
            modifier = Modifier.padding(innerPadding)
        ) {
            if (!puedeFirmarActa) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Acceso denegado. Solo Técnicos e Ingenieros pueden entregar el Acta.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else ContenidoRemoto(carga, { revision++ }) { datos ->
                val (clienteDueno, equipo, medicion, protocolo) = datos
                val actaLista = exito || datos.entregada
                val puedeConfirmar = clienteDueno != null && protocolo != null && medicion != null
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.espacioPantalla)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    if (clienteDueno != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(Dimens.espacioMedio)) {
                                Text("Empresa Cliente:", style = MaterialTheme.typography.labelMedium)
                                Text(clienteDueno.nombre, style = MaterialTheme.typography.titleLarge)
                                Text("RUT: ${clienteDueno.rut}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No se puede generar el Acta: no se encontró la empresa cliente asociada al protocolo.",
                                modifier = Modifier.padding(Dimens.espacioMedio),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Text("Resumen del Acta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilaInfo("N° Protocolo", "#$protocoloId")
                            if (equipo != null) {
                                FilaInfo("Equipo", "${equipo.tipoEquipo} ${equipo.tag}")
                                FilaInfo("Instalación", equipo.instalacion)
                            }
                            if (protocolo != null) {
                                FilaInfo("Fecha de Prueba", protocolo.fecha)
                                FilaInfo("T° / Humedad", "${protocolo.temperatura}°C / ${protocolo.humedad}%")
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            if (medicion != null) {
                                FilaInfo("Tipo de Prueba", medicion.tipoPrueba)
                                when (medicion.tipoPrueba) {
                                    "AISLACION" -> {
                                        FilaInfo("Lectura 1 min", "${medicion.lectura1Min} MΩ")
                                        FilaInfo("Lectura 10 min", "${medicion.lectura10Min} MΩ")
                                    }
                                    "TTR" -> {
                                        FilaInfo("Relación Teórica", medicion.lectura1Min)
                                        FilaInfo("Relación Medida", medicion.lectura10Min)
                                    }
                                    "CONTACTOS" -> {
                                        FilaInfo("Resistencia Medida", "${medicion.lectura1Min} μΩ")
                                        FilaInfo("Máx Placa", "${medicion.lectura10Min} μΩ")
                                    }
                                    "RELE" -> {
                                        FilaInfo("Corriente de Ajuste", "${medicion.corrienteAjuste} A")
                                        FilaInfo("Corriente de Operación", "${medicion.corrienteOperacion} A")
                                        FilaInfo("Tiempo Esperado", "${medicion.tiempoEsperado} ms")
                                        FilaInfo("Tiempo Medido", "${medicion.tiempoMedido} ms")
                                    }
                                    "DEVANADOS" -> {
                                        FilaInfo("Fase A", "${medicion.devanadoA} mΩ")
                                        FilaInfo("Fase B", "${medicion.devanadoB} mΩ")
                                        FilaInfo("Fase C", "${medicion.devanadoC} mΩ")
                                    }
                                }
                            }
                            if (protocolo != null) {
                                FilaInfo("Resultado", protocolo.resultadoGlobal, colorValor = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Text(
                        "El Acta se entregará marcada como 'Pendiente de formalización'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    if (actaLista) {
                        MensajeExito(
                            titulo = "Acta entregada exitosamente",
                            detalles = listOf(
                                "Empresa: ${clienteDueno?.nombre ?: "N/A"}",
                                "Protocolo: #$protocoloId",
                                "Estado: Pendiente de formalización"
                            )
                        )
                    }

                    if (protocolo?.tecnicoId == usuarioActual?.id &&
                        protocolo?.estado in listOf("BORRADOR", "REGISTRADO_TERRENO", "OBSERVADO", "PRECAUCION")) {
                        OutlinedButton(
                            onClick = onEditarProtocolo,
                            enabled = !operacion.ejecutando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Editar protocolo")
                        }
                    }

                    MensajeError(texto = operacion.error)
                    BotonPrincipal(
                        texto = if (actaLista) "Volver al inicio" else "Confirmar Entrega de Acta",
                        onClick = {
                            if (actaLista) {
                                onActaEntregada()
                            } else if (puedeConfirmar && api != null) {
                                operacion.ejecutar {
                                    api.confirmarEntregaActa(protocoloId)
                                    exito = true
                                }
                            } else if (puedeConfirmar) {
                                val fechaHora = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                                checkNotNull(dbHelper).guardarRecepcionActa(
                                    protocoloId = protocoloId,
                                    nombre = clienteDueno?.nombre ?: "Cliente",
                                    rut = clienteDueno?.rut ?: "N/A",
                                    cargo = "Representante Legal / Jefe de Planta",
                                    fechaHora = fechaHora
                                )
                                exito = true
                            }
                        },
                        habilitado = (puedeConfirmar || actaLista) && !operacion.ejecutando,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
private data class DatosActa(
    val cliente: Usuario?,
    val equipo: cl.duocuc.miregistro.model.Equipo?,
    val medicion: cl.duocuc.miregistro.model.Medicion?,
    val protocolo: cl.duocuc.miregistro.model.Protocolo?,
    val entregada: Boolean
)