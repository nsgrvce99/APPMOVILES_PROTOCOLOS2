package cl.duocuc.miregistro

import cl.duocuc.miregistro.network.InicioSesion
import cl.duocuc.miregistro.network.RetrofitClient
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RetrofitClientTest {
    @Test
    fun enviaTokenSoloDespuesDelLoginYLeePaginacion() {
        MockWebServer().also { it.start(java.net.InetAddress.getByName("127.0.0.1"), 0) }.use { servidor ->
            servidor.enqueue(MockResponse().setBody("""
                {"token":"nuevo","idUsuario":5,"rolId":4,"requiereCambioContrasena":true,
                 "usuario":{"id":5,"nombre":"Cliente","rut":"1-9","correo":"demo@example.invalid","rolId":4,"activo":true}}
            """.trimIndent()))
            servidor.enqueue(MockResponse().setBody("""
                {"content":[],"page":{"size":10,"number":2,"totalElements":23,"totalPages":3}}
            """.trimIndent()))
            val api = RetrofitClient.crear({ "jwt-prueba" }, "http://127.0.0.1:${servidor.port}/", permitirHttp = true)

            runBlocking {
                val sesion = api.login(InicioSesion("demo@example.invalid", "clave-prueba"))
                assertEquals("nuevo", sesion.token)
                assertEquals(2, api.misEquipos("TR-01", 2).page.number)
            }

            val login = servidor.takeRequest()
            assertEquals("/api/auth/login", login.path)
            assertNull(login.getHeader("Authorization"))
            val consulta = servidor.takeRequest()
            assertEquals("/api/equipos/mis-equipos?q=TR-01&page=2", consulta.path)
            assertEquals("Bearer jwt-prueba", consulta.getHeader("Authorization"))
        }
    }

    @Test
    fun elClientePuedeUsarseComoLlaveDeCargaEnCompose() {
        val api = RetrofitClient.crear({ null }, "http://10.0.2.2:8080", permitirHttp = true)
        assertTrue(api.equals(api))
    }

    @Test
    fun admiteLaDireccionLocalPredeterminadaYUnaIpDeLaLan() {
        RetrofitClient.crear({ null }, "http://10.0.2.2:8080", permitirHttp = true)
        RetrofitClient.crear({ null }, "http://192.168.1.20:8080", permitirHttp = true)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaHttpPublicoTambienEnModoAcademico() {
        RetrofitClient.crear({ null }, "http://example.test", permitirHttp = true)
    }

    @Test(expected = IllegalArgumentException::class)
    fun exigeHttpsEnProduccion() {
        RetrofitClient.crear({ null }, "http://10.0.2.2:8080", permitirHttp = false)
    }
}