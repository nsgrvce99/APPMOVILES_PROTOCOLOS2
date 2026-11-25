package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.miregistro.network.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroPruebaViewModel(
    private val dbHelper: UsuarioDbHelper?,
    private val api: InduztekApi? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProtocoloUiState())
    val uiState: StateFlow<ProtocoloUiState> = _uiState.asStateFlow()

    private val _instrumentos = MutableStateFlow<List<Instrumento>>(emptyList())
    val instrumentos: StateFlow<List<Instrumento>> = _instrumentos.asStateFlow()

    private val _instrumentoSeleccionado = MutableStateFlow<Instrumento?>(null)
    val instrumentoSeleccionado: StateFlow<Instrumento?> = _instrumentoSeleccionado.asStateFlow()

    private var protocoloIdEditando: Long? = null

    init { cargarInstrumentos() }

    fun cargarInstrumentos() {
        viewModelScope.launch {
            try {
                _instrumentos.value = if (api == null) checkNotNull(dbHelper).listarInstrumentos()
                    else todasLasPaginas { api.instrumentos("", it) }
                _uiState.update { it.copy(errorCarga = null) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(errorCarga = mensajeRed(error)) }
            }
        }
    }

    suspend fun cargarProtocoloParaEditar(protocoloId: Long) {
        _uiState.update { it.copy(cargandoEdicion = true) }
        try {
            val detalle = api?.protocolo(protocoloId)
            val protocolo = detalle?.protocolo?.aProtocolo()
                ?: dbHelper?.obtenerProtocoloPorId(protocoloId) ?: return
            val medicion = detalle?.mediciones?.firstOrNull()?.aMedicion(protocoloId)
                ?: dbHelper?.obtenerMedicionDeProtocolo(protocoloId)
            val instrumentoPrevio = medicion?.let { med ->
                if (api == null) _instrumentos.value.find { it.id == med.instrumentoId }
                else api.instrumento(med.instrumentoId)
            }
            fun formato(campo: String, valor: String): String =
                if (detalle == null) valor
                else detalle.textoIngresado?.get(campo) ?: valor.toBigDecimalOrNull()?.stripTrailingZeros()?.toPlainString().orEmpty()

            protocoloIdEditando = protocoloId
            _instrumentoSeleccionado.value = instrumentoPrevio

        _uiState.update {
            it.copy(
                temperaturaAmbiente = formato("temperatura", protocolo.temperatura),
                humedadRelativa = formato("humedad", protocolo.humedad),
                tipoPrueba = medicion?.tipoPrueba ?: "AISLACION",
                lectura1Min = formato("lectura1Min", medicion?.lectura1Min ?: ""),
                lectura10Min = formato("lectura10Min", medicion?.lectura10Min ?: ""),
                corrienteAjuste = formato("corrienteAjuste", medicion?.corrienteAjuste ?: ""),
                corrienteOperacion = formato("corrienteOperacion", medicion?.corrienteOperacion ?: ""),
                tiempoEsperado = formato("tiempoEsperado", medicion?.tiempoEsperado ?: ""),
                tiempoMedido = formato("tiempoMedido", medicion?.tiempoMedido ?: ""),
                lecturaDevanadoA = formato("lecturaDevanadoA", medicion?.devanadoA ?: ""),
                lecturaDevanadoB = formato("lecturaDevanadoB", medicion?.devanadoB ?: ""),
                lecturaDevanadoC = formato("lecturaDevanadoC", medicion?.devanadoC ?: ""),
                resultadoPrueba = protocolo.resultadoGlobal,
                motivoObservacion = protocolo.motivoObservacion,
                guardadoExitoso = false, errorMedicion = null
            )
        }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update { it.copy(errorMedicion = mensajeRed(error)) }
        } finally {
            _uiState.update { it.copy(cargandoEdicion = false) }
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
                lecturaDevanadoA = "",
                lecturaDevanadoB = "",
                lecturaDevanadoC = "",
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
    fun actualizarDevanadoA(valor: String) { _uiState.update { it.copy(lecturaDevanadoA = valor) } }
    fun actualizarDevanadoB(valor: String) { _uiState.update { it.copy(lecturaDevanadoB = valor) } }
    fun actualizarDevanadoC(valor: String) { _uiState.update { it.copy(lecturaDevanadoC = valor) } }

    private fun recalcularIP() {
        val state = _uiState.value
        if (state.tipoPrueba == "DEVANADOS") {
            _uiState.update { it.copy(resultadoPrueba = "Pendiente de revisión manual") }
            return
        }
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
        if (state.guardando || state.cargandoEdicion) return
        val instrumento = _instrumentoSeleccionado.value

        if (state.temperaturaAmbiente.isBlank() || state.humedadRelativa.isBlank()) {
            _uiState.update { it.copy(errorTemperatura = "Temperatura y humedad son obligatorias (RN-04)") }
            return
        }

        if (instrumento == null) {
            _uiState.update { it.copy(errorMedicion = "Debe seleccionar un instrumento (RF-05)") }
            return
        }

        if (api != null) {
            guardarRemoto(api, equipoId, instrumento, state, esEdicion)
            return
        }
        val local = checkNotNull(dbHelper)
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

            local.actualizarProtocolo(protocoloIdEditando!!, state.temperaturaAmbiente, state.humedadRelativa, state.resultadoPrueba)
            local.actualizarMedicion(
                protocoloId = protocoloIdEditando!!,
                tipoPrueba = state.tipoPrueba,
                lectura1 = state.lectura1Min,
                lectura10 = state.lectura10Min,
                ip = state.resultadoPrueba,
                instrumentoId = instrumento.id,
                corrienteAjuste = state.corrienteAjuste,
                corrienteOperacion = state.corrienteOperacion,
                tiempoEsperado = state.tiempoEsperado,
                tiempoMedido = state.tiempoMedido,
                devanadoA = state.lecturaDevanadoA,
                devanadoB = state.lecturaDevanadoB,
                devanadoC = state.lecturaDevanadoC
            )
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
            val nuevoId = local.guardarProtocolo(protocolo)
            if (nuevoId > 0) {
                local.guardarMedicion(
                    protocoloId = nuevoId,
                    tipoPrueba = state.tipoPrueba,
                    lectura1 = state.lectura1Min,
                    lectura10 = state.lectura10Min,
                    ip = state.resultadoPrueba,
                    instrumentoId = instrumento.id,
                    corrienteAjuste = state.corrienteAjuste,
                    corrienteOperacion = state.corrienteOperacion,
                    tiempoEsperado = state.tiempoEsperado,
                    tiempoMedido = state.tiempoMedido,
                    devanadoA = state.lecturaDevanadoA,
                    devanadoB = state.lecturaDevanadoB,
                    devanadoC = state.lecturaDevanadoC
                )
            }
            _uiState.update { it.copy(guardadoExitoso = true, protocoloGuardadoId = nuevoId) }
        }
    }

    private fun guardarRemoto(api: InduztekApi, equipoId: Long, instrumento: Instrumento,
                              state: ProtocoloUiState, esEdicion: Boolean) {
        val temperatura = state.temperaturaAmbiente.toBigDecimalOrNull()
        val humedad = state.humedadRelativa.toBigDecimalOrNull()
        if (temperatura == null || humedad == null || humedad.signum() < 0 || humedad > 100.toBigDecimal()) {
            _uiState.update { it.copy(errorTemperatura = "Ingresa una temperatura válida y humedad entre 0 y 100%") }
            return
        }
        val idEdicion = protocoloIdEditando
        if (esEdicion && idEdicion == null) {
            _uiState.update { it.copy(errorMedicion = "No se pudo cargar el protocolo para editarlo") }
            return
        }
        val valores = LecturasRemotas(
            lectura1Min = state.lectura1Min.toBigDecimalOrNull(),
            lectura10Min = state.lectura10Min.toBigDecimalOrNull(),
            corrienteAjuste = state.corrienteAjuste.toBigDecimalOrNull(),
            corrienteOperacion = state.corrienteOperacion.toBigDecimalOrNull(),
            tiempoEsperado = state.tiempoEsperado.toBigDecimalOrNull(),
            tiempoMedido = state.tiempoMedido.toBigDecimalOrNull(),
            lecturaDevanadoA = state.lecturaDevanadoA.toBigDecimalOrNull(),
            lecturaDevanadoB = state.lecturaDevanadoB.toBigDecimalOrNull(),
            lecturaDevanadoC = state.lecturaDevanadoC.toBigDecimalOrNull()
        )
        val textoIngresado = mapOf(
            "temperatura" to state.temperaturaAmbiente, "humedad" to state.humedadRelativa,
            "lectura1Min" to state.lectura1Min, "lectura10Min" to state.lectura10Min,
            "corrienteAjuste" to state.corrienteAjuste, "corrienteOperacion" to state.corrienteOperacion,
            "tiempoEsperado" to state.tiempoEsperado, "tiempoMedido" to state.tiempoMedido,
            "lecturaDevanadoA" to state.lecturaDevanadoA, "lecturaDevanadoB" to state.lecturaDevanadoB,
            "lecturaDevanadoC" to state.lecturaDevanadoC
        ).filterValues { it.toBigDecimalOrNull() != null }
        val solicitud = ProtocoloSolicitud(
            equipoId, instrumento.id, temperatura, humedad, state.tipoPrueba, valores, textoIngresado
        )
        _uiState.update { it.copy(guardando = true, errorMedicion = null) }
        viewModelScope.launch {
            try {
                val guardado = if (esEdicion) api.editarProtocolo(checkNotNull(idEdicion), solicitud)
                    else api.crearProtocolo(solicitud)
                _uiState.update { it.copy(guardadoExitoso = true, protocoloGuardadoId = guardado.id) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(errorMedicion = mensajeRed(error)) }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }
}