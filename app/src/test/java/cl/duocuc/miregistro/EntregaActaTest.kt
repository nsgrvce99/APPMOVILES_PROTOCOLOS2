package cl.duocuc.miregistro

import cl.duocuc.miregistro.network.RetrofitClient
import cl.duocuc.miregistro.network.confirmarEntregaActa
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class EntregaActaTest {
    private val acta = """{"id":9,"protocoloId":7,"nombreReceptor":"QA","rutReceptor":"12-3","cargoReceptor":"Jefe","fechaHoraRecepcion":"2026-11-25T12:00:00"}"""

    @Test
    fun reintentaLaEntregaSinVolverACerrarYReutilizaElActaPersistida() = runBlocking {
        MockWebServer().use { servidor ->
            servidor.start(InetAddress.getByName("127.0.0.1"), 0)
            val api = RetrofitClient.crear({ "qa" }, "http://127.0.0.1:${servidor.port}/", true)
            servidor.enqueue(MockResponse().setBody(detalle("BORRADOR")))
            servidor.enqueue(MockResponse().setBody(protocolo("REGISTRADO_TERRENO")))
            servidor.enqueue(MockResponse().setResponseCode(503))
            val error = runCatching { api.confirmarEntregaActa(7) }.exceptionOrNull()
            assertEquals(503, (error as? HttpException)?.code())

            servidor.enqueue(MockResponse().setBody(detalle("REGISTRADO_TERRENO")))
            servidor.enqueue(MockResponse().setResponseCode(201).setBody(acta))
            assertEquals(9L, api.confirmarEntregaActa(7).id)

            servidor.enqueue(MockResponse().setBody(detalle("FORMALIZADO", acta)))
            assertEquals(9L, api.confirmarEntregaActa(7).id)
            val llamadas = List(6) {
                val solicitud = checkNotNull(servidor.takeRequest(3, TimeUnit.SECONDS))
                "${solicitud.method} ${solicitud.path}"
            }
            assertEquals(listOf(
                "GET /api/protocolos/7", "PATCH /api/protocolos/7/cerrar", "POST /api/protocolos/7/acta",
                "GET /api/protocolos/7", "POST /api/protocolos/7/acta", "GET /api/protocolos/7"
            ), llamadas)
            assertEquals(6, servidor.requestCount)
        }
    }

    private fun detalle(estado: String, recepcion: String = "null") =
        """{"protocolo":${protocolo(estado)},"mediciones":[],"acta":$recepcion}"""

    private fun protocolo(estado: String) =
        """{"id":7,"equipoId":2,"tecnicoId":1,"fecha":"2026-11-25T12:00:00","temperatura":20,"humedad":50,"resultadoGlobal":"APROBADO","estado":"$estado"}"""
}
