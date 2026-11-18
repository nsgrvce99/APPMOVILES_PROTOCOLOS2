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
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Protocolo
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.FilaInfo
import cl.duocuc.miregistro.ui.theme.Dimens
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialEquipoScreen(
    equipoId: Long,
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    // 👇 NUEVO: Buscamos el equipo para obtener su TAG
    val equipo = remember(equipoId) { dbHelper.buscarEquipoPorId(equipoId) }

    val esCliente = usuarioActual?.rolId == 4L

    // --- ESTADOS PARA PAGINACIÓN ---
    var protocolos by remember { mutableStateOf<List<Protocolo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var offset by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 10 // Puedes ajustar el límite (10 o 20)
    val listState = rememberLazyListState()



    // --- LÓGICA DE PAGINACIÓN ---
    fun cargarHistorial(reset: Boolean = false) {
        if (isLoading) return
        if (reset) {
            offset = 0
            hasMore = true
            protocolos = emptyList()
        }
        if (!hasMore) return

        isLoading = true
        val nuevos = dbHelper.listarProtocolosParaHistorialPaginado(equipoId, esCliente, limit, offset)

        protocolos = protocolos + nuevos
        offset += limit
        hasMore = nuevos.size == limit
        isLoading = false
    }

    LaunchedEffect(equipoId, esCliente) {
        cargarHistorial(reset = true)
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= totalItems - 3
        }.collect { shouldLoadMore ->
            if (shouldLoadMore) cargarHistorial()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                // 👇 TÍTULO MODIFICADO
                titulo = "Historial de Equipo: ${equipo?.tag ?: "ID $equipoId"}",
                onVolver = onVolver
            )
        }
    ) { innerPadding ->
        if (protocolos.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No hay historial para este equipo.")
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                items(protocolos) { protocolo ->
                    // Buscamos la medición actual
                    val medicion = dbHelper.obtenerMedicionDeProtocolo(protocolo.id)

                    // Buscamos la medición ANTERIOR.
                    // Ojo: Como estamos paginando, el "anterior" podría no estar en la lista actual.
                    // Para solucionar esto de forma limpia, podrías consultar el anterior directo a la BD.
                    // Pero para no complicar el código, usamos el índice actual.
                    val indiceActual = protocolos.indexOf(protocolo)
                    val protocoloAnterior = protocolos.getOrNull(indiceActual + 1)
                    val medicionAnterior = protocoloAnterior?.let { dbHelper.obtenerMedicionDeProtocolo(it.id) }
                    val tecnico = remember(protocolo.tecnicoId) { dbHelper.buscarPorId(protocolo.tecnicoId) }
                    val nombreTecnico = tecnico?.nombre ?: "Desconocido"

                    val estadoMostrado = when (protocolo.estado) {
                        "REGISTRADO_TERRENO", "OBSERVADO", "PRECAUCION" -> "Pendiente de formalización"
                        "FORMALIZADO" -> "Formalizado"
                        "BORRADOR" -> "Borrador"
                        "ANULADO" -> "Anulado"
                        else -> protocolo.estado
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
                            FilaInfo("Fecha", protocolo.fecha)
                            FilaInfo("Técnico", nombreTecnico)
                            FilaInfo("Resultado", protocolo.resultadoGlobal)
                            FilaInfo("Estado", estadoMostrado)
                            FilaInfo("T° / Humedad", "${protocolo.temperatura}°C / ${protocolo.humedad}%")

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

                                var lecturaActual: Double? = null
                                var lecturaAnterior: Double? = null
                                var etiquetaComparacion = ""

                                when (medicion.tipoPrueba) {
                                    "AISLACION", "TTR", "CONTACTOS" -> {
                                        lecturaActual = medicion.lectura1Min.toDoubleOrNull()
                                        lecturaAnterior = medicionAnterior?.lectura1Min?.toDoubleOrNull()
                                        etiquetaComparacion = "Lectura 1min"
                                    }
                                    "RELE" -> {
                                        lecturaActual = medicion.tiempoMedido.toDoubleOrNull()
                                        lecturaAnterior = medicionAnterior?.tiempoMedido?.toDoubleOrNull()
                                        etiquetaComparacion = "Tiempo Medido"
                                    }
                                    "DEVANADOS" -> {
                                        lecturaActual = medicion.devanadoA.toDoubleOrNull()
                                        lecturaAnterior = medicionAnterior?.devanadoA?.toDoubleOrNull()
                                        etiquetaComparacion = "Fase A"
                                    }
                                }

                                if (medicionAnterior != null && medicionAnterior.tipoPrueba == medicion.tipoPrueba && lecturaActual != null && lecturaAnterior != null && lecturaAnterior > 0) {
                                    val variacion = ((lecturaActual - lecturaAnterior) / lecturaAnterior) * 100
                                    Spacer(Modifier.height(4.dp))
                                    if (abs(variacion) > 25.0) {
                                        Text("⚠ Variación significativa en $etiquetaComparacion: ${"%.1f".format(variacion)}% vs anterior (RN-09)", color = Color(0xFFD32F2F))
                                    } else {
                                        Text("Variación en $etiquetaComparacion vs anterior: ${"%.1f".format(variacion)}%", color = Color(0xFF388E3C))
                                    }
                                } else if (medicionAnterior == null || medicionAnterior.tipoPrueba != medicion.tipoPrueba) {
                                    Text("Sin historial previo del mismo tipo para comparar", color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // --- INDICADOR DE CARGA AL FINAL ---
                if (isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}