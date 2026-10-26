package cl.duocuc.miregistro.model



data class Perfil(
    val id: Long,
    val rut: String,
    val nombre: String,
    val correo: String,
    val rolNombre: String,
    val activo: Boolean,
    val giro: String? = null,
    val cargo: String? = null
)

data class PerfilUiState(
    val perfil: Perfil?,
    val cargando: Boolean = false,
    val mensajeError: String? = null
)
