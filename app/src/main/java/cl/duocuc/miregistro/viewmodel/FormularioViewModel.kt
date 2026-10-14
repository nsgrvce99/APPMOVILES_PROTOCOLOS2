package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.miregistro.model.FormularioUiState
import cl.duocuc.miregistro.model.NivelExperiencia
import cl.duocuc.miregistro.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// =====================================================================
//  FormularioViewModel.kt  —  LÓGICA DE LA PANTALLA 1  (la "VM" de MVVM)
//  ---------------------------------------------------------------------
//  - Guarda el estado y sobrevive a la rotación (lo escrito no se pierde).
//  - Expone el estado en SOLO LECTURA (StateFlow).
//  - Recibe los eventos de la vista con funciones "on...".
//  - Valida el formulario (la vista NO decide si un correo es válido).
//
//  Regla: el ViewModel NUNCA importa nada de Compose (ni Text ni Color).
//
//  CAMBIO (Bloque 1): onRegistrar() devuelve Boolean (true = datos válidos).
//  Así la pantalla sabe si puede navegar. El ViewModel NO navega.
//
//  CAMBIO (Bloque 2): onRegistrar() devuelve Usuario? (los datos que
//  viajarán al Perfil, o null si hay errores) + evento onCiudadSeleccionada.
// =====================================================================

class FormularioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    // ----- Eventos de los campos del formulario en terreno -----

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null, registroExitoso = false) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, registroExitoso = false) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, registroExitoso = false) }
    }

    fun onCiudadSeleccionada(ciudad: String) {
        _uiState.update { it.copy(ciudad = ciudad) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onNivelSeleccionado(nivel: NivelExperiencia) {
        _uiState.update { it.copy(nivel = nivel) }
    }

    fun onNoticiasCambia(valor: Boolean) {
        _uiState.update { it.copy(recibirNoticias = valor) }
    }

    fun onTerminosCambia(valor: Boolean) {
        _uiState.update { it.copy(aceptaTerminos = valor, errorTerminos = null) }
    }

    // ----- Validación y Registro en Terreno -----

    /**
     * Valida los datos obligatorios del protocolo en terreno
     * (TAG del equipo, temperatura ambiental, aceptación de acta preliminar).
     */
    fun onRegistrar(): Usuario? {
        val estado = _uiState.value

        val errorNombre = if (estado.nombre.isBlank()) "El TAG del equipo es obligatorio (RN-01)" else null
        val errorCorreo = if (estado.correo.isBlank()) "El número de serie o marca es obligatorio" else null
        // Validación de temperatura ambiental obligatoria según RN-04
        val errorContrasena = if (estado.contrasena.isBlank()) "La temperatura ambiente es obligatoria (RN-04)" else null
        val errorTerminos = if (!estado.aceptaTerminos) "Debes confirmar el acta preliminar de terreno" else null

        val hayErrores = listOf(errorNombre, errorCorreo, errorContrasena, errorTerminos)
            .any { it != null }

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorCorreo = errorCorreo,
                errorContrasena = errorContrasena,
                errorTerminos = errorTerminos,
                registroExitoso = !hayErrores
            )
        }

        // Si no hay errores, retorna un objeto Usuario adaptado con los datos ingresados
        return if (hayErrores) null else Usuario(
            rut = "12.345.678-9", // RUT sintético para el MVP
            nombre = estado.nombre, // TAG del equipo
            correo = estado.correo, // Serie/Placa
            rolId = 1L,             // Rol por defecto: Técnico de terreno
            rolNombre = "P-01 Técnico de terreno"
        )
    }

    /** Botón "Limpiar": reinicia el formulario al estado inicial. */
    fun onLimpiar() {
        _uiState.value = FormularioUiState()
    }
}
