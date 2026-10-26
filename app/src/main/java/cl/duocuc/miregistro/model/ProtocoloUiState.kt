package cl.duocuc.miregistro.model

data class ProtocoloUiState(
    val codigoEquipo: String ="",
    val tipoPrueba: String = "AISLACION",
    val tipoEquipo: String = "Transformador",
    val temperaturaAmbiente: String = "",
    val humedadRelativa: String = "",
    val lectura1Min: String = "",
    val lectura10Min: String = "",
    val tensionPrueba: String = "5 kV",
    val instrumentoSeleccionado: String = "",
    val errorTemperatura: String? = null,
    val errorMedicion: String? = null,
    val resultadoPrueba: String = "Pendiente",
    val guardadoExitoso: Boolean = false,
    val protocoloGuardadoId: Long? = null,
    val relacionTeorica: String = "",
    val relacionMedida: String = "",
    val valorMedidoContacto: String = "",
    val valorPlacaContacto: String = ""

)