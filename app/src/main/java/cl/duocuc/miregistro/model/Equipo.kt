package cl.duocuc.miregistro.model

data class Equipo(
    val id: Int,
    val tag: String,               // Ej: "TR-01" (único por instalación según RN-01)
    val tipoEquipo: String,        // Transformador, Interruptor, Celda MT, etc.
    val clasificacion: String,     // Primario o Secundario (Aclaración nº 1)
    val instalacion: String,       // Ej: "Planta Lampa"
    val marca: String,
    val serie: String,
    val potenciaTension: String    // Datos de placa
)
