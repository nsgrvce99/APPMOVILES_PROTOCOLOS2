package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.FilaInfo
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProtocoloScreen(
    protocoloId: Long,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }


    val protocolo = remember(protocoloId) { dbHelper.obtenerProtocoloPorId(protocoloId) }
    val medicion = remember(protocoloId) { dbHelper.obtenerMedicionDeProtocolo(protocoloId) }

    Scaffold(
        topBar = { AppTopBar(titulo = "Detalle del Protocolo", onVolver = onVolver) }
    ) { innerPadding ->
        if (protocolo == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No se encontró el protocolo.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Dimens.espacioMedio)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                Text("Protocolo #${protocolo.id}", style = MaterialTheme.typography.titleLarge)

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Dimens.espacioMedio)) {
                        FilaInfo("Fecha", protocolo.fecha)
                        FilaInfo("Temperatura", "${protocolo.temperatura} °C")
                        FilaInfo("Humedad", "${protocolo.humedad} %")
                        FilaInfo("Resultado Global", protocolo.resultadoGlobal)
                        FilaInfo("Estado", protocolo.estado)
                    }
                }

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

                                    Text("Datos de relé registrados.")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}