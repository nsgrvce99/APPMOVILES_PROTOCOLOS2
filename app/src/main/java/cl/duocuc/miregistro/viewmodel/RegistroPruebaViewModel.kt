package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroPruebaViewModel(private val dbHelper: UsuarioDbHelper) : ViewModel() {

    private val _uiState = MutableStateFlow(ProtocoloUiState())
    val uiState: StateFlow<ProtocoloUiState> = _uiState.asStateFlow()

    private val _instrumentos = MutableStateFlow<List<Instrumento>>(emptyList())
    val instrumentos: StateFlow<List<Instrumento>> = _instrumentos.asStateFlow()

    private val _instrumentoSeleccionado = MutableStateFlow<Instrumento?>(null)
    val instrumentoSeleccionado: StateFlow<Instrumento?> = _instrumentoSeleccionado.asStateFlow()

    private var protocoloIdEditando: Long? = null

    init {
        _instrumentos.value = dbHelper.listarInstrumentos()
    }

    fun cargarProtocoloParaEditar(protocoloId: Long) {
        val protocolo = dbHelper.obtenerProtocoloPorId(protocoloId) ?: return
        protocoloIdEditando = protocoloId

        val medicion = dbHelper.obtenerMedicionDeProtocolo(protocoloId)

        val instrumentoPrevio = medicion?.let { med ->
            _instrumentos.value.find { it.id == med.instrumentoId }
        }
        _instrumentoSeleccionado.value = instrumentoPrevio

        _uiState.update {
            it.copy(
                temperaturaAmbiente = protocolo.temperatura,
                humedadRelativa = protocolo.humedad,
                tipoPrueba = medicion?.tipoPrueba ?: "AISLACION",
                lectura1Min = medicion?.lectura1Min ?: "",
                lectura10Min = medicion?.lectura10Min ?: "",
                resultadoPrueba = protocolo.resultadoGlobal,
                motivoObservacion = protocolo.motivoObservacion,
                guardadoExitoso = false
            )
        }
    }

    fun onTipoPruebaCambia(tipo: String) {
        _uiState.update {
            it.copy(
                tipoPrueba = tipo,
                lectura1Min = "",
                lectura10Min = "",
                corrienteAjuste = "",
                corrienteOperacion = "",
                tiempoEsperado = "",
                tiempoMedido = "",
                resultadoPrueba = "Pendiente",
                errorMedicion = null
            )
        }
    }

    fun onInstrumentoSeleccionado(instrumento: Instrumento) {
        _instrumentoSeleccionado.value = instrumento
        _uiState.update { it.copy(errorMedicion = null) }
    }

    fun actualizarTemperatura(temp: String) {
        _uiState.update { it.copy(temperaturaAmbiente = temp, errorTemperatura = null) }
    }

    fun actualizarHumedad(humedad: String) {
        _uiState.update { it.copy(humedadRelativa = humedad) }
    }

    fun actualizarLectura1Min(lectura: String) {
        _uiState.update { it.copy(lectura1Min = lectura) }
        recalcularIP()
    }

    fun actualizarLectura10Min(lectura: String) {
        _uiState.update { it.copy(lectura10Min = lectura) }
        recalcularIP()
    }

    fun actualizarCorrienteAjuste(valor: String) {
        _uiState.update { it.copy(corrienteAjuste = valor) }
        recalcularIP()
    }

    fun actualizarCorrienteOperacion(valor: String) {
        _uiState.update { it.copy(corrienteOperacion = valor) }
        recalcularIP()
    }

    fun actualizarTiempoEsperado(valor: String) {
        _uiState.update { it.copy(tiempoEsperado = valor) }
        recalcularIP()
    }

    fun actualizarTiempoMedido(valor: String) {
        _uiState.update { it.copy(tiempoMedido = valor) }
        recalcularIP()
    }

    private fun recalcularIP() {
        val state = _uiState.value

        if (state.tipoPrueba == "RELE") {
            val tEsperado = state.tiempoEsperado.toDoubleOrNull()
            val tMedido = state.tiempoMedido.toDoubleOrNull()

            if (tEsperado == null || tMedido == null || tEsperado == 0.0) {
                _uiState.update { it.copy(resultadoPrueba = "Pendiente") }
                return
            }

            val diferencia = Math.abs(tMedido - tEsperado) / tEsperado * 100
            val resultado = when {
                diferencia <= 5.0 -> "Aprobado"
                diferencia <= 10.0 -> "Precaucion"
                else -> "Rechazado"
            }
            _uiState.update { it.copy(resultadoPrueba = resultado) }
            return
        }

        val v1 = state.lectura1Min.toDoubleOrNull()
        val v10 = state.lectura10Min.toDoubleOrNull()

        if (v1 == null || v10 == null || v1 == 0.0) {
            _uiState.update { it.copy(resultadoPrueba = "Pendiente") }
            return
        }

        val resultado = when (state.tipoPrueba) {
            "AISLACION" -> {
                val ip = v10 / v1
                when {
                    ip >= 2.0 -> "Aprobado"
                    ip >= 1.0 -> "Precaucion"
                    else -> "Rechazado"
                }
            }
            "TTR" -> {
                val error = Math.abs(v10 - v1) / v1 * 100
                when {
                    error <= 0.5 -> "Aprobado"
                    error <= 1.0 -> "Precaucion"
                    else -> "Rechazado"
                }
            }
            "CONTACTOS" -> {
                when {
                    v1 <= v10 -> "Aprobado"
                    v1 <= v10 * 1.2 -> "Precaucion"
                    else -> "Rechazado"
                }
            }
            else -> "Pendiente"
        }
        _uiState.update { it.copy(resultadoPrueba = resultado) }
    }


    fun guardarProtocolo(equipoId: Long, tecnicoId: Long, esEdicion: Boolean = false) {
        val state = _uiState.value
        val instrumento = _instrumentoSeleccionado.value

        if (state.temperaturaAmbiente.isBlank() || state.humedadRelativa.isBlank()) {
            _uiState.update { it.copy(errorTemperatura = "Temperatura y humedad son obligatorias (RN-04)") }
            return
        }

        if (instrumento == null) {
            _uiState.update { it.copy(errorMedicion = "Debe seleccionar un instrumento (RF-05)") }
            return
        }

        try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val fechaVencimiento = sdf.parse(instrumento.fechaVencimiento)
            if (fechaVencimiento != null && fechaVencimiento.before(Date())) {
                _uiState.update { it.copy(errorMedicion = "El instrumento está vencido (RN-11)") }
                return
            }
        } catch (e: Exception) {}

        val fechaActualStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

        if (esEdicion && protocoloIdEditando != null) {

            dbHelper.actualizarProtocolo(protocoloIdEditando!!, state.temperaturaAmbiente, state.humedadRelativa, state.resultadoPrueba)
            _uiState.update { it.copy(guardadoExitoso = true, protocoloGuardadoId = protocoloIdEditando) }
        } else {

            val protocolo = Protocolo(
                equipoId = equipoId,
                tecnicoId = tecnicoId,
                fecha = fechaActualStr,
                temperatura = state.temperaturaAmbiente,
                humedad = state.humedadRelativa,
                resultadoGlobal = state.resultadoPrueba,
                estado = "BORRADOR"
            )
            val nuevoId = dbHelper.guardarProtocolo(protocolo)
            if (nuevoId > 0) {
                dbHelper.guardarMedicion(
                    protocoloId = nuevoId,
                    tipoPrueba = state.tipoPrueba,
                    lectura1 = state.lectura1Min,
                    lectura10 = state.lectura10Min,
                    ip = state.resultadoPrueba,
                    instrumentoId = instrumento.id
                )
            }
            _uiState.update { it.copy(guardadoExitoso = true, protocoloGuardadoId = nuevoId) }
        }
    }
}