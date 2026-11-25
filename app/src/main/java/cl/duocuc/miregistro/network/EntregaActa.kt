package cl.duocuc.miregistro.network

internal suspend fun InduztekApi.confirmarEntregaActa(protocoloId: Long): ActaRemota {
    val detalle = protocolo(protocoloId)
    detalle.acta?.let { return it }
    if (detalle.protocolo.estado == "BORRADOR") cerrarProtocolo(protocoloId)
    return entregarActa(protocoloId)
}
