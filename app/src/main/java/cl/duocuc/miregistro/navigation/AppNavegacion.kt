package cl.duocuc.miregistro.navigation

import cl.duocuc.miregistro.ui.screens.FormalizacionScreen
import cl.duocuc.miregistro.ui.screens.HistorialEquipoScreen
import cl.duocuc.miregistro.ui.screens.InstalacionesScreen
import cl.duocuc.miregistro.ui.screens.ListaEquiposScreen
import cl.duocuc.miregistro.ui.screens.RegistroPruebaScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duocuc.miregistro.ui.screens.EditarScreen
import cl.duocuc.miregistro.ui.screens.FormularioScreen
import cl.duocuc.miregistro.ui.screens.PerfilScreen
import cl.duocuc.miregistro.ui.screens.UsuariosScreen
import cl.duocuc.miregistro.viewmodel.UsuariosViewModel

// =====================================================================
//  AppNavegacion.kt  —  MAPA DE NAVEGACIÓN COMPLETO (Induztek)
// =====================================================================

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    val usuariosViewModel: UsuariosViewModel = viewModel()
    val usuarioActual by usuariosViewModel.usuarioActual.collectAsStateWithLifecycle()

    // Usamos Ruta.Instalaciones como punto de partida oficial del caso Induztek
    NavHost(navController = navController, startDestination = Ruta.Instalaciones.ruta) {

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
