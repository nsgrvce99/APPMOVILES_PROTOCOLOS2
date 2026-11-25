package cl.duocuc.miregistro.network

import cl.duocuc.miregistro.model.Equipo
import cl.duocuc.miregistro.model.Instrumento
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InduztekApi {
    @POST("api/auth/login")
    suspend fun login(@Body solicitud: InicioSesion): SesionRemota

    @GET("api/usuarios/me")
    suspend fun perfil(): UsuarioRemoto

    @PATCH("api/usuarios/me/contrasena")
    suspend fun cambiarContrasena(@Body solicitud: CambioContrasena): Response<Unit>

    @GET("api/instalaciones")
    suspend fun instalaciones(@Query("q") filtro: String, @Query("page") pagina: Int, @Query("size") limite: Int = 20): Pagina<InstalacionRemota>

    @GET("api/instalaciones/{id}/equipos")
    suspend fun equipos(@Path("id") instalacion: Long, @Query("page") pagina: Int): Pagina<Equipo>

    @GET("api/equipos/mis-equipos")
    suspend fun misEquipos(@Query("q") filtro: String, @Query("page") pagina: Int): Pagina<Equipo>

    @GET("api/instrumentos")
    suspend fun instrumentos(@Query("q") filtro: String, @Query("page") pagina: Int, @Query("size") limite: Int = 20): Pagina<Instrumento>

    @GET("api/equipos/{id}")
    suspend fun equipo(@Path("id") id: Long): Equipo

    @POST("api/equipos")
    suspend fun crearEquipo(@Body datos: EquipoSolicitud): Equipo

    @DELETE("api/equipos/{id}")
    suspend fun eliminarEquipo(@Path("id") id: Long): Response<Unit>

    @POST("api/instalaciones")
    suspend fun crearInstalacion(@Body datos: InstalacionSolicitud): InstalacionRemota

    @DELETE("api/instalaciones/{id}")
    suspend fun eliminarInstalacion(@Path("id") id: Long): Response<Unit>

    @GET("api/usuarios/{id}")
    suspend fun usuario(@Path("id") id: Long): UsuarioRemoto

    @GET("api/clientes")
    suspend fun clientes(@Query("query") filtro: String, @Query("limit") limite: Int,
                         @Query("offset") desplazamiento: Int): List<UsuarioRemoto>

    @POST("api/clientes")
    suspend fun crearCliente(@Body datos: ClienteSolicitud): UsuarioRemoto

    @PUT("api/clientes/{id}")
    suspend fun editarCliente(@Path("id") id: Long, @Body datos: ClienteSolicitud): UsuarioRemoto

    @PATCH("api/clientes/{id}/activar")
    suspend fun activarCliente(@Path("id") id: Long, @Body datos: ActivacionSolicitud): UsuarioRemoto

    @DELETE("api/clientes/{id}")
    suspend fun eliminarCliente(@Path("id") id: Long): Response<Unit>

    @GET("api/usuarios/personal")
    suspend fun personal(@Query("query") filtro: String, @Query("limit") limite: Int,
                         @Query("offset") desplazamiento: Int): List<UsuarioRemoto>

    @POST("api/usuarios/personal")
    suspend fun crearPersonal(@Body datos: PersonalSolicitud): PersonalCreado

    @PUT("api/usuarios/{id}")
    suspend fun editarPersonal(@Path("id") id: Long, @Body datos: PersonalSolicitud): UsuarioRemoto

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarPersonal(@Path("id") id: Long): Response<Unit>

    @PATCH("api/usuarios/me/foto")
    suspend fun cambiarFoto(@Body datos: FotoSolicitud): UsuarioRemoto

    @GET("api/instrumentos/{id}")
    suspend fun instrumento(@Path("id") id: Long): Instrumento

    @POST("api/instrumentos")
    suspend fun crearInstrumento(@Body datos: InstrumentoSolicitud): Instrumento

    @PUT("api/instrumentos/{id}")
    suspend fun editarInstrumento(@Path("id") id: Long, @Body datos: InstrumentoSolicitud): Instrumento

    @DELETE("api/instrumentos/{id}")
    suspend fun eliminarInstrumento(@Path("id") id: Long): Response<Unit>

    @GET("api/protocolos/{id}")
    suspend fun protocolo(@Path("id") id: Long): DetalleRemoto

    @GET("api/protocolos/mis-protocolos")
    suspend fun misProtocolos(@Query("page") pagina: Int, @Query("size") limite: Int): Pagina<ProtocoloRemoto>

    @GET("api/protocolos/pendientes")
    suspend fun pendientes(@Query("page") pagina: Int, @Query("size") limite: Int): Pagina<ProtocoloRemoto>

    @GET("api/protocolos/resumen-visita")
    suspend fun resumen(): ResumenVisitaRemoto

    @POST("api/protocolos")
    suspend fun crearProtocolo(@Body datos: ProtocoloSolicitud): ProtocoloRemoto

    @PUT("api/protocolos/{id}")
    suspend fun editarProtocolo(@Path("id") id: Long, @Body datos: ProtocoloSolicitud): ProtocoloRemoto

    @PATCH("api/protocolos/{id}/cerrar")
    suspend fun cerrarProtocolo(@Path("id") id: Long): ProtocoloRemoto

    @PATCH("api/protocolos/{id}/anular")
    suspend fun anularProtocolo(@Path("id") id: Long): ProtocoloRemoto

    @GET("api/protocolos/{id}/variacion")
    suspend fun variacion(@Path("id") id: Long): VariacionRemota

    @PATCH("api/protocolos/{id}/formalizar")
    suspend fun formalizar(@Path("id") id: Long, @Body datos: ComentarioSolicitud): ProtocoloRemoto

    @PATCH("api/protocolos/{id}/observar")
    suspend fun observar(@Path("id") id: Long, @Body datos: ObservacionSolicitud): ProtocoloRemoto

    @DELETE("api/protocolos/{id}")
    suspend fun eliminarProtocolo(@Path("id") id: Long): Response<Unit>

    @POST("api/protocolos/{id}/acta")
    suspend fun entregarActa(@Path("id") id: Long): ActaRemota

    @GET("api/protocolos/equipo/{id}")
    suspend fun historial(@Path("id") equipo: Long, @Query("page") pagina: Int, @Query("size") limite: Int = 10): Pagina<ProtocoloRemoto>
}

data class InicioSesion(val correo: String, val contrasena: String)
data class CambioContrasena(val actual: String, val nueva: String, val confirmar: String)
data class SesionRemota(
    val token: String,
    val idUsuario: Long,
    val rolId: Long,
    val requiereCambioContrasena: Boolean,
    val usuario: UsuarioRemoto
)
data class UsuarioRemoto(
    val id: Long,
    val nombre: String,
    val rut: String,
    val correo: String,
    val rolId: Long,
    val activo: Boolean,
    val giro: String?,
    val telefono: String?,
    val cargo: String?,
    val fotoUri: String?,
    val rolNombre: String? = null
)
data class InstalacionRemota(val id: Long, val nombre: String, val idCliente: Long)
data class ProtocoloRemoto(
    val id: Long,
    val equipoId: Long,
    val tecnicoId: Long,
    val fecha: String,
    val temperatura: String,
    val humedad: String,
    val resultadoGlobal: String,
    val estado: String,
    val motivoObservacion: String?,
    val comentarioVariacion: String?
)
data class Pagina<T>(val content: List<T>, val page: MetadatosPagina)
data class MetadatosPagina(val size: Int, val number: Int, val totalElements: Long, val totalPages: Int)

data class VariacionRemota(val significativa: Boolean)
