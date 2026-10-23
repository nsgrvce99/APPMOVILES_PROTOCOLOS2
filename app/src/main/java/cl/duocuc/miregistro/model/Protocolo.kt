package cl.duocuc.miregistro.model
data class Protocolo(
    val id: Long = 0,
    val equipoId: Long,
    val tecnicoId: Long,
    val fecha: String,
    val temperatura: String,
    val humedad: String,
    val resultadoGlobal: String,
    val estado: String,
    val motivoObservacion: String? = null
)