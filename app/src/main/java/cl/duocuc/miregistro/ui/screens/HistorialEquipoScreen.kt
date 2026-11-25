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
import cl.duocuc.miregistro.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.FilaInfo
import cl.duocuc.miregistro.ui.components.FondoFooter
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.model.compararMediciones

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialEquipoScreen(
    equipoId: Long,
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    api: InduztekApi? = null
) {
    val context = LocalContext.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }


    val equipoCarga by cargar(equipoId, api) {
        if (api != null) api.equipo(equipoId)
        else withContext(Dispatchers.IO) { dbHelper?.buscarEquipoPorId(equipoId) }
    }
    val equipo = (equipoCarga as? EstadoCarga.Lista)?.valor

    val esCliente = usuarioActual?.rolId == 4L


    var protocolos by remember { mutableStateOf<List<Protocolo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var offset by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 10
    val listState = rememberLazyListState()




    var errorHistorial by remember { mutableStateOf<String?>(null) }
    val alcance = rememberCoroutineScope()
    suspend fun cargarHistorial(reset: Boolean = false) {
        if (isLoading) return
        if (reset) {
            offset = 0
            hasMore = true
            protocolos = emptyList()
        }
        if (!hasMore) return

        isLoading = true
        errorHistorial = null
        try {
            if (api != null) {
                val pagina = api.historial(equipoId, offset / limit, limit)
                protocolos += pagina.content.map { it.aProtocolo() }
                hasMore = pagina.page.number + 1 < pagina.page.totalPages
            } else {
                val nuevos = withContext(Dispatchers.IO) {
                    checkNotNull(dbHelper).listarProtocolosParaHistorialPaginado(equipoId, esCliente, limit, offset)
                }
                protocolos += nuevos
                hasMore = nuevos.size == limit
            }
            offset += limit
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            errorHistorial = mensajeRed(error)
        } finally {
            isLoading = false
        }
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

                titulo = "Historial de Equipo: ${equipo?.tag ?: "ID $equipoId"}",
                onVolver = onVolver
            )
        }
    ) { innerPadding ->
        FondoFooter(
            modifier = Modifier.padding(innerPadding)
        ) {
            if (errorHistorial != null && protocolos.isEmpty()) {
                ContenidoRemoto<Unit>(EstadoCarga.Error(checkNotNull(errorHistorial)), {
                    alcance.launch { cargarHistorial(reset = true) }
                }) {}
            } else if (protocolos.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay historial para este equipo.")
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Dimens.espacioMedio),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    items(protocolos) { protocolo ->

                        val indiceActual = protocolos.indexOf(protocolo)
                        val protocoloAnterior = protocolos.getOrNull(indiceActual + 1)
                        val detalleActual by cargar(protocolo.id, api) {
                            if (api != null) {
                                val detalle = api.protocolo(protocolo.id)
                                detalle.mediciones.firstOrNull()?.aMedicion(protocolo.id) to detalle.tecnico?.nombre
                            } else withContext(Dispatchers.IO) {
                                checkNotNull(dbHelper).obtenerMedicionDeProtocolo(protocolo.id) to
                                    dbHelper.buscarPorId(protocolo.tecnicoId)?.nombre
                            }
                        }
                        val detalleAnterior by cargar(protocoloAnterior?.id, api) {
                            protocoloAnterior?.id?.let { id ->
                                if (api != null) api.protocolo(id).mediciones.firstOrNull()?.aMedicion(id)
                                else withContext(Dispatchers.IO) { dbHelper?.obtenerMedicionDeProtocolo(id) }
                            }
                        }
                        val medicion = (detalleActual as? EstadoCarga.Lista)?.valor?.first
                        val medicionAnterior = (detalleAnterior as? EstadoCarga.Lista)?.valor
                        val nombreTecnico = (detalleActual as? EstadoCarga.Lista)?.valor?.second ?: "—"

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

                                if (detalleActual is EstadoCarga.Error) {
                                    Text("Mediciones temporalmente no disponibles", color = MaterialTheme.colorScheme.error)
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

                                    if (detalleAnterior is EstadoCarga.Lista) {
                                        val comparacion = compararMediciones(medicion, medicionAnterior)
                                        if (comparacion != null) {
                                            val porcentaje = "%.1f".format(comparacion.porcentaje)
                                            Spacer(Modifier.height(4.dp))
                                            if (comparacion.significativa) {
                                                Text("⚠ Variación significativa en ${comparacion.etiqueta}: $porcentaje% vs anterior.", color = Color(0xFFD32F2F))
                                            } else {
                                                Text("Variación en ${comparacion.etiqueta} vs anterior: $porcentaje%", color = Color(0xFF388E3C))
                                            }
                                        } else if (medicionAnterior?.tipoPrueba != medicion.tipoPrueba) {
                                            Text("Sin historial previo del mismo tipo para comparar", color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }


                    if (errorHistorial != null) item {
                        TextButton(onClick = { alcance.launch { cargarHistorial() } }) { Text("Reintentar historial") }
                    } else if (hasMore && !isLoading) item {
                        TextButton(onClick = { alcance.launch { cargarHistorial() } }) { Text("Cargar más pruebas") }
                    }
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
}