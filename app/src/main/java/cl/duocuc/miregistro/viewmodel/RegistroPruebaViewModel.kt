package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Instrumento
import cl.duocuc.miregistro.model.Protocolo
import cl.duocuc.miregistro.model.ProtocoloUiState
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

    init {
        _instrumentos.value = dbHelper.listarInstrumentos()
    }

    // 👇 NUEVO: Cambiar tipo de prueba
    fun onTipoPruebaCambia(tipo: String) {
        _uiState.update {
            it.copy(
                tipoPrueba = tipo,
                lectura1Min = "", // Limpiamos los campos al cambiar de prueba
                lectura10Min = "",
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

    // 👇 NUEVO: Recalcular resultado según el tipo de prueba
    private fun recalcularIP() {
        val state = _uiState.value
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
                    ip >= 1.0 -> "Observado"
                    else -> "Rechazado"
                }
            }
            "TTR" -> {
                val error = Math.abs(v10 - v1) / v1 * 100
                when {
                    error <= 0.5 -> "Aprobado"
                    error <= 1.0 -> "Observado"
                    else -> "Rechazado"
                }
            }
            "CONTACTOS" -> {
                // Aquí v1 es la medida y v10 es el máximo de placa
                when {
                    v1 <= v10 -> "Aprobado"
                    v1 <= v10 * 1.2 -> "Observado"
                    else -> "Rechazado"
                }
            }
            else -> "Pendiente"
        }
        _uiState.update { it.copy(resultadoPrueba = resultado) }
    }

    fun guardarProtocolo(equipoId: Long, tecnicoId: Long) {
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

        // Validar calibración
        try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val fechaVencimiento = sdf.parse(instrumento.fechaVencimiento)
            if (fechaVencimiento != null && fechaVencimiento.before(Date())) {
                _uiState.update { it.copy(errorMedicion = "El instrumento está vencido (RN-11)") }
                return
            }
        } catch (e: Exception) {}

        val fechaActualStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

        val protocolo = Protocolo(
            equipoId = equipoId,
            tecnicoId = tecnicoId,
            fecha = fechaActualStr,
            temperatura = state.temperaturaAmbiente,
            humedad = state.humedadRelativa,
            resultadoGlobal = state.resultadoPrueba,
            estado = "REGISTRADO_TERRENO"
        )

        val nuevoId = dbHelper.guardarProtocolo(protocolo)

        if (nuevoId > 0) {
            dbHelper.guardarMedicion(
                protocoloId = nuevoId,
                tipoPrueba = state.tipoPrueba, // Guardamos el tipo seleccionado
                lectura1 = state.lectura1Min,
                lectura10 = state.lectura10Min,
                ip = state.resultadoPrueba
            )
        }

        _uiState.update { it.copy(guardadoExitoso = true, protocoloGuardadoId = nuevoId) }
    }
}