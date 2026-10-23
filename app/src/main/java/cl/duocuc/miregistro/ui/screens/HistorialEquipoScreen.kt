package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.data.UsuarioDbHelper
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
            TopAppBar(
                title = { Text("Historial Equipo ID: $equipoId") },
                navigationIcon = { IconButton(onClick = onVolver) { Text("←") } }
            )
        }
    ) { innerPadding ->
        if (protocolos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
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

                    // ✅ Estado amigable para el cliente (RN-19)
                    val estadoMostrado = if (protocolo.estado == "REGISTRADO_TERRENO" || protocolo.estado == "OBSERVADO") {
                        "Pendiente de formalización"
                    } else {
                        protocolo.estado
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
                            Text("Fecha: ${protocolo.fecha}", style = MaterialTheme.typography.titleMedium)
                            Text("Resultado: ${protocolo.resultadoGlobal}")
                            Text("Estado: $estadoMostrado")
                            Text("T°: ${protocolo.temperatura}°C | Humedad: ${protocolo.humedad}%")

                            // 👇 👇 👇 AQUÍ VA EL BLOQUE DEL MOTIVO 👇 👇 👇
                            protocolo.motivoObservacion?.let { motivo ->
                                if (motivo.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "📝 Motivo observación: $motivo",
                                        color = Color(0xFFED6C02),  // Naranja de advertencia
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            if (medicion != null) {
                                Spacer(Modifier.height(8.dp))
                                Text("Aislación 1min: ${medicion.lectura1Min} MΩ")
                                Text("Aislación 10min: ${medicion.lectura10Min} MΩ")

                                val lecturaActual = medicion.lectura1Min.toDoubleOrNull()
                                val lecturaAnterior = medicionAnterior?.lectura1Min?.toDoubleOrNull()

                                if (lecturaActual != null && lecturaAnterior != null && lecturaAnterior > 0) {
                                    val variacion = ((lecturaActual - lecturaAnterior) / lecturaAnterior) * 100
                                    Spacer(Modifier.height(4.dp))
                                    if (abs(variacion) > 25.0) {
                                        Text(
                                            "⚠ Variación significativa: ${"%.1f".format(variacion)}% vs anterior (RN-09)",
                                            color = Color(0xFFD32F2F)
                                        )
                                    } else {
                                        Text(
                                            "Variación vs anterior: ${"%.1f".format(variacion)}%",
                                            color = Color(0xFF388E3C)
                                        )
                                    }
                                } else if (medicionAnterior == null) {
                                    Text("Sin historial previo para comparar", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}