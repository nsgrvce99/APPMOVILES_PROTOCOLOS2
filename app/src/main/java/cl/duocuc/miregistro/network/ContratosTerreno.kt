package cl.duocuc.miregistro.network

import java.math.BigDecimal

data class EquipoSolicitud(val tag: String, val tipoEquipo: String, val marca: String,
                           val serie: String, val instalacionId: Long)
data class InstalacionSolicitud(val nombre: String, val idCliente: Long)
data class ClienteSolicitud(val nombre: String, val rut: String, val correo: String, val giro: String)
data class ActivacionSolicitud(val contrasena: String)
data class PersonalSolicitud(val nombre: String, val rut: String, val correo: String, val rolId: Long)
data class PersonalCreado(val usuario: UsuarioRemoto, val contrasenaTemporal: String)
data class FotoSolicitud(val fotoUri: String?)
data class InstrumentoSolicitud(val nombre: String, val marca: String, val serie: String, val fechaVencimiento: String)
data class ComentarioSolicitud(val texto: String?)
data class ObservacionSolicitud(val motivo: String)
data class PersonaRemota(val id: Long, val nombre: String, val archivado: Boolean = false)
data class EquipoReferencia(val id: Long, val idCliente: Long, val tag: String?, val tipoEquipo: String?,
                            val instalacion: String?, val archivado: Boolean = false)
data class ActaRemota(val id: Long, val protocoloId: Long, val nombreReceptor: String, val rutReceptor: String,
                     val cargoReceptor: String, val fechaHoraRecepcion: String)
data class DetalleRemoto(val protocolo: ProtocoloRemoto, val mediciones: List<MedicionRemota>,
                        val equipo: EquipoReferencia?, val cliente: PersonaRemota?, val tecnico: PersonaRemota?,
                        val acta: ActaRemota?, val textoIngresado: Map<String, String>? = null)
data class MedicionRemota(val id: Long, val tipoPrueba: String, val instrumentoId: Long,
                         val indicePolarizacion: String?, val valores: LecturasRemotas)
data class LecturasRemotas(
    val lectura1Min: BigDecimal? = null, val lectura10Min: BigDecimal? = null,
    val corrienteAjuste: BigDecimal? = null, val corrienteOperacion: BigDecimal? = null,
    val tiempoEsperado: BigDecimal? = null, val tiempoMedido: BigDecimal? = null,
    val lecturaDevanadoA: BigDecimal? = null, val lecturaDevanadoB: BigDecimal? = null,
    val lecturaDevanadoC: BigDecimal? = null
)
data class ProtocoloSolicitud(val equipoId: Long, val instrumentoId: Long, val temperatura: BigDecimal,
                             val humedad: BigDecimal, val tipoPrueba: String, val valores: LecturasRemotas,
                             val textoIngresado: Map<String, String> = emptyMap())
data class ResumenVisitaRemoto(val total: Int, val aprobadas: Int, val precaucion: Int, val rechazadas: Int,
                              val borradores: Int, val pendientes: Int, val formalizadas: Int)
