package cl.duocuc.miregistro

import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Equipo
import cl.duocuc.miregistro.model.Protocolo
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.viewmodel.HistorialEquipoViewModel
import cl.duocuc.miregistro.viewmodel.RegistroPruebaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class UsuarioDbHelperTest {
    private lateinit var db: UsuarioDbHelper

    @Before fun abrir() { db = UsuarioDbHelper(RuntimeEnvironment.getApplication()) }
    @After fun cerrar() { db.close() }

    @Test
    fun activaClienteYConservaIdentidadAlActualizarPerfil() {
        val id = altaCliente()
        assertNull(db.autenticarUsuario("cliente-qa@example.test", "temporal"))
        assertFalse(db.buscarPorId(id)!!.activo)
        assertTrue(db.existeCorreo("cliente-qa@example.test"))
        assertTrue(db.existeRut("12000002-7"))

        db.activarCliente(id, "autorizada")
        assertEquals(id, db.autenticarUsuario("cliente-qa@example.test", "autorizada")?.id)
        db.actualizarDatosCliente(id, "QA renovado", "12000002-7", "Energía", "nuevo-qa@example.test")
        db.cambiarContrasena(id, "actualizada")
        db.actualizarFotoUsuario(id, "content://qa/foto")
        assertNull(db.autenticarUsuario("nuevo-qa@example.test", "autorizada"))
        val actual = db.autenticarUsuario("nuevo-qa@example.test", "actualizada")!!
        assertEquals("QA renovado", actual.nombre)
        assertEquals("content://qa/foto", actual.fotoUri)
        assertEquals(id, db.listarClientesPaginado("nuevo-qa", 1, 0).single().id)
        assertTrue(db.listarClientes().any { it.id == id })
        db.eliminarCliente(id)
        assertNull(db.buscarPorId(id))
    }

    @Test
    fun filtraEquiposPorTitularYPaginaPorInstalacion() {
        val cliente = altaCliente()
        val planta = db.insertarInstalacion("QA terreno", cliente)
        assertTrue(planta > 0)
        assertEquals(listOf(planta to "QA terreno"), db.listarInstalacionesPaginado("QA terreno", 1, 0))
        assertTrue((planta to "QA terreno") in db.listarInstalaciones())
        val equipo = altaEquipo(cliente)
        assertTrue(db.existeTagEnInstalacion("QA-TR", "QA terreno"))
        assertFalse(db.existeTagEnInstalacion("QA-TR", "Otra planta"))
        assertEquals(cliente, db.obtenerIdClienteDeInstalacion("QA terreno"))
        assertEquals(equipo, db.buscarEquiposPorCliente(cliente, "QA-TR").single().id.toLong())
        assertTrue(db.buscarEquiposPorCliente(5, "QA-TR").isEmpty())
        assertEquals(equipo, db.listarEquiposPorInstalacion("QA terreno").single().id.toLong())
        val bloqueo = runCatching { db.eliminarInstalacion("QA terreno") }
        assertTrue(bloqueo.exceptionOrNull()?.message.orEmpty().contains("equipos registrados"))
        db.eliminarEquipo(equipo)
        assertNull(db.buscarEquipoPorId(equipo))
        db.eliminarInstalacion("QA terreno")
        assertTrue(db.listarInstalacionesPaginado("QA terreno", 1, 0).isEmpty())
    }

    @Test
    fun conservaActaHistorialYProtegeElProtocoloOficial() {
        val cliente = altaCliente()
        val equipo = altaEquipo(cliente)
        val instrumento = db.insertarInstrumento("Medidor QA", "Marca QA", "QA01", "31-12-2099")
        assertTrue(instrumento > 0)
        assertFalse(db.instrumentoTieneMediciones(instrumento))
        val borrador = altaProtocolo(equipo, "2026-11-12")
        assertTrue(db.guardarMedicion(borrador, "AISLACION", "100", "200", "2", instrumento) > 0)
        assertTrue(db.instrumentoTieneMediciones(instrumento))
        assertFalse(db.tieneVariacionSignificativa(borrador))
        assertTrue(db.listarProtocolosParaHistorialPaginado(equipo, true, 10, 0).isEmpty())

        db.cerrarBorrador(borrador)
        assertEquals("REGISTRADO_TERRENO", db.obtenerProtocoloPorId(borrador)?.estado)
        assertTrue(db.listarProtocolosPendientesDetallados().any { it.id == borrador })
        assertTrue(db.guardarRecepcionActa(borrador, "QA", "12000002-7", "Supervisor", "2026-11-12") > 0)
        db.formalizarProtocoloConComentario(borrador, "Acta recibida")
        val posterior = altaProtocolo(equipo, "2026-11-13")
        assertTrue(db.guardarMedicion(posterior, "AISLACION", "140", "210", "1.5", instrumento) > 0)
        assertTrue(db.tieneVariacionSignificativa(posterior))
        assertEquals(cliente, db.obtenerClienteDeProtocolo(borrador)?.id)
        assertEquals("QA-TR", db.obtenerEquipoDeProtocolo(borrador)?.tag)
        assertEquals(listOf(posterior, borrador), HistorialEquipoViewModel(db, equipo).protocolos.value.map { it.id })
        assertEquals(listOf(borrador), db.listarProtocolosParaHistorialPaginado(equipo, true, 10, 0).map { it.id })
        assertEquals("Acta recibida", db.obtenerProtocoloPorId(borrador)?.comentarioVariacion)
        assertTrue(runCatching { db.eliminarProtocolo(borrador) }.isFailure)
        assertTrue(runCatching { db.eliminarEquipo(equipo) }.isFailure)
        assertTrue(runCatching { db.eliminarCliente(cliente) }.isFailure)

        db.eliminarProtocolo(posterior)
        assertNull(db.obtenerMedicionDeProtocolo(posterior))
        assertEquals(listOf(borrador), db.listarProtocolosPorEquipo(equipo).map { it.id })
    }

    @Test
    fun actualizaCatalogoDeInstrumentosYBuscaPorMarca() {
        val id = db.insertarInstrumento("QA B", "Marca propia", "200", "31-12-2099")
        val anterior = db.insertarInstrumento("QA A", "Marca propia", "201", "31-12-2099")
        assertEquals(anterior, db.listarInstrumentosPaginado("Marca propia", 1, 0).single().id)
        assertEquals(id, db.listarInstrumentosPaginado("Marca propia", 1, 1).single().id)
        assertFalse(db.estaVencido("31-12-2099"))
        assertTrue(db.estaVencido("01-01-2000"))
        assertFalse(db.estaPorVencer("fecha-inválida"))
        db.actualizarInstrumento(id, "QA nuevo", "Otra marca", "252", "30-12-2099")
        assertEquals("252", db.listarInstrumentos().single { it.id == id }.serie)
        db.eliminarInstrumento(id)
        assertTrue(db.listarInstrumentosPaginado("QA nuevo", 1, 0).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun registraYReeditaUnaPruebaLocalConInstrumentoVigente() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val equipo = altaEquipo(altaCliente())
            val instrumento = db.insertarInstrumento("Megóhmetro QA", "QA", "QA12", "31-12-2099")
            val vencido = db.insertarInstrumento("Vencido QA", "QA", "QA13", "01-01-2000")
            val vm = RegistroPruebaViewModel(db)
            vm.actualizarTemperatura("23")
            vm.actualizarHumedad("50")
            vm.actualizarLectura1Min("100")
            vm.actualizarLectura10Min("220")
            vm.onInstrumentoSeleccionado(vm.instrumentos.value.single { it.id == vencido })
            vm.guardarProtocolo(equipo, 1)
            assertTrue(vm.uiState.value.errorMedicion.orEmpty().contains("vencido"))
            assertTrue(db.listarProtocolosPorEquipo(equipo).isEmpty())

            vm.onInstrumentoSeleccionado(vm.instrumentos.value.single { it.id == instrumento })
            vm.guardarProtocolo(equipo, 1)
            val id = checkNotNull(vm.uiState.value.protocoloGuardadoId)
            assertEquals("220", db.obtenerMedicionDeProtocolo(id)?.lectura10Min)
            db.observarProtocolo(id, "Revisar")
            vm.cargarProtocoloParaEditar(id)
            assertEquals("Revisar", vm.uiState.value.motivoObservacion)
            vm.actualizarLectura10Min("230")
            vm.guardarProtocolo(equipo, 1, esEdicion = true)

            assertEquals(id, db.listarProtocolosPorEquipo(equipo).single().id)
            assertEquals("230", db.obtenerMedicionDeProtocolo(id)?.lectura10Min)
            assertEquals("REGISTRADO_TERRENO", db.obtenerProtocoloPorId(id)?.estado)
            assertEquals(id, db.listarProtocolosPorTecnicoDetalladosPaginado(1, 1, 0).single().id)
            val resumen = db.obtenerResumenVisita(1)
            assertEquals(1, resumen.totalPruebas)
            assertEquals(1, resumen.aprobadas)
            assertEquals(1, resumen.pendientesFormalizar)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun actualizaDatosDelPersonalYLoMantieneEnElListadoInterno() {
        val cliente = altaCliente()
        val id = db.insertarUsuario(
            Usuario(rut = "12000001-9", nombre = "QA especialista", correo = "interno-qa@example.test",
                contrasena = "temporal", rolId = 1)
        )
        assertTrue(id > 0)
        assertEquals(id, db.listarPersonalInternoPaginado("interno-qa", 1, 0).single().id)
        db.actualizarPersonalInterno(id, "QA ingeniero", "12000001-9", "qa@interno.example.test", 2)
        val actualizado = db.buscarPorId(id)
        assertEquals(2L, actualizado?.rolId)
        assertEquals("qa@interno.example.test", actualizado?.correo)
        assertFalse(db.listarPersonalInternoPaginado("QA", 20, 0).any { it.id == cliente })
        assertEquals(1, db.eliminarUsuario(id))
        assertNull(db.buscarPorId(id))
    }

    private fun altaCliente() = db.insertarUsuario(
        Usuario(rut = "12000002-7", nombre = "Cliente QA", correo = "cliente-qa@example.test",
            contrasena = "temporal", rolId = 4, activo = false, giro = "Pruebas")
    ).also { assertTrue(it > 0) }

    private fun altaEquipo(cliente: Long) = db.insertarEquipo(
        Equipo(tag = "QA-TR", tipoEquipo = "Transformador", instalacion = "QA terreno",
            marca = "QA", serie = "01", idCliente = cliente)
    ).also { assertTrue(it > 0) }

    private fun altaProtocolo(equipo: Long, fecha: String) = db.guardarProtocolo(
        Protocolo(equipoId = equipo, tecnicoId = 1, fecha = fecha, temperatura = "20",
            humedad = "40", resultadoGlobal = "Aprobado", estado = "BORRADOR")
    ).also { assertTrue(it > 0) }
}
