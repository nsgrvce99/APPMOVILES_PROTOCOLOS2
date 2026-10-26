package cl.duocuc.miregistro.model

data class Equipo(
    val id: Int = 0,
    val tag: String,
    val tipoEquipo: String,
    val clasificacion: String = "Primario",
    val instalacion: String,
    val marca: String,
    val serie: String,
    val potenciaTension: String = "",
    val idCliente: Long = 5L
)
