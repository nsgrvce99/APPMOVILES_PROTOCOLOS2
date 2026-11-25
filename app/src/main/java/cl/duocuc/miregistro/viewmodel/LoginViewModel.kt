package cl.duocuc.miregistro.viewmodel

import cl.duocuc.miregistro.utils.Validaciones

import LoginUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.miregistro.network.mensajeRed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import cl.duocuc.miregistro.data.SesionPreferencias
import cl.duocuc.miregistro.data.UsuarioDbHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(private val iniciar: suspend (String, String) -> ResultadoIngreso) : ViewModel() {
    data class ResultadoIngreso(val rolId: Long, val temporal: Boolean)

    constructor(dbHelper: UsuarioDbHelper, sesionPrefs: SesionPreferencias) : this({ correo, clave ->
        val usuario = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dbHelper.autenticarUsuario(correo, clave.trim())
        } ?: throw CredencialesLocalesInvalidas()
        sesionPrefs.guardar(usuario.id, usuario.rolId)
        ResultadoIngreso(usuario.rolId, Validaciones.esClaveTemporalLocal(usuario.contrasena))
    })

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChanged(correo: String) {
        _uiState.update { it.copy(correo = correo, errorMensaje = null) }
    }

    fun onContrasenaChanged(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, errorMensaje = null) }
    }


    fun autenticar(onSuccess: (Long) -> Unit, onRequiereCambio: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.correo.isBlank() || currentState.contrasena.isBlank()) {
            _uiState.update { it.copy(errorMensaje = "Debe ingresar correo y contraseña") }
            return
        }

        if (currentState.cargando) return
        _uiState.update { it.copy(cargando = true, errorMensaje = null) }
        viewModelScope.launch {
            try {
                val resultado = iniciar(currentState.correo.trim(), currentState.contrasena)
                _uiState.update { it.copy(contrasena = "", cargando = false) }
                if (resultado.temporal) onRequiereCambio() else onSuccess(resultado.rolId)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val mensaje = if (error is CredencialesLocalesInvalidas) "Credenciales inválidas o usuario inactivo"
                    else mensajeRed(error)
                _uiState.update { it.copy(errorMensaje = mensaje, cargando = false) }
            }
        }
    }

    private class CredencialesLocalesInvalidas : RuntimeException()

    fun onAlternarVisibilidadContrasena() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }
}

