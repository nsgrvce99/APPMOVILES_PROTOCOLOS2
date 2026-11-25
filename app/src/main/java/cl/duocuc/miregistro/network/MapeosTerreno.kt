package cl.duocuc.miregistro.network

import cl.duocuc.miregistro.model.Medicion
import cl.duocuc.miregistro.model.Protocolo
import cl.duocuc.miregistro.model.ProtocoloDetallado

fun ProtocoloRemoto.aProtocolo() = Protocolo(
    id, equipoId, tecnicoId, fecha.replace('T', ' ').take(16), temperatura, humedad, resultadoVisible(resultadoGlobal),
    estado, motivoObservacion, comentarioVariacion
)

fun MedicionRemota.aMedicion(protocoloId: Long) = Medicion(
    id, protocoloId, tipoPrueba, valores.lectura1Min?.toPlainString().orEmpty(),
    valores.lectura10Min?.toPlainString().orEmpty(), indicePolarizacion.orEmpty(), instrumentoId,
    valores.corrienteAjuste?.toPlainString().orEmpty(), valores.corrienteOperacion?.toPlainString().orEmpty(),
    valores.tiempoEsperado?.toPlainString().orEmpty(), valores.tiempoMedido?.toPlainString().orEmpty(),
    valores.lecturaDevanadoA?.toPlainString().orEmpty(), valores.lecturaDevanadoB?.toPlainString().orEmpty(),
    valores.lecturaDevanadoC?.toPlainString().orEmpty()
)

private fun resultadoVisible(resultado: String) = when (resultado) {
    "APROBADO" -> "Aprobado"
    "PRECAUCION" -> "Precaucion"
    "RECHAZADO" -> "Rechazado"
    "REVISION_MANUAL" -> "Pendiente de revisión manual"
    "PENDIENTE" -> "Pendiente"
    else -> resultado
}

fun DetalleRemoto.aTarjeta(): ProtocoloDetallado {
    val registro = protocolo.aProtocolo()
    return ProtocoloDetallado(registro.id, equipo?.tag.orEmpty(), equipo?.tipoEquipo.orEmpty(),
        equipo?.instalacion.orEmpty(), tecnico?.nombre.orEmpty(), registro.fecha,
        registro.resultadoGlobal, registro.estado, registro.motivoObservacion)
}