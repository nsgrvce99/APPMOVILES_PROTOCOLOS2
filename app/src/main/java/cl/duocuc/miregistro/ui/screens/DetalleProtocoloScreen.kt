package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.network.*
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.FilaInfo
import cl.duocuc.miregistro.ui.components.FondoFooter
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProtocoloScreen(
    protocoloId: Long,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val api = LocalApiTerreno.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }


    var revision by remember { mutableIntStateOf(0) }
    val carga by cargar(protocoloId, revision, api) {
        if (api != null) api.protocolo(protocoloId).let {
            it.protocolo.aProtocolo() to it.mediciones.firstOrNull()?.aMedicion(protocoloId)
        } else dbHelper?.obtenerProtocoloPorId(protocoloId) to dbHelper?.obtenerMedicionDeProtocolo(protocoloId)
    }

    Scaffold(
        topBar = { AppTopBar(titulo = "Detalle del Protocolo", onVolver = onVolver) }
    ) { innerPadding ->
        FondoFooter(
            modifier = Modifier.padding(innerPadding)
        ) {
            ContenidoRemoto(carga, { revision++ }) { (protocolo, medicion) ->
            if (protocolo == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontró el protocolo.")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.espacioMedio)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    Text("Protocolo Número ${protocolo.id}", style = MaterialTheme.typography.titleLarge)

                    // --- CARD DE DATOS GENERALES ---
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimens.espacioMedio)) {
                            FilaInfo("Fecha", protocolo.fecha)
                            FilaInfo("Temperatura", "${protocolo.temperatura} °C")
                            FilaInfo("Humedad", "${protocolo.humedad} %")
                            FilaInfo("Resultado Global", protocolo.resultadoGlobal)
                            FilaInfo("Estado", protocolo.estado)
                        }
                    }

                    // --- CARD DE MEDICIONES ---
                    if (medicion != null) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(Dimens.espacioMedio)) {
                                Text("Mediciones", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(Dimens.espacioChico))
                                FilaInfo("Tipo de Prueba", medicion.tipoPrueba)

                                when (medicion.tipoPrueba) {
                                    "AISLACION" -> {
                                        FilaInfo("Lectura 1 min", "${medicion.lectura1Min} MΩ")
                                        FilaInfo("Lectura 10 min", "${medicion.lectura10Min} MΩ")
                                        FilaInfo("IP calculado", medicion.indicePolarizacion)
                                    }
                                    "TTR" -> {
                                        FilaInfo("Relación Teórica", medicion.lectura1Min)
                                        FilaInfo("Relación Medida", medicion.lectura10Min)
                                    }
                                    "CONTACTOS" -> {
                                        FilaInfo("Resistencia Medida", "${medicion.lectura1Min} μΩ")
                                        FilaInfo("Máximo de Placa", "${medicion.lectura10Min} μΩ")
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
                        }
                    }

                    if (protocolo.comentarioVariacion != null && protocolo.comentarioVariacion!!.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(Dimens.espacioMedio)) {
                                Text(
                                    "📝 Comentario de Variación.",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = protocolo.comentarioVariacion!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}