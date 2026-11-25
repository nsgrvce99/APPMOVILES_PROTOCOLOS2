package cl.duocuc.miregistro

import cl.duocuc.miregistro.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun configurar() = Dispatchers.setMain(dispatcher)
    @After fun restaurar() = Dispatchers.resetMain()

    @Test
    fun exigeDatosYEvitaEnviarDosVecesMientrasCarga() = runTest(dispatcher) {
        var intentos = 0
        var rol: Long? = null
        val vm = LoginViewModel { correo, clave ->
            intentos++
            assertEquals("demo@example.invalid", correo)
            assertEquals("Clave! Demo ", clave)
            LoginViewModel.ResultadoIngreso(4, false)
        }
        vm.autenticar({ rol = it }, {})
        assertNotNull(vm.uiState.value.errorMensaje)
        vm.onCorreoChanged("  demo@example.invalid ")
        vm.onContrasenaChanged("Clave! Demo ")
        vm.onAlternarVisibilidadContrasena()
        assertTrue(vm.uiState.value.contrasenaVisible)
        vm.autenticar({ rol = it }, {})
        assertTrue(vm.uiState.value.cargando)
        vm.autenticar({ fail("La segunda consulta no debe ejecutarse") }, {})
        advanceUntilIdle()
        assertEquals(1, intentos)
        assertEquals(4L, rol)
        assertFalse(vm.uiState.value.cargando)
        assertEquals("", vm.uiState.value.contrasena)
    }

    @Test
    fun muestraErrorDeConexionYLlevaAlCambioCuandoLaClaveEsTemporal() = runTest(dispatcher) {
        var falla = true
        var temporal = false
        val vm = LoginViewModel { _, _ ->
            if (falla) throw IOException("privado")
            LoginViewModel.ResultadoIngreso(1, true)
        }
        vm.onCorreoChanged("demo@example.invalid")
        vm.onContrasenaChanged("Clave!Demo")
        vm.autenticar({ fail("No debe entrar") }, { temporal = true })
        advanceUntilIdle()
        assertFalse(temporal)
        assertTrue(checkNotNull(vm.uiState.value.errorMensaje).contains("conectar"))
        falla = false
        vm.autenticar({ fail("Debe cambiar la contraseña") }, { temporal = true })
        advanceUntilIdle()
        assertTrue(temporal)
        assertNull(vm.uiState.value.errorMensaje)
    }
}
