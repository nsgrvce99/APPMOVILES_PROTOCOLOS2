package cl.duocuc.miregistro

import cl.duocuc.miregistro.network.*
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class ContratosTerrenoTest {
    @Test
    fun paginaSinInventarRegistrosYRespetaCabeceraDelServidor() = runBlocking {
        val pedidos = mutableListOf<Int>()
        val datos = todasLasPaginas { pagina ->
            pedidos += pagina
            Pagina(listOf("equipo-$pagina"), MetadatosPagina(1, pagina, 3, 3))
        }
        assertEquals(listOf(0, 1, 2), pedidos)
        assertEquals(listOf("equipo-0", "equipo-1", "equipo-2"), datos)
        assertEquals(emptyList<String>(), todasLasPaginas {
            Pagina(emptyList<String>(), MetadatosPagina(20, 0, 0, 0))
        })
    }

    @Test
    fun conservaMedicionesYLaClasificacionEnLasTarjetas() {
        val medicion = MedicionRemota(1, "RELE", 3, null, LecturasRemotas(
            corrienteAjuste = BigDecimal("1.20"), tiempoEsperado = BigDecimal("100.00"),
            tiempoMedido = BigDecimal("103.00")
        )).aMedicion(9)
        assertEquals(9L, medicion.protocoloId)
        assertEquals("1.20", medicion.corrienteAjuste)
        assertEquals("", medicion.lectura1Min)

        val resumen = ProtocoloRemoto(9, 4, 7, "2026-11-25T12:00:00", "20", "55",
            "PRECAUCION", "OBSERVADO", "Revisar", null)
        val detalle = DetalleRemoto(resumen, emptyList(), EquipoReferencia(4, 5, "TR-4", "Transformador", "Planta"),
            null, PersonaRemota(7, "Técnico Demo"), null).aTarjeta()
        assertEquals("TR-4", detalle.equipoTag)
        assertEquals("2026-11-25 12:00", detalle.fecha)
        assertEquals("Precaucion", detalle.resultadoGlobal)
        assertEquals("Técnico Demo", detalle.tecnicoNombre)
        assertEquals("Revisar", detalle.motivoObservacion)
        val esperado = mapOf("APROBADO" to "Aprobado", "RECHAZADO" to "Rechazado",
            "REVISION_MANUAL" to "Pendiente de revisión manual", "PENDIENTE" to "Pendiente", "Otro" to "Otro")
        esperado.forEach { (estado, etiqueta) ->
            assertEquals(etiqueta, resumen.copy(resultadoGlobal = estado).aProtocolo().resultadoGlobal)
        }
    }

    @Test
    fun propagaConflictosYSolicitudesSinPermiso() {
        Response.success(Unit).verificar()
        val esperado = mapOf(400 to "datos", 401 to "sesión", 403 to "permiso", 404 to "registro",
            409 to "protegida", 503 to "servicio", 418 to "operación")
        esperado.forEach { (codigo, palabra) ->
            val respuesta = Response.error<Unit>(codigo, "".toResponseBody())
            val error = assertThrows(HttpException::class.java) { respuesta.verificar() }
            assertTrue(mensajeRed(error).contains(palabra))
        }
    }
}
