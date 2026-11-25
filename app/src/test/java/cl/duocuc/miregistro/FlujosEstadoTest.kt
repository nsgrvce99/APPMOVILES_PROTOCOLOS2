package cl.duocuc.miregistro

import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.network.MetadatosPagina
import cl.duocuc.miregistro.network.OperacionUi
import cl.duocuc.miregistro.network.Pagina
import cl.duocuc.miregistro.network.todasLasPaginas
import cl.duocuc.miregistro.viewmodel.PerfilViewModel
import cl.duocuc.miregistro.viewmodel.UsuariosViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FlujosEstadoTest {
    @Test
    fun conservaIdentidadDePerfilYAplicaPermisosAlCambiarRol() {
        val usuarios = UsuariosViewModel()
        assertFalse(usuarios.puedeRegistrarPrueba())
        val tecnico = Usuario(id = 42, rut = "12000002-7", nombre = "QA", correo = "qa@example.test",
            contrasena = "temporal", rolId = 1)
        usuarios.registrar(tecnico)
        assertTrue(usuarios.puedeRegistrarPrueba())
        assertFalse(usuarios.puedeFormalizar())
        val ingeniero = tecnico.copy(rolId = 2)
        usuarios.actualizar(0, ingeniero)
        assertEquals(ingeniero, usuarios.usuarios.value.single())
        assertTrue(usuarios.puedeFormalizar())
        usuarios.iniciarSesionComo(tecnico.copy(rolId = 4))
        assertTrue(usuarios.esCliente())
        assertFalse(usuarios.puedeRegistrarPrueba())
        usuarios.cerrarSesion()
        assertNull(usuarios.usuarioActual.value)

        val perfil = PerfilViewModel()
        val nuevo = checkNotNull(perfil.uiState.value.perfil).copy(id = tecnico.id, nombre = tecnico.nombre)
        perfil.cargarPerfil(nuevo)
        assertEquals(nuevo, perfil.uiState.value.perfil)
        assertFalse(perfil.uiState.value.cargando)
    }

    @Test
    fun recorreTodasLasPaginasYRespetaElFinal() = runTest {
        val solicitadas = mutableListOf<Int>()
        val datos = todasLasPaginas { pagina ->
            solicitadas.add(pagina)
            Pagina(listOf("equipo-$pagina"), MetadatosPagina(size = 1, number = pagina, totalElements = 3, totalPages = 3))
        }
        assertEquals(listOf(0, 1, 2), solicitadas)
        assertEquals(listOf("equipo-0", "equipo-1", "equipo-2"), datos)
    }

    @Test
    fun evitaDobleEnvioYPermiteReintentarTrasError() = runTest {
        val operacion = OperacionUi(this)
        val liberar = CompletableDeferred<Unit>()
        operacion.ejecutar {
            liberar.await()
            throw IOException("Simulación de interrupción")
        }
        assertTrue(operacion.ejecutando)
        operacion.ejecutar { fail("La operación en curso debe bloquear el segundo envío") }
        liberar.complete(Unit)
        runCurrent()
        assertFalse(operacion.ejecutando)
        assertNotNull(operacion.error)

        var reintentos = 0
        operacion.ejecutar { reintentos++ }
        runCurrent()
        assertEquals(1, reintentos)
        assertNull(operacion.error)
        assertFalse(operacion.ejecutando)
    }
}
