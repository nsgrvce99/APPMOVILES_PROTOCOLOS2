package cl.duocuc.miregistro.model

data class Medicion(
    val id: Long = 0,
    val protocoloId: Long,
    val tipoPrueba: String,
    val lectura1Min: String,
    val lectura10Min: String,
    val indicePolarizacion: String
)

