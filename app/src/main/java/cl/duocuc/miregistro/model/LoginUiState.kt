data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,
    val errorMensaje: String? = null,
    val cargando: Boolean = false
)