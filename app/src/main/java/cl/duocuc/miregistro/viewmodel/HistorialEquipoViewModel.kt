package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Protocolo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistorialEquipoViewModel(dbHelper: UsuarioDbHelper, equipoId: Long) : ViewModel() {

    private val _protocolos = MutableStateFlow<List<Protocolo>>(emptyList())
    val protocolos: StateFlow<List<Protocolo>> = _protocolos.asStateFlow()

    init {
        // Carga inicial de datos
        _protocolos.value = dbHelper.listarProtocolosPorEquipo(equipoId)
    }
}