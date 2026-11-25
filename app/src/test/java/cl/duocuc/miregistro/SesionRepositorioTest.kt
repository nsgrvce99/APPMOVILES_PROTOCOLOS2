package cl.duocuc.miregistro

import cl.duocuc.miregistro.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class SesionRepositorioTest {
    private val perfil = """
        {"id":5,"nombre":"Cliente","rut":"1-9","correo":"demo@example.invalid",
         "rolId":4,"rolNombre":"Cliente","activo":true}
    """.trimIndent()

    @Test
    fun guardaSesionTemporalYExigeNuevoIngresoAlCambiarClave() = runBlocking {
        MockWebServer().also { it.start(java.net.InetAddress.getByName("127.0.0.1"), 0) }.use { servidor ->
            val almacen = Memoria()
            val repo = SesionRepositorio(almacen)
            val api = conectar(servidor, repo)
            servidor.enqueue(json("""{"token":"temporal","requiereCambioContrasena":true,"usuario":$perfil}"""))
            servidor.enqueue(MockResponse().setResponseCode(204))
            repo.entrar(api, "  demo@example.invalid ", "clave-prueba")
            assertEquals(EstadoSesion.CambioObligatorio, repo.estado.value)
            assertTrue(checkNotNull(almacen.datos).temporal)
            val restaurada = SesionRepositorio(almacen)
            restaurada.restaurar(conectar(servidor, restaurada))
            assertEquals(EstadoSesion.CambioObligatorio, restaurada.estado.value)
            assertEquals(1, servidor.requestCount)

            repo.cambiarContrasena(api, CambioContrasena("clave-prueba", "Nueva!09", "Nueva!09"))
            assertTrue(repo.estado.value is EstadoSesion.Cerrada)
            assertNull(almacen.datos)
            assertNull(repo.token())
            assertEquals("/api/auth/login", servidor.takeRequest().path)
            assertEquals("Bearer temporal", servidor.takeRequest().getHeader("Authorization"))
        }
    }

    @Test
    fun validaLaIdentidadVigenteAlRestaurarYBorraAlRecibir401() = runBlocking {
        MockWebServer().also { it.start(java.net.InetAddress.getByName("127.0.0.1"), 0) }.use { servidor ->
            val almacen = Memoria(SesionGuardada("vigente", "demo@example.invalid", false))
            val repo = SesionRepositorio(almacen)
            val api = conectar(servidor, repo)
            servidor.enqueue(json(perfil))
            repo.restaurar(api)
            val perfilVigente = (repo.estado.value as EstadoSesion.Activa).usuario
            assertEquals(4L, perfilVigente.rolId)
            assertEquals("", perfilVigente.contrasena)
            repo.invalidar("anterior")
            assertEquals("vigente", repo.token())

            servidor.enqueue(MockResponse().setResponseCode(401))
            try { api.misEquipos("", 0); fail("Se esperaba un 401") } catch (error: retrofit2.HttpException) {
                assertEquals(401, error.code())
            }
            assertTrue(repo.estado.value is EstadoSesion.Cerrada)
            assertNull(almacen.datos)
        }
    }

    @Test
    fun conservaSesionDuranteCaidaYPermiteReintentar() = runBlocking {
        MockWebServer().also { it.start(java.net.InetAddress.getByName("127.0.0.1"), 0) }.use { servidor ->
            val almacen = Memoria(SesionGuardada("vigente", "demo@example.invalid", false))
            val repo = SesionRepositorio(almacen)
            val api = conectar(servidor, repo)
            servidor.enqueue(MockResponse().setResponseCode(503))
            repo.restaurar(api)
            assertTrue(repo.estado.value is EstadoSesion.SinConexion)
            assertNotNull(almacen.datos)
            servidor.enqueue(json(perfil))
            repo.restaurar(api)
            assertTrue(repo.estado.value is EstadoSesion.Activa)
        }
    }

    @Test
    fun generaClavesTemporalesUnicasConElFormatoDelServidor() {
        val claves = List(24) { generarClaveTemporal() }
        assertEquals(claves.size, claves.toSet().size)
        assertTrue(claves.all { it.length in 6..18 && it.any(Char::isUpperCase) && it.contains('!') })
    }

    @Test
    fun descartaTokenCuandoUsuariosDeniegaLaRestauracion() = runBlocking {
        MockWebServer().also { it.start(java.net.InetAddress.getByName("127.0.0.1"), 0) }.use { servidor ->
            val almacen = Memoria(SesionGuardada("vigente", "demo@example.invalid", false))
            val repo = SesionRepositorio(almacen)
            servidor.enqueue(MockResponse().setResponseCode(403))

            repo.restaurar(conectar(servidor, repo))

            assertEquals(EstadoSesion.Cerrada("La sesión venció. Vuelve a iniciar sesión."), repo.estado.value)
            assertNull(repo.token())
            assertNull(almacen.datos)
        }
    }

    @Test
    fun actualizaPerfilYConservaLaSesionSiRechazanLaClaveActual() = runBlocking {
        MockWebServer().also { it.start(java.net.InetAddress.getByName("127.0.0.1"), 0) }.use { servidor ->
            val repo = SesionRepositorio(Memoria())
            val api = conectar(servidor, repo)
            servidor.enqueue(json("""{"token":"vigente","requiereCambioContrasena":false,"usuario":$perfil}"""))
            servidor.enqueue(json(perfil.replace("Cliente\",\"rut", "Cliente actualizado\",\"rut")))
            servidor.enqueue(MockResponse().setResponseCode(400))

            repo.entrar(api, "demo@example.invalid", "clave-prueba")
            repo.actualizarPerfil(api)
            assertEquals("Cliente actualizado", (repo.estado.value as EstadoSesion.Activa).usuario.nombre)
            try {
                repo.cambiarContrasena(api, CambioContrasena("incorrecta", "Nueva!09", "Nueva!09"))
                fail("El cambio debía rechazarse")
            } catch (error: retrofit2.HttpException) {
                assertEquals(400, error.code())
            }
            assertEquals("vigente", repo.token())
            assertTrue(repo.estado.value is EstadoSesion.Activa)
        }
    }

    @Test
    fun unaRespuestaDePerfilTardiaNoRestauraUnaSesionCerrada() = runBlocking {
        MockWebServer().also { it.start(java.net.InetAddress.getByName("127.0.0.1"), 0) }.use { servidor ->
            val almacen = Memoria(SesionGuardada("vigente", "demo@example.invalid", false))
            val repo = SesionRepositorio(almacen)
            val api = conectar(servidor, repo)
            servidor.enqueue(json(perfil))
            repo.restaurar(api)
            checkNotNull(servidor.takeRequest(5, TimeUnit.SECONDS))

            servidor.enqueue(json(perfil).setBodyDelay(500, TimeUnit.MILLISECONDS))
            val peticion = async { repo.actualizarPerfil(api) }
            val solicitud = withContext(Dispatchers.IO) { servidor.takeRequest(5, TimeUnit.SECONDS) }
            assertEquals("/api/usuarios/me", solicitud?.path)
            repo.cerrar()
            peticion.await()

            assertNull(repo.token())
            assertNull(almacen.datos)
            assertEquals(EstadoSesion.Cerrada(), repo.estado.value)
        }
    }

    private fun json(cadena: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(cadena)
    private fun conectar(servidor: MockWebServer, repo: SesionRepositorio) =
        RetrofitClient.crear(repo::token, "http://127.0.0.1:${servidor.port}/", true, repo::invalidar)

    private class Memoria(var datos: SesionGuardada? = null) : AlmacenSesion {
        override fun leer() = datos
        override fun guardar(sesion: SesionGuardada?) { datos = sesion }
    }
}
