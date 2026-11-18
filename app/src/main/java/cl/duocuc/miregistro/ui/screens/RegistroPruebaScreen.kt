package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.utils.Validaciones
import cl.duocuc.miregistro.viewmodel.RegistroPruebaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroPruebaScreen(
    equipoId: Long,
    usuarioActual: Usuario?,
    protocoloId: Long? = null,
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
    val tiposPrueba = listOf("AISLACION", "TTR", "CONTACTOS", "RELE", "DEVANADOS")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val instrumentos by viewModel.instrumentos.collectAsStateWithLifecycle()
    val instrumentoSeleccionado by viewModel.instrumentoSeleccionado.collectAsStateWithLifecycle()
    val equipo = remember(equipoId) { dbHelper.buscarEquipoPorId(equipoId) }
    LaunchedEffect(uiState.protocoloGuardadoId) {
        uiState.protocoloGuardadoId?.let { id ->
            onPruebaGuardadaExitosamente(id)
        }
    }
    LaunchedEffect(protocoloId) {
        if (protocoloId != null) {
            viewModel.cargarProtocoloParaEditar(protocoloId)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Protocolo de Equipo: ${equipo?.tag ?: "ID $equipoId"}",
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

            if (uiState.motivoObservacion != null && uiState.motivoObservacion!!.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(Dimens.espacioMedio)) {
                        Text("⚠️ Observación del Ingeniero / Jefe Técnico:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.height(4.dp))
                        Text(text = uiState.motivoObservacion!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            CampoValidado(
                valor = uiState.temperaturaAmbiente,
                onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarTemperatura(it) },
                etiqueta = "Temperatura Ambiente (°C)",
                icono = R.drawable.ic_candado,
                error = uiState.errorTemperatura,
                tipoTeclado = KeyboardType.Number
            )

            CampoValidado(
                valor = uiState.humedadRelativa,
                onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarHumedad(it) },
                etiqueta = "Humedad Relativa (%)",
                icono = R.drawable.ic_candado,
                tipoTeclado = KeyboardType.Number
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
                    CampoValidado(valor = uiState.lectura1Min, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarLectura1Min(it) }, etiqueta = "Lectura 1 min (MΩ)", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
                    CampoValidado(valor = uiState.lectura10Min, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarLectura10Min(it) }, etiqueta = "Lectura 10 min (MΩ)", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
                }
                "TTR" -> {
                    CampoValidado(valor = uiState.lectura1Min, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarLectura1Min(it) }, etiqueta = "Relación Teórica", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
                    CampoValidado(valor = uiState.lectura10Min, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarLectura10Min(it) }, etiqueta = "Relación Medida", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
                }
                "CONTACTOS" -> {
                    CampoValidado(valor = uiState.lectura1Min, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarLectura1Min(it) }, etiqueta = "Resistencia Medida (μΩ)", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
                    CampoValidado(valor = uiState.lectura10Min, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarLectura10Min(it) }, etiqueta = "Máximo de Placa (μΩ)", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
                }
                "RELE" -> {
                    Text("Prueba de Operación de Relé (RN-08b)", style = MaterialTheme.typography.titleMedium)
                    CampoValidado(valor = uiState.corrienteAjuste, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarCorrienteAjuste(it) }, etiqueta = "Corriente de Ajuste (A)", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
                    CampoValidado(valor = uiState.corrienteOperacion, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarCorrienteOperacion(it) }, etiqueta = "Corriente de Operación (A)", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
                    CampoValidado(valor = uiState.tiempoEsperado, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarTiempoEsperado(it) }, etiqueta = "Tiempo Esperado (ms)", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
                    CampoValidado(valor = uiState.tiempoMedido, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarTiempoMedido(it) }, etiqueta = "Tiempo Medido (ms)", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
                }
                "DEVANADOS" -> {
                    Text("Resistencia de Devanados (RN-08c)", style = MaterialTheme.typography.titleMedium)
                    CampoValidado(valor = uiState.lecturaDevanadoA, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarDevanadoA(it) }, etiqueta = "Fase A (mΩ)", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
                    CampoValidado(valor = uiState.lecturaDevanadoB, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarDevanadoB(it) }, etiqueta = "Fase B (mΩ)", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
                    CampoValidado(valor = uiState.lecturaDevanadoC, onValorCambia = { if (Validaciones.esNumero(it)) viewModel.actualizarDevanadoC(it) }, etiqueta = "Fase C (mΩ)", icono = R.drawable.ic_check, tipoTeclado = KeyboardType.Number)
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
                    Text("Resultado: ${uiState.resultadoPrueba}", modifier = Modifier.padding(Dimens.espacioMedio), style = MaterialTheme.typography.titleMedium)
                }
            }

            MensajeError(texto = uiState.errorMedicion)

            Button(
                onClick = {
                    viewModel.guardarProtocolo(
                        equipoId = equipoId,
                        tecnicoId = usuarioActual?.id ?: 1L,
                        esEdicion = protocoloId != null
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (protocoloId != null) "Actualizar Protocolo" else "Guardar Protocolo")
            }
        }
    }
}