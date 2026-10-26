package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.miregistro.model.Rol
import cl.duocuc.miregistro.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update



class UsuariosViewModel : ViewModel() {


    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()


    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()


    fun iniciarSesionComo(usuario: Usuario) {
        _usuarioActual.value = usuario
    }


    fun cerrarSesion() {
        _usuarioActual.value = null
    }


    fun registrar(usuario: Usuario) {
        _usuarios.update { lista -> lista + usuario }
        if (_usuarioActual.value == null) {
            _usuarioActual.value = usuario
        }
    }


    fun actualizar(indice: Int, editado: Usuario) {
        val anterior = _usuarios.value.getOrNull(indice)
        _usuarios.update { lista ->
            lista.mapIndexed { i, u -> if (i == indice) editado else u }
        }
        if (_usuarioActual.value == anterior) {
            _usuarioActual.value = editado
        }
    }




    fun puedeFormalizar(): Boolean {
        val rolId = _usuarioActual.value?.rolId

        return rolId == 2L || rolId == 3L
    }


    fun puedeRegistrarPrueba(): Boolean {
        val rolId = _usuarioActual.value?.rolId

        return rolId == 1L || rolId == 2L || rolId == 3L
    }


    fun esCliente(): Boolean {
        val rolId = _usuarioActual.value?.rolId

        return rolId == 4L
    }
}
