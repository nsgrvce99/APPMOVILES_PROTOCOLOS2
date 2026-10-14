package cl.duocuc.miregistro.model

data class ProtocoloUiState(
    val codigoEquipo: String = "",
    val tipoEquipo: String = "Transformador",
    val temperaturaAmbiente: String = "",
    val humedadRelativa: String = "",
    // Mediciones específicas (ej. Aislación / IP)
    val lectura1Min: String = "",
    val lectura10Min: String = "",
    val tensionPrueba: String = "5 kV",
    // Estado visual y validaciones
    val errorTemperatura: String? = null,
    val errorMedicion: String? = null,
    val resultadoPrueba: String = "Pendiente", // Aprobado / Observado / Rechazado
    val guardadoExitoso: Boolean = false
)