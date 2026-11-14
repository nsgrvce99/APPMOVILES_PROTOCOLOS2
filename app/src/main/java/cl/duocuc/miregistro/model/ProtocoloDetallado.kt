package cl.duocuc.miregistro.model

data class ProtocoloDetallado(
    val id: Long,
    val equipoTag: String,
    val equipoTipo: String,
    val instalacion: String,
    val tecnicoNombre: String,
    val fecha: String,
    val resultadoGlobal: String,
    val estado: String,
    val motivoObservacion: String? = null
)