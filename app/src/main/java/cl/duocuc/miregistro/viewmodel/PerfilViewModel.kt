package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.miregistro.model.Perfil
import cl.duocuc.miregistro.model.PerfilUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update



class PerfilViewModel : ViewModel() {


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

    fun cargarPerfil(perfilNuevo: Perfil) {
        _uiState.update { it.copy(perfil = perfilNuevo) }
    }
}
