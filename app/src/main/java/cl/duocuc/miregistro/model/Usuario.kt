package cl.duocuc.miregistro.model

// =====================================================================
//  Usuario.kt  —  DATOS QUE VIAJAN ENTRE PANTALLAS  (NUEVO, Bloque 2)
//  ---------------------------------------------------------------------
//  Es el "paquete" que el Formulario entrega y que el Perfil y la
//  lista de usuarios muestran. La contraseña NO viaja.
// =====================================================================

data class Usuario(
    val id: Long = 0L,
    val rut: String,
    val nombre: String,
    val correo: String,
    val rolId: Long,             // Llave foránea hacia la tabla rol (1=Técnico, 2=Ingeniero, etc.)
    val rolNombre: String? = null, // Nombre descriptivo del rol (obtenido mediante JOIN en SQLite)
    val activo: Boolean = true,
    val giro: String? = null,      // Opcional: Solo si es rol CLIENTE
    val cargo: String? = null      // Opcional: Solo si es rol CLIENTE
)
