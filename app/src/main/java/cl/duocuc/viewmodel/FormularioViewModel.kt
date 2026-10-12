package cl.duocuc.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.model.FormularioUiState
import cl.duocuc.model.NivelExperiencia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel : ViewModel(){
    //manipular o manejar estados de la pantalla
    //mutable y privada para manejar la información y modificarla solo en el viewmodel
    private val _uiState = MutableStateFlow(FormularioUiState())
    //publica de solo lectura para mostrar los datos sincronizados y actualizados en la ui
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    //Eventos de los campos: cada texto mal escrito llega y se manipula por acá
    fun onNombreCambia(valor: String){
        _uiState.update { it.copy(nombre = valor, errorNombre = null, registroExitoso = false) }
    }
    fun onCorreoCambia(valor: String){
        _uiState.update { it.copy(correo = valor, errorCorreo = null, registroExitoso = false) }
    }
    fun onContrasenaCambia(valor: String){
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, registroExitoso = false) }
    }
    fun onAlternaVisibilidad(){
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }
    //eventos para el control de seleccion
    fun onNivelSeleccionado(nivel: NivelExperiencia){
        _uiState.update { it.copy(nivel = nivel) }
    }
    fun onNoticiaCambia(valor: Boolean){
        _uiState.update { it.copy(recibirNoticias = valor) }
    }
    fun onTerminosCambia(valor: Boolean){
        _uiState.update { it.copy(aceptaTerminos = valor) }
    }
    //Acciones de los botones
    //Boton registrarse
    fun onRegistrar(){
        val estado = _uiState.value //traigo toda la información de las variables dentro del state
        //validar todos los errores que pueden haber ocurrido
        //en los campos cuando el usuario los relleno con informacion
        val errorNombre = if(estado.nombre.isBlank()) "El nombre es obligatorio" else null
        val errorCorreo = if(!estado.correo.contains("@") || !estado.correo.contains("."))
            "Ingrese un correo válido" else null
        val errorContrasena = if(estado.contrasena.length < 6) "Mínimo 6 caracteres" else null
        val errorTerminos = if(!estado.aceptaTerminos) "Debes aceptar los términos" else null
        //validar si existen errores para cambiarlos y mostrarlos
    }
    //Boton limpiar
    fun onLimpiar(){
        _uiState.value = FormularioUiState()
    }

}