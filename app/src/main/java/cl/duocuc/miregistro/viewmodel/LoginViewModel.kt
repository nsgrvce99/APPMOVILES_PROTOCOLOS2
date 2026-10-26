package cl.duocuc.miregistro.viewmodel

import LoginUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duocuc.miregistro.data.SesionPreferencias
import cl.duocuc.miregistro.data.UsuarioDbHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update



class LoginViewModel(
    private val dbHelper: UsuarioDbHelper,
    private val sesionPrefs: SesionPreferencias
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChanged(correo: String) {
        _uiState.update { it.copy(correo = correo, errorMensaje = null) }
    }

    fun onContrasenaChanged(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, errorMensaje = null) }
    }

    fun autenticar(onSuccess: (Long) -> Unit) {
        val currentState = _uiState.value
        if (currentState.correo.isBlank() || currentState.contrasena.isBlank()) {
            _uiState.update { it.copy(errorMensaje = "Debe ingresar correo y contraseña") }
            return

        }

        val usuario = dbHelper.autenticarUsuario(currentState.correo.trim(), currentState.contrasena.trim())

        if (usuario != null) {
            sesionPrefs.guardar(usuario.id, usuario.rolId)
            onSuccess(usuario.rolId)
        } else {
            _uiState.update { it.copy(errorMensaje = "Credenciales inválidas o usuario inactivo") }
        }
    }
    fun onAlternarVisibilidadContrasena() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }
}

