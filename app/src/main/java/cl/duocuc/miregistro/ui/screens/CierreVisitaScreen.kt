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
    onActaEntregada: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    val puedeFirmarActa = usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L


    val clienteDueno = remember(protocoloId) { dbHelper.obtenerClienteDeProtocolo(protocoloId) }
    val equipo = remember(protocoloId) { dbHelper.obtenerEquipoDeProtocolo(protocoloId) }
    val medicion = remember(protocoloId) { dbHelper.obtenerMedicionDeProtocolo(protocoloId) }
    val protocolo = remember(protocoloId) { dbHelper.obtenerProtocoloPorId(protocoloId) }

    var exito by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(titulo = "Acta de Terreno", onVolver = onVolver) }
    ) { innerPadding ->
        if (!puedeFirmarActa) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Acceso denegado. Solo Técnicos e Ingenieros pueden entregar el Acta.", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                            }
                        }
                        if (protocolo != null) {
                            FilaInfo("Resultado", protocolo.resultadoGlobal, colorValor = MaterialTheme.colorScheme.primary)
                        }
                    }
                }



                Text(
                    "El Acta se entregará marcada como 'Pendiente de formalización' (RN-14).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (exito) {
                    MensajeExito(
                        titulo = "Acta entregada exitosamente",
                        detalles = listOf(
                            "Empresa: ${clienteDueno?.nombre ?: "N/A"}",
                            "Protocolo: #$protocoloId",
                            "Estado: Pendiente de formalización"
                        )
                    )
                }

                BotonPrincipal(
                    texto = if (exito) "Volver al inicio" else "Confirmar Entrega de Acta",
                    onClick = {
                        if (exito) {
                            onActaEntregada()
                        } else {
                            val fechaHora = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                            dbHelper.guardarRecepcionActa(
                                protocoloId = protocoloId,
                                nombre = clienteDueno?.nombre ?: "Cliente",
                                rut = clienteDueno?.rut ?: "N/A",
                                cargo = "Representante Legal / Jefe de Planta",
                                fechaHora = fechaHora
                            )
                            exito = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}