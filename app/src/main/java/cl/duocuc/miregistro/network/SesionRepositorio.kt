package cl.duocuc.miregistro.network

import cl.duocuc.miregistro.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

interface AlmacenSesion {
    fun leer(): SesionGuardada?
    fun guardar(sesion: SesionGuardada?)
}

data class SesionGuardada(val token: String, val correo: String, val temporal: Boolean)

sealed interface EstadoSesion {
    data object Restaurando : EstadoSesion
    data class Cerrada(val mensaje: String? = null) : EstadoSesion
    data object CambioObligatorio : EstadoSesion
    data class Activa(val usuario: Usuario) : EstadoSesion
    data class SinConexion(val mensaje: String) : EstadoSesion
}

class SesionRepositorio(private val almacen: AlmacenSesion) {
    private val estadoInterno = MutableStateFlow<EstadoSesion>(EstadoSesion.Restaurando)
    val estado = estadoInterno.asStateFlow()

    @Volatile private var sesion: SesionGuardada? = null

    fun token(): String? = sesion?.token

    suspend fun restaurar(api: InduztekApi) {
        estadoInterno.value = EstadoSesion.Restaurando
        val guardada = withContext(Dispatchers.IO) { almacen.leer() }
        sesion = guardada
        if (guardada == null) {
            estadoInterno.value = EstadoSesion.Cerrada()
            return
        }
        if (guardada.temporal) {
            estadoInterno.value = EstadoSesion.CambioObligatorio
            return
        }
        try {
            val usuario = api.perfil().aUsuario()
            if (sesion?.token == guardada.token) {
                estadoInterno.value = EstadoSesion.Activa(usuario)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (error is HttpException && error.code() in listOf(401, 403)) {
                invalidar(guardada.token)
            } else if (sesion?.token == guardada.token) {
                estadoInterno.value = EstadoSesion.SinConexion(mensajeRed(error))
            }
        }
    }

    suspend fun entrar(api: InduztekApi, correo: String, contrasena: String) {
        val respuesta = api.login(InicioSesion(correo.trim(), contrasena))
        val datos = SesionGuardada(respuesta.token, respuesta.usuario.correo, respuesta.requiereCambioContrasena)
        val destino = if (datos.temporal) EstadoSesion.CambioObligatorio
            else EstadoSesion.Activa(respuesta.usuario.aUsuario())
        withContext(Dispatchers.IO) { guardar(datos, destino) }
    }

    suspend fun cambiarContrasena(api: InduztekApi, solicitud: CambioContrasena) {
        checkNotNull(sesion) { "La sesión ha terminado" }
        val respuesta = api.cambiarContrasena(solicitud)
        if (!respuesta.isSuccessful) throw HttpException(respuesta)
        cerrar("Contraseña actualizada. Inicia sesión con tu nueva contraseña.")
    }

    suspend fun actualizarPerfil(api: InduztekApi) {
        val actual = sesion ?: return
        val usuario = api.perfil().aUsuario()
        if (sesion?.token == actual.token) estadoInterno.value = EstadoSesion.Activa(usuario)
    }

    suspend fun cerrar(mensaje: String? = null) = withContext(Dispatchers.IO) {
        guardar(null, EstadoSesion.Cerrada(mensaje))
    }

    @Synchronized
    fun invalidar(tokenAnterior: String) {
        if (sesion?.token == tokenAnterior) {
            guardar(null, EstadoSesion.Cerrada("La sesión venció. Vuelve a iniciar sesión."))
        }
    }

    @Synchronized
    private fun guardar(datos: SesionGuardada?, destino: EstadoSesion) {
        if (datos == null) {
            sesion = null
            estadoInterno.value = destino
            almacen.guardar(null)
        } else {
            almacen.guardar(datos)
            sesion = datos
            estadoInterno.value = destino
        }
    }
}

fun UsuarioRemoto.aUsuario() = Usuario(
    id = id, rut = rut, nombre = nombre, correo = correo, contrasena = "", rolId = rolId,
    rolNombre = rolNombre, activo = activo, giro = giro, cargo = cargo, fotoUri = fotoUri
)

fun mensajeRed(error: Throwable): String = when (error) {
    is HttpException -> when (error.code()) {
        400 -> "Revisa los datos ingresados."
        401 -> "Credenciales inválidas o sesión vencida."
        403 -> "No tienes permiso para esta operación."
        404 -> "El registro no está disponible."
        409 -> "La operación está protegida o existe un registro duplicado."
        in 500..599 -> "El servicio no está disponible. Intenta nuevamente."
        else -> "No se pudo completar la operación."
    }
    is IOException -> "No se pudo conectar con el servidor. Revisa tu conexión."
    else -> "No se pudo completar la operación."
}

internal fun generarClaveTemporal(): String {
    val aleatorio = java.security.SecureRandom()
    val caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789"
    return buildString {
        append("A!")
        repeat(14) { append(caracteres[aleatorio.nextInt(caracteres.length)]) }
    }
}