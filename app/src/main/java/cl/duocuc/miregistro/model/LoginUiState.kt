data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false, // 👈 NUEVO
    val errorMensaje: String? = null,
    val cargando: Boolean = false
)