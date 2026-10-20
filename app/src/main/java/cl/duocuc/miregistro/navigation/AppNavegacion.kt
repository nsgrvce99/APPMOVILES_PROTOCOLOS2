package cl.duocuc.miregistro.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duocuc.miregistro.data.SesionPreferencias
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.ui.screens.*
import cl.duocuc.miregistro.viewmodel.LoginViewModel
import cl.duocuc.miregistro.viewmodel.LoginViewModelFactory
import cl.duocuc.miregistro.viewmodel.UsuariosViewModel

// =====================================================================
//  AppNavegacion.kt  —  MAPA DE NAVEGACIÓN COMPLETO (Induztek)
// =====================================================================

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // 1. Inicializar Preferencias y Base de Datos
    val dbHelper = UsuarioDbHelper(context)
    val sesionPrefs = SesionPreferencias(context)

    val usuariosViewModel: UsuariosViewModel = viewModel()
    val usuarioActual by usuariosViewModel.usuarioActual.collectAsStateWithLifecycle()

    // 2. Determinar destino inicial según sesión activa y rol
    val startDestination = if (sesionPrefs.haySesionActiva()) {
        if (sesionPrefs.leerRol() == 4L) Ruta.HistorialCliente.ruta else Ruta.Instalaciones.ruta
    } else {
        Ruta.Login.ruta
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // --- RUTAS DE AUTENTICACIÓN Y CLIENTES ---

        composable(Ruta.Login.ruta) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(dbHelper, sesionPrefs)
            )

            LoginClienteScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { rolId ->
                    val destino = if (rolId == 4L) Ruta.HistorialCliente.ruta else Ruta.Instalaciones.ruta
                    navController.navigate(destino) {
                        // Evita que el usuario vuelva al login presionando el botón de retroceso
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                },
                onRegistroClick = { /* Opcional */ }
            )
        }

        composable(Ruta.HistorialCliente.ruta) {
            HistorialEquipoScreen(
                equipoId = 1, // Puedes hacerlo dinámico después
                onVolver = {
                    // Ejemplo: Usar el botón de volver para cerrar sesión
                    sesionPrefs.borrar()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- RUTAS OPERATIVAS (TRABAJADORES INDUZTEK) ---

        // 1. Pantalla de Selección de Planta / Faena (Instalaciones)
        composable(Ruta.Instalaciones.ruta) {
            InstalacionesScreen(
                usuarioActual = usuarioActual,
                onSeleccionarInstalacion = { instalacionId ->
                    navController.navigate(Ruta.ListaEquipos.crear(instalacionId))
                },
                onIrAFormalizacion = {
                    navController.navigate(Ruta.Formalizacion.ruta)
                }
            )
        }

        // 2. Lista de Equipos o TAGs en la instalación seleccionada
        composable(
            route = Ruta.ListaEquipos.ruta,
            arguments = listOf(navArgument("instalacionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val instalacionId = backStackEntry.arguments?.getInt("instalacionId") ?: 0
            ListaEquiposScreen(
                instalacionId = instalacionId,
                onVolver = { navController.navigateUp() },
                onSeleccionarEquipoParaPrueba = { equipoId ->
                    navController.navigate(Ruta.RegistroPrueba.crear(equipoId))
                },
                onVerHistorialEquipo = { equipoId ->
                    navController.navigate(Ruta.HistorialEquipo.crear(equipoId))
                }
            )
        }

        // 3. Registro de Pruebas en Terreno (RF-02, RN-04)
        composable(
            route = Ruta.RegistroPrueba.ruta,
            arguments = listOf(navArgument("equipoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getInt("equipoId") ?: 0
            RegistroPruebaScreen(
                equipoId = equipoId,
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() },
                onPruebaGuardadaExitosamente = {
                    navController.popBackStack() // Vuelve a la lista de equipos tras guardar
                }
            )
        }

        // 4. Historial del Equipo (RF-10)
        composable(
            route = Ruta.HistorialEquipo.ruta,
            arguments = listOf(navArgument("equipoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getInt("equipoId") ?: 0
            HistorialEquipoScreen(
                equipoId = equipoId,
                onVolver = { navController.navigateUp() }
            )
        }

        // 5. Formalización de Protocolos (Exclusivo Rol Ingeniero / Jefe Técnico)
        composable(Ruta.Formalizacion.ruta) {
            FormalizacionScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }
    }
}