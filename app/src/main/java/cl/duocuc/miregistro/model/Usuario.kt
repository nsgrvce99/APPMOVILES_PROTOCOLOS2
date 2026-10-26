package cl.duocuc.miregistro.model



data class Usuario(
    val id: Long = 0L,
    val rut: String,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rolId: Long,
    val rolNombre: String? = null,
    val activo: Boolean = true,
    val giro: String? = null,
    val cargo: String? = null
)
