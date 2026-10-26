package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.FilaInfo
import cl.duocuc.miregistro.ui.theme.Dimens
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialEquipoScreen(
    equipoId: Long,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }
    val protocolos = remember(equipoId) { dbHelper.listarProtocolosPorEquipo(equipoId) }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Historial Equipo ID: $equipoId",
                onVolver = onVolver
            )
        }
    ) { innerPadding ->
        if (protocolos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No hay historial para este equipo.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                items(protocolos) { protocolo ->
                    val medicion = dbHelper.obtenerMedicionDeProtocolo(protocolo.id)
                    val indiceActual = protocolos.indexOf(protocolo)
                    val protocoloAnterior = protocolos.getOrNull(indiceActual + 1)
                    val medicionAnterior = protocoloAnterior?.let { dbHelper.obtenerMedicionDeProtocolo(it.id) }

                    val estadoMostrado = if (protocolo.estado == "REGISTRADO_TERRENO" || protocolo.estado == "OBSERVADO") {
                        "Pendiente de formalización"
                    } else {
                        protocolo.estado
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Dimens.espacioMedio)) {

                            FilaInfo("Fecha", protocolo.fecha)
                            FilaInfo("Resultado", protocolo.resultadoGlobal)
                            FilaInfo("Estado", estadoMostrado)
                            FilaInfo("T° / Humedad", "${protocolo.temperatura}°C / ${protocolo.humedad}%")

                            protocolo.motivoObservacion?.let { motivo ->
                                if (motivo.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("📝 Motivo observación: $motivo", color = Color(0xFFED6C02), style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            if (medicion != null) {
                                Spacer(Modifier.height(8.dp))
                                Text("Tipo Prueba: ${medicion.tipoPrueba}", style = MaterialTheme.typography.labelLarge)

                                when (medicion.tipoPrueba) {
                                    "AISLACION" -> {
                                        FilaInfo("Lectura 1min", "${medicion.lectura1Min} MΩ")
                                        FilaInfo("Lectura 10min", "${medicion.lectura10Min} MΩ")
                                    }
                                    "TTR" -> {
                                        FilaInfo("Relación Teórica", medicion.lectura1Min)
                                        FilaInfo("Relación Medida", medicion.lectura10Min)
                                    }
                                    "CONTACTOS" -> {
                                        FilaInfo("Resistencia Medida", "${medicion.lectura1Min} μΩ")
                                        FilaInfo("Máx de Placa", "${medicion.lectura10Min} μΩ")
                                    }
                                }

                                val lecturaActual = medicion.lectura1Min.toDoubleOrNull()
                                val lecturaAnterior = medicionAnterior?.lectura1Min?.toDoubleOrNull()

                                if (medicionAnterior != null && medicionAnterior.tipoPrueba == medicion.tipoPrueba && lecturaActual != null && lecturaAnterior != null && lecturaAnterior > 0) {
                                    val variacion = ((lecturaActual - lecturaAnterior) / lecturaAnterior) * 100
                                    Spacer(Modifier.height(4.dp))
                                    if (abs(variacion) > 25.0) {
                                        Text("⚠ Variación significativa: ${"%.1f".format(variacion)}% vs anterior", color = Color(0xFFD32F2F))
                                    } else {
                                        Text("Variación vs anterior: ${"%.1f".format(variacion)}%", color = Color(0xFF388E3C))
                                    }
                                } else if (medicionAnterior == null || medicionAnterior.tipoPrueba != medicion.tipoPrueba) {
                                    Text("Sin historial previo del mismo tipo para comparar", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}