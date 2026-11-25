package cl.duocuc.miregistro

import cl.duocuc.miregistro.network.RetrofitClient
import cl.duocuc.miregistro.viewmodel.RegistroPruebaViewModel
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroPruebaViewModelTest {
    @Before fun configurar() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun restaurar() = Dispatchers.resetMain()

    @Test
    fun conservaUmbralesDeLasCincoPruebas() = runBlocking {
        MockWebServer().also { it.start(java.net.InetAddress.getByName("127.0.0.1"), 0) }.use { servidor ->
            val vm = iniciar(servidor)
            vm.onTipoPruebaCambia("AISLACION")
            vm.actualizarLectura1Min("10")
            assertEquals("Pendiente", vm.uiState.value.resultadoPrueba)
            vm.actualizarLectura10Min("20")
            assertEquals("Aprobado", vm.uiState.value.resultadoPrueba)
            vm.actualizarLectura10Min("19")
            assertEquals("Precaucion", vm.uiState.value.resultadoPrueba)
            vm.actualizarLectura10Min("8")
            assertEquals("Rechazado", vm.uiState.value.resultadoPrueba)
            vm.onTipoPruebaCambia("TTR")
            vm.actualizarLectura1Min("100")
            vm.actualizarLectura10Min("100.5")
            assertEquals("Aprobado", vm.uiState.value.resultadoPrueba)
            vm.actualizarLectura10Min("100.8")
            assertEquals("Precaucion", vm.uiState.value.resultadoPrueba)
            vm.actualizarLectura10Min("103")
            assertEquals("Rechazado", vm.uiState.value.resultadoPrueba)
            vm.onTipoPruebaCambia("CONTACTOS")
            vm.actualizarLectura10Min("100")
            vm.actualizarLectura1Min("100")
            assertEquals("Aprobado", vm.uiState.value.resultadoPrueba)
            vm.actualizarLectura1Min("120")
            assertEquals("Precaucion", vm.uiState.value.resultadoPrueba)
            vm.actualizarLectura1Min("121")
            assertEquals("Rechazado", vm.uiState.value.resultadoPrueba)
            vm.onTipoPruebaCambia("RELE")
            vm.actualizarTiempoEsperado("0")
            vm.actualizarTiempoMedido("100")
            assertEquals("Pendiente", vm.uiState.value.resultadoPrueba)
            vm.actualizarTiempoEsperado("100")
            assertEquals("Aprobado", vm.uiState.value.resultadoPrueba)
            vm.actualizarTiempoMedido("106")
            assertEquals("Precaucion", vm.uiState.value.resultadoPrueba)
            vm.actualizarTiempoMedido("115")
            assertEquals("Rechazado", vm.uiState.value.resultadoPrueba)
            vm.onTipoPruebaCambia("DEVANADOS")
            vm.actualizarDevanadoA("10")
            vm.actualizarDevanadoB("11")
            vm.actualizarDevanadoC("12")
            vm.actualizarCorrienteAjuste("")
            vm.actualizarCorrienteOperacion("")
            assertEquals("Pendiente de revisión manual", vm.uiState.value.resultadoPrueba)
        }
    }

    @Test
    fun enviaUnaSolaSolicitudAtomicaConMedicionesYPermiteReintentar() = runBlocking {
        MockWebServer().also { it.start(java.net.InetAddress.getByName("127.0.0.1"), 0) }.use { servidor ->
            val vm = iniciar(servidor)
            vm.guardarProtocolo(7, 999)
            assertNotNull(vm.uiState.value.errorTemperatura)
            vm.actualizarTemperatura("21.5")
            vm.actualizarHumedad("101")
            vm.guardarProtocolo(7, 999)
            assertNotNull(vm.uiState.value.errorMedicion)
            vm.onInstrumentoSeleccionado(vm.instrumentos.value.single())
            vm.guardarProtocolo(7, 999)
            assertEquals(1, servidor.requestCount)
            vm.actualizarHumedad("55")
            vm.actualizarLectura1Min("10")
            vm.actualizarLectura10Min("20")

            servidor.enqueue(MockResponse().setResponseCode(409))
            vm.guardarProtocolo(7, 999)
            withTimeout(5_000) { vm.uiState.first { it.errorMedicion != null && !it.guardando } }
            assertFalse(vm.uiState.value.guardadoExitoso)
            servidor.enqueue(MockResponse().setBody("""
                {"id":8,"equipoId":7,"tecnicoId":2,"fecha":"2026-11-25T12:00:00","temperatura":21.5,
                 "humedad":55,"resultadoGlobal":"APROBADO","estado":"BORRADOR"}
            """.trimIndent()))
            vm.guardarProtocolo(7, 999)
            assertEquals(8L, withTimeout(5_000) { vm.uiState.first { it.guardadoExitoso } }.protocoloGuardadoId)
            servidor.takeRequest()
            servidor.takeRequest()
            val solicitud = servidor.takeRequest()
            assertEquals("/api/protocolos", solicitud.path)
            val datos = JsonParser.parseString(solicitud.body.readUtf8()).asJsonObject
            assertFalse(datos.has("tecnicoId"))
            assertEquals(7L, datos["equipoId"].asLong)
            assertEquals(20, datos["valores"].asJsonObject["lectura10Min"].asInt)
        }
    }

    @Test
    fun recuperaLaMedicionObservadaYGuardaPorLaRutaDeEdicion() = runBlocking {
        MockWebServer().also { it.start(java.net.InetAddress.getByName("127.0.0.1"), 0) }.use { servidor ->
            val vm = iniciar(servidor)
            val protocolo = """
                {"id":8,"equipoId":7,"tecnicoId":2,"fecha":"2026-11-25T12:00:00",
                 "temperatura":21.500,"humedad":55.000,"resultadoGlobal":"APROBADO",
                 "estado":"OBSERVADO","motivoObservacion":"Verificar lectura"}
            """.trimIndent()
            servidor.enqueue(MockResponse().setBody("""
                {"protocolo":$protocolo,"mediciones":[{"id":4,"tipoPrueba":"AISLACION",
                    "instrumentoId":3,"valores":{"lectura1Min":10.00000000,"lectura10Min":20.00000000}}],
                 "textoIngresado":{"humedad":"55.0","lectura1Min":"10.00"}}
            """.trimIndent()))
            servidor.enqueue(MockResponse().setBody("""
                {"id":3,"nombre":"Instrumento","marca":"Prueba","serie":"QA-3",
                 "fechaVencimiento":"30-12-2099","calibracion":"VIGENTE"}
            """.trimIndent()))

            vm.cargarProtocoloParaEditar(8)
            assertFalse(vm.uiState.value.cargandoEdicion)
            assertEquals("Verificar lectura", vm.uiState.value.motivoObservacion)
            assertEquals("21.5", vm.uiState.value.temperaturaAmbiente)
            assertEquals("55.0", vm.uiState.value.humedadRelativa)
            assertEquals("10.00", vm.uiState.value.lectura1Min)
            assertEquals("20", vm.uiState.value.lectura10Min)
            assertEquals(3L, vm.instrumentoSeleccionado.value?.id)
            vm.actualizarLectura10Min("23")
            servidor.enqueue(MockResponse().setBody(protocolo.replace("OBSERVADO", "BORRADOR")))
            vm.guardarProtocolo(7, 999, esEdicion = true)
            assertEquals(8L, withTimeout(5_000) { vm.uiState.first { it.guardadoExitoso } }.protocoloGuardadoId)

            repeat(3) { servidor.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS) }
            val solicitud = checkNotNull(servidor.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS))
            assertEquals("PUT", solicitud.method)
            assertEquals("/api/protocolos/8", solicitud.path)
            val datos = JsonParser.parseString(solicitud.body.readUtf8()).asJsonObject
            assertEquals(23, datos["valores"].asJsonObject["lectura10Min"].asInt)
            assertEquals("55.0", datos["textoIngresado"].asJsonObject["humedad"].asString)
            assertEquals("10.00", datos["textoIngresado"].asJsonObject["lectura1Min"].asString)
        }
    }

    private suspend fun iniciar(servidor: MockWebServer): RegistroPruebaViewModel {
        servidor.enqueue(MockResponse().setBody("""
            {"content":[{"id":3,"nombre":"Instrumento","marca":"Prueba","serie":"QA-3",
                         "fechaVencimiento":"30-12-2099","calibracion":"VIGENTE"}],
             "page":{"number":0,"size":20,"totalElements":1,"totalPages":1}}
        """.trimIndent()))
        val vm = RegistroPruebaViewModel(null, RetrofitClient.crear({ "prueba" }, "http://127.0.0.1:${servidor.port}/", true))
        withTimeout(5_000) { vm.instrumentos.first { it.isNotEmpty() } }
        return vm
    }
}
