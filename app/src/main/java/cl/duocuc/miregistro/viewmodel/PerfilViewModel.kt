package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.miregistro.model.Perfil
import cl.duocuc.miregistro.model.PerfilUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// =====================================================================
//  PerfilViewModel.kt  —  LÓGICA DE LA PANTALLA 2
//  ---------------------------------------------------------------------
//  Por ahora los datos están escritos aquí ("datos de ejemplo").
//  Más adelante vendrán de una base de datos o una API, y la pantalla
//  NO tendrá que cambiar: esa es una ventaja de separar con MVVM.
//  Esta pantalla aún NO recibe datos del formulario (no hay navegación).
// =====================================================================

class PerfilViewModel : ViewModel() {

    // Perfil de ejemplo basado en los datos del Anexo 9 (Induztek)
    private val perfilEjemplo = Perfil(
        id = 1L,
        rut = "17.284.519-3",
        nombre = "Matías Godoy Herrera",
        correo = "mgodoy@induztek.ejemplo",
        rolNombre = "P-01 Técnico de terreno",
        activo = true,
        giro = null,
        cargo = "Técnico Especializado"
    )

    private val _uiState = MutableStateFlow(PerfilUiState(perfil = perfilEjemplo))
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    /** Actualiza el perfil mostrado en pantalla si llega un usuario desde la sesión */
    fun cargarPerfil(perfilNuevo: Perfil) {
        _uiState.update { it.copy(perfil = perfilNuevo) }
    }
}
