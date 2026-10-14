package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.miregistro.model.Rol
import cl.duocuc.miregistro.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// =====================================================================
//  UsuariosViewModel.kt  —  VIEWMODEL COMPARTIDO  (NUEVO, Bloque 2)
//  ---------------------------------------------------------------------
//  Problema: cada pantalla tiene su propio ViewModel y NO se conocen.
//  Solución: un ViewModel que vive POR ENCIMA de las pantallas.
//  Se crea en AppNavegacion (fuera del NavHost), así pertenece a la
//  Activity y no se destruye al cambiar de pantalla.
//
//    Formulario --registrar(usuario)--> [UsuariosViewModel] --usuarioActual--> Perfil
//                                                └──────────── usuarios ────> Lista (Bloque 3)
//
//  CAMBIO (Bloque 4): actualizar() (formulario de edición) y cerrarSesion().
//
//  No recibe nada en el constructor -> viewModel() lo crea sin factory.
//  Los datos viven en MEMORIA: si se cierra la app, se pierden.
// =====================================================================

class UsuariosViewModel : ViewModel() {

    // Lista de usuarios/personal registrados
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    // Usuario con sesión iniciada actualmente (null = nadie)
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    /** Inicia sesión con un usuario específico */
    fun iniciarSesionComo(usuario: Usuario) {
        _usuarioActual.value = usuario
    }

    /** Cierra la sesión actual */
    fun cerrarSesion() {
        _usuarioActual.value = null
    }

    /** Agrega un nuevo usuario a la lista */
    fun registrar(usuario: Usuario) {
        _usuarios.update { lista -> lista + usuario }
        if (_usuarioActual.value == null) {
            _usuarioActual.value = usuario
        }
    }

    /** Actualiza los datos de un usuario en una posición específica */
    fun actualizar(indice: Int, editado: Usuario) {
        val anterior = _usuarios.value.getOrNull(indice)
        _usuarios.update { lista ->
            lista.mapIndexed { i, u -> if (i == indice) editado else u }
        }
        if (_usuarioActual.value == anterior) {
            _usuarioActual.value = editado
        }
    }

    // -----------------------------------------------------------------
    // VALIDACIÓN DE PERMISOS SEGÚN ANEXO 7 (Matriz de Permisos Induztek)
    // -----------------------------------------------------------------

    /** Verifica si el usuario actual puede formalizar o observar protocolos (Ingeniero / Jefe Técnico) */
    fun puedeFormalizar(): Boolean {
        val rolId = _usuarioActual.value?.rolId
        // Según el script precargado: 2 = INGENIERO, 3 = JEFE_TECNICO
        return rolId == 2L || rolId == 3L
    }

    /** Verifica si el usuario actual puede registrar pruebas en terreno (Técnico / Ingeniero / Jefe Técnico) */
    fun puedeRegistrarPrueba(): Boolean {
        val rolId = _usuarioActual.value?.rolId
        // 1 = TECNICO, 2 = INGENIERO, 3 = JEFE_TECNICO
        return rolId == 1L || rolId == 2L || rolId == 3L
    }

    /** Verifica si el usuario actual tiene rol de Cliente (solo consulta, RN-18) */
    fun esCliente(): Boolean {
        val rolId = _usuarioActual.value?.rolId
        // 4 = CLIENTE
        return rolId == 4L
    }
}
