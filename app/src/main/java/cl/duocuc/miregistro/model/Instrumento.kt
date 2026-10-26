package cl.duocuc.miregistro.model

data class Instrumento(
    val id: Long = 0,
    val nombre: String,
    val marca: String,
    val serie: String,
    val fechaVencimiento: String
)