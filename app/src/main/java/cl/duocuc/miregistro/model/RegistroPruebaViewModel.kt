package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.miregistro.data.UsuarioDbHelper
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

    fun actualizarTemperatura(temp: String) {
        _uiState.update { it.copy(temperaturaAmbiente = temp, errorTemperatura = null) }
    }

    fun actualizarHumedad(humedad: String) {
        _uiState.update { it.copy(humedadRelativa = humedad) }
    }

    fun actualizarLectura1Min(lectura: String) {
        _uiState.update {
            val ip = calcularIP(lectura, it.lectura10Min)
            it.copy(lectura1Min = lectura, resultadoPrueba = ip)
        }
    }

    fun actualizarLectura10Min(lectura: String) {
        _uiState.update {
            val ip = calcularIP(it.lectura1Min, lectura)
            it.copy(lectura10Min = lectura, resultadoPrueba = ip)
        }
    }

    /**
     * Cálculo del IP y resultado de la prueba según RN-06.
     * Regla 1: si lectura a 1 min < 100 MΩ → Rechazado (sin importar el IP).
     * Regla 2: si lectura a 1 min ≥ 100 MΩ → aplicar criterio del IP.
     */
    private fun calcularIP(min1: String, min10: String): String {
        val v1 = min1.toDoubleOrNull()
        val v10 = min10.toDoubleOrNull()

        // Si falta alguna lectura, no se puede calcular
        if (v1 == null || v10 == null || v1 <= 0) {
            return "Pendiente"
        }

        // ⚠️ REGLA ADICIONAL DE RN-06: lectura 1min < 100 MΩ → Rechazado
        if (v1 < 100.0) {
            return "Rechazado (RN-06: lectura 1min < 100 MΩ)"
        }

        // Cálculo normal del IP
        val ip = v10 / v1
        return when {
            ip >= 2.0 -> "Aprobado (IP: %.2f)".format(ip)
            ip >= 1.0 -> "Observado (IP: %.2f)".format(ip)
            else -> "Rechazado (IP: %.2f)".format(ip)
        }
    }

    fun guardarProtocolo(equipoId: Long, tecnicoId: Long) {
        val state = _uiState.value
        if (state.temperaturaAmbiente.isBlank() || state.humedadRelativa.isBlank()) {
            _uiState.update { it.copy(errorTemperatura = "Temperatura y humedad son obligatorias (RN-04)") }
            return
        }

        val fechaActual = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

        val protocolo = Protocolo(
            equipoId = equipoId,
            tecnicoId = tecnicoId,
            fecha = fechaActual,
            temperatura = state.temperaturaAmbiente,
            humedad = state.humedadRelativa,
            resultadoGlobal = state.resultadoPrueba,  // En el MVP solo hay 1 prueba
            estado = "REGISTRADO_TERRENO"
        )

        val nuevoId = dbHelper.guardarProtocolo(protocolo)

        if (nuevoId > 0) {
            dbHelper.guardarMedicion(
                protocoloId = nuevoId,
                tipoPrueba = "AISLACION",
                lectura1 = state.lectura1Min,
                lectura10 = state.lectura10Min,
                ip = state.resultadoPrueba
            )
        }

        _uiState.update { it.copy(guardadoExitoso = true, protocoloGuardadoId = nuevoId) }
    }
}