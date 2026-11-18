package cl.duocuc.miregistro.model

data class Medicion(
    val id: Long = 0,
    val protocoloId: Long,
    val tipoPrueba: String,
    val lectura1Min: String,
    val lectura10Min: String,
    val indicePolarizacion: String,
    val instrumentoId: Long = 0L,
    val corrienteAjuste: String = "",
    val corrienteOperacion: String = "",
    val tiempoEsperado: String = "",
    val tiempoMedido: String = "",
    val devanadoA: String = "",
    val devanadoB: String = "",
    val devanadoC: String = ""
)

