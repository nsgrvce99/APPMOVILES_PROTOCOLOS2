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
import kotlin.math.abs

class RegistroPruebaViewModel(private val dbHelper: UsuarioDbHelper) : ViewModel() {

    private val _uiState = MutableStateFlow(ProtocoloUiState())
    val uiState: StateFlow<ProtocoloUiState> = _uiState.asStateFlow()

    // --- EVENTOS DE UI ---
    fun actualizarTipoPrueba(tipo: String) {
        _uiState.update { it.copy(tipoPrueba = tipo, resultadoPrueba = "Pendiente") }
    }

    fun actualizarTemperatura(temp: String) = _uiState.update { it.copy(temperaturaAmbiente = temp, errorTemperatura = null) }
    fun actualizarHumedad(hum: String) = _uiState.update { it.copy(humedadRelativa = hum) }
    fun actualizarInstrumento(inst: String) = _uiState.update { it.copy(instrumentoSeleccionado = inst, errorMedicion = null) }

    // --- AISLACION ---
    fun actualizarLectura1Min(lectura: String) = _uiState.update {
        val res = calcularIP(lectura, it.lectura10Min)
        it.copy(lectura1Min = lectura, resultadoPrueba = res)
    }
    fun actualizarLectura10Min(lectura: String) = _uiState.update {
        val res = calcularIP(it.lectura1Min, lectura)
        it.copy(lectura10Min = lectura, resultadoPrueba = res)
    }

    // --- TTR (RN-07) ---
    fun actualizarRelacionTeorica(valor: String) = _uiState.update {
        val res = calcularTTR(valor, it.relacionMedida)
        it.copy(relacionTeorica = valor, resultadoPrueba = res)
    }
    fun actualizarRelacionMedida(valor: String) = _uiState.update {
        val res = calcularTTR(it.relacionTeorica, valor)
        it.copy(relacionMedida = valor, resultadoPrueba = res)
    }

    // --- CONTACTOS (RN-08) ---
    fun actualizarValorMedidoContacto(valor: String) = _uiState.update {
        val res = calcularContactos(valor, it.valorPlacaContacto)
        it.copy(valorMedidoContacto = valor, resultadoPrueba = res)
    }
    fun actualizarValorPlacaContacto(valor: String) = _uiState.update {
        val res = calcularContactos(it.valorMedidoContacto, valor)
        it.copy(valorPlacaContacto = valor, resultadoPrueba = res)
    }

    // ==========================================
    // MATEMÁTICAS (Anexo 7)
    // ==========================================

    private fun calcularIP(min1: String, min10: String): String {
        val v1 = min1.toDoubleOrNull() ?: return "Pendiente"
        val v10 = min10.toDoubleOrNull() ?: return "Pendiente"
        if (v1 <= 0) return "Pendiente"

        if (v1 < 100.0) return "Rechazado (RN-06: < 100 MΩ)"

        val ip = v10 / v1
        return when {
            ip >= 2.0 -> "Aprobado (IP: %.2f)".format(ip)
            ip >= 1.0 -> "Observado (IP: %.2f)".format(ip)
            else -> "Rechazado (IP: %.2f)".format(ip)
        }
    }

    private fun calcularTTR(teorica: String, medida: String): String {
        val t = teorica.toDoubleOrNull() ?: return "Pendiente"
        val m = medida.toDoubleOrNull() ?: return "Pendiente"
        if (t <= 0) return "Pendiente"

        val error = (abs(m - t) / t) * 100
        return when {
            error <= 0.5 -> "Aprobado (Error: %.2f%%)".format(error)
            error <= 1.0 -> "Observado (Error: %.2f%%)".format(error)
            else -> "Rechazado (Error: %.2f%%)".format(error)
        }
    }

    private fun calcularContactos(medido: String, placa: String): String {
        val m = medido.toDoubleOrNull() ?: return "Pendiente"
        val p = placa.toDoubleOrNull() ?: return "Pendiente"
        if (p <= 0) return "Pendiente"

        val porcentaje = (m / p) * 100
        return when {
            porcentaje <= 100.0 -> "Aprobado (%.1f%% de placa)".format(porcentaje)
            porcentaje <= 120.0 -> "Observado (%.1f%% de placa)".format(porcentaje)
            else -> "Rechazado (%.1f%% de placa)".format(porcentaje)
        }
    }

    // ==========================================

    fun guardarProtocolo(equipoId: Long, tecnicoId: Long) {
        val state = _uiState.value

        if (state.temperaturaAmbiente.isBlank() || state.humedadRelativa.isBlank()) {
            _uiState.update { it.copy(errorTemperatura = "Temperatura y humedad son obligatorias (RN-04)") }
            return
        }
        if (state.instrumentoSeleccionado.isBlank()) {
            _uiState.update { it.copy(errorMedicion = "Debe seleccionar un instrumento (RF-05)") }
            return
        }
        if (state.resultadoPrueba == "Pendiente") {
            _uiState.update { it.copy(errorMedicion = "Debe completar los valores de la prueba") }
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
            // Guardamos los valores dependiendo del tipo de prueba
            val lectura1 = when(state.tipoPrueba) {
                "TTR" -> state.relacionTeorica
                "CONTACTOS" -> state.valorMedidoContacto
                else -> state.lectura1Min
            }
            val lectura10 = when(state.tipoPrueba) {
                "TTR" -> state.relacionMedida
                "CONTACTOS" -> state.valorPlacaContacto
                else -> state.lectura10Min
            }

            dbHelper.guardarMedicion(
                protocoloId = nuevoId,
                tipoPrueba = state.tipoPrueba,
                lectura1 = lectura1,
                lectura10 = lectura10,
                ip = state.resultadoPrueba
            )
        }

        _uiState.update { it.copy(guardadoExitoso = true, protocoloGuardadoId = nuevoId) }
    }
}