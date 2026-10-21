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
        _uiState.update { it.copy(lectura1Min = lectura) }
    }

    fun actualizarLectura10Min(lectura: String) {
        _uiState.update {
            val ip = calcularIP(it.lectura1Min, lectura)
            it.copy(lectura10Min = lectura, resultadoPrueba = ip)
        }
    }

    private fun calcularIP(min1: String, min10: String): String {
        val v1 = min1.toDoubleOrNull()
        val v10 = min10.toDoubleOrNull()
        if (v1 != null && v10 != null && v1 > 0) {
            val ip = v10 / v1
            return when {
                ip >= 2.0 -> "Aprobado"
                ip >= 1.0 -> "Observado"
                else -> "Rechazado"
            }
        }
        return "Pendiente"
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
            resultadoGlobal = state.resultadoPrueba,
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