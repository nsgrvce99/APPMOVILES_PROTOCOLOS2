package cl.duocuc.miregistro.model

data class Equipo(
    val id: Int = 0,                 // 👈 0 por defecto para equipos nuevos
    val tag: String,
    val tipoEquipo: String,
    val clasificacion: String = "Primario",
    val instalacion: String,
    val marca: String,
    val serie: String,
    val potenciaTension: String = "",
    val idCliente: Long = 5L         // 👈 Por defecto asignamos al cliente de prueba
)
