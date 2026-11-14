package cl.duocuc.miregistro.model

data class ResumenVisita(
    val totalPruebas: Int = 0,
    val aprobadas: Int = 0,
    val precaucion: Int = 0,
    val rechazadas: Int = 0,
    val borradores: Int = 0,
    val pendientesFormalizar: Int = 0,
    val formalizadas: Int = 0
)