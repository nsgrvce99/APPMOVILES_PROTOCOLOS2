package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.components.MensajeError
import cl.duocuc.miregistro.ui.components.SelectorDesplegable
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.viewmodel.RegistroPruebaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroPruebaScreen(
    equipoId: Long,
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onPruebaGuardadaExitosamente: (Long) -> Unit,
    dbHelper: UsuarioDbHelper = UsuarioDbHelper(LocalContext.current)
) {
    val viewModel: RegistroPruebaViewModel = viewModel(
        key = "reg_$equipoId",
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RegistroPruebaViewModel(dbHelper) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val instrumentos by viewModel.instrumentos.collectAsStateWithLifecycle()
    val instrumentoSeleccionado by viewModel.instrumentoSeleccionado.collectAsStateWithLifecycle()

    val tiposPrueba = listOf("AISLACION", "TTR", "CONTACTOS")

    LaunchedEffect(uiState.protocoloGuardadoId) {
        uiState.protocoloGuardadoId?.let { id ->
            onPruebaGuardadaExitosamente(id)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Protocolo Equipo ID: $equipoId",
                onVolver = onVolver
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioPantalla)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Text("Técnico: ${usuarioActual?.nombre ?: "N/A"}", style = MaterialTheme.typography.titleMedium)

            CampoTexto(
                valor = uiState.temperaturaAmbiente,
                onValorCambia = viewModel::actualizarTemperatura,
                etiqueta = "Temperatura Ambiente (°C)",
                icono = R.drawable.ic_candado,
                error = uiState.errorTemperatura
            )

            CampoTexto(
                valor = uiState.humedadRelativa,
                onValorCambia = viewModel::actualizarHumedad,
                etiqueta = "Humedad Relativa (%)",
                icono = R.drawable.ic_candado
            )


            SelectorDesplegable(
                etiqueta = "Tipo de Prueba",
                opciones = tiposPrueba,
                seleccionada = uiState.tipoPrueba,
                onSeleccionar = { viewModel.onTipoPruebaCambia(it) },
                textoDe = { it }
            )


            when (uiState.tipoPrueba) {
                "AISLACION" -> {
                    CampoTexto(
                        valor = uiState.lectura1Min,
                        onValorCambia = viewModel::actualizarLectura1Min,
                        etiqueta = "Lectura 1 min (MΩ)",
                        icono = R.drawable.ic_check
                    )
                    CampoTexto(
                        valor = uiState.lectura10Min,
                        onValorCambia = viewModel::actualizarLectura10Min,
                        etiqueta = "Lectura 10 min (MΩ)",
                        icono = R.drawable.ic_check
                    )
                }
                "TTR" -> {
                    CampoTexto(
                        valor = uiState.lectura1Min,
                        onValorCambia = viewModel::actualizarLectura1Min,
                        etiqueta = "Relación Teórica",
                        icono = R.drawable.ic_check
                    )
                    CampoTexto(
                        valor = uiState.lectura10Min,
                        onValorCambia = viewModel::actualizarLectura10Min,
                        etiqueta = "Relación Medida",
                        icono = R.drawable.ic_check
                    )
                }
                "CONTACTOS" -> {
                    CampoTexto(
                        valor = uiState.lectura1Min,
                        onValorCambia = viewModel::actualizarLectura1Min,
                        etiqueta = "Resistencia Medida (μΩ)",
                        icono = R.drawable.ic_check
                    )
                    CampoTexto(
                        valor = uiState.lectura10Min,
                        onValorCambia = viewModel::actualizarLectura10Min,
                        etiqueta = "Máximo de Placa (μΩ)",
                        icono = R.drawable.ic_check
                    )
                }
            }


            SelectorDesplegable(
                etiqueta = "Instrumento utilizado",
                opciones = instrumentos,
                seleccionada = instrumentoSeleccionado,
                onSeleccionar = { viewModel.onInstrumentoSeleccionado(it) },
                textoDe = { "${it.nombre} - ${it.marca}" }
            )


            if (uiState.resultadoPrueba != "Pendiente") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Resultado: ${uiState.resultadoPrueba}",
                        modifier = Modifier.padding(Dimens.espacioMedio),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            MensajeError(texto = uiState.errorMedicion)

            Button(
                onClick = {
                    viewModel.guardarProtocolo(equipoId, usuarioActual?.id ?: 1L)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Protocolo")
            }
        }
    }
}