package cl.duocuc.miregistro.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import cl.duocuc.miregistro.model.LoginViewModel
import cl.duocuc.miregistro.model.LoginViewModelFactory
import cl.duocuc.miregistro.ui.screens.*
import cl.duocuc.miregistro.viewmodel.UsuariosViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val dbHelper = UsuarioDbHelper(context)
    val sesionPrefs = SesionPreferencias(context)

    val usuariosViewModel: UsuariosViewModel = viewModel()
    val usuarioActual by usuariosViewModel.usuarioActual.collectAsStateWithLifecycle()

    // Recuperar usuario si hay sesión activa
    LaunchedEffect(Unit) {
        if (sesionPrefs.haySesionActiva()) {
            val usuarioGuardado = dbHelper.buscarPorId(sesionPrefs.leer())
            if (usuarioGuardado != null) {
                usuariosViewModel.iniciarSesionComo(usuarioGuardado)
            }
        }
    }

    // 👇 CAMBIO: El cliente ahora inicia en "Mis Equipos"
    val startDestination = if (sesionPrefs.haySesionActiva()) {
        if (sesionPrefs.leerRol() == 4L) Ruta.MisEquiposCliente.ruta else Ruta.Instalaciones.ruta
    } else {
        Ruta.Login.ruta
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // ---- LOGIN ----
        composable(Ruta.Login.ruta) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(dbHelper, sesionPrefs)
            )
            LoginClienteScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { rolId ->
                    val usuarioLogueado = dbHelper.buscarPorId(sesionPrefs.leer())
                    if (usuarioLogueado != null) {
                        usuariosViewModel.iniciarSesionComo(usuarioLogueado)
                    }

                    // 👇 CAMBIO: Si es cliente, va a "Mis Equipos"
                    val destino = if (rolId == 4L) Ruta.MisEquiposCliente.ruta else Ruta.Instalaciones.ruta
                    navController.navigate(destino) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                },
                onRegistroClick = { }
            )
        }

        // ---- NUEVA PANTALLA: MIS EQUIPOS (Rol 4) ----
        composable(Ruta.MisEquiposCliente.ruta) {
            MisEquiposClienteScreen(
                usuarioActual = usuarioActual,
                onVolver = {
                    // Al volver desde aquí, se cierra sesión
                    sesionPrefs.borrar()
                    usuariosViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerHistorialEquipo = { equipoId ->
                    navController.navigate(Ruta.HistorialEquipo.crear(equipoId))
                }
            )
        }

        // ---- INSTALACIONES ----
        composable(Ruta.Instalaciones.ruta) {
            InstalacionesScreen(
                usuarioActual = usuarioActual,
                onSeleccionarInstalacion = { instalacionId ->
                    navController.navigate(Ruta.ListaEquipos.crear(instalacionId))
                },
                onIrAFormalizacion = {
                    navController.navigate(Ruta.Formalizacion.ruta)
                },
                onCerrarSesion = {
                    sesionPrefs.borrar()
                    usuariosViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ---- LISTA DE EQUIPOS ----
        composable(
            route = Ruta.ListaEquipos.ruta,
            arguments = listOf(navArgument("instalacionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val instalacionId = backStackEntry.arguments?.getLong("instalacionId") ?: 0L
            ListaEquiposScreen(
                instalacionId = instalacionId,
                onVolver = { navController.navigateUp() },
                onSeleccionarEquipoParaPrueba = { equipoId ->
                    navController.navigate(Ruta.RegistroPrueba.crear(equipoId))
                },
                onVerHistorialEquipo = { equipoId ->
                    navController.navigate(Ruta.HistorialEquipo.crear(equipoId))
                },
                onRegistrarEquipoNuevo = {
                    navController.navigate(Ruta.RegistrarEquipo.crear(instalacionId))
                }
            )
        }
        // ---- REGISTRAR EQUIPO NUEVO (HU-01) ----
        composable(
            route = Ruta.RegistrarEquipo.ruta,
            arguments = listOf(navArgument("instalacionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val instalacionId = backStackEntry.arguments?.getLong("instalacionId") ?: 0L
            RegistrarEquipoScreen(
                instalacionId = instalacionId,
                onVolver = { navController.navigateUp() },
                onEquipoRegistrado = {
                    navController.popBackStack(Ruta.ListaEquipos.crear(instalacionId), inclusive = false)
                }
            )
        }

        // ---- REGISTRO DE PRUEBA ----
        composable(
            route = Ruta.RegistroPrueba.ruta,
            arguments = listOf(navArgument("equipoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getLong("equipoId") ?: 0L
            RegistroPruebaScreen(
                equipoId = equipoId,
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() },
                onPruebaGuardadaExitosamente = { protocoloId ->
                    navController.navigate(Ruta.CierreVisita.crear(protocoloId)) {
                        popUpTo(Ruta.RegistroPrueba.crear(equipoId)) { inclusive = true }
                    }
                }
            )
        }

        // ---- HISTORIAL EQUIPO ----
        composable(
            route = Ruta.HistorialEquipo.ruta,
            arguments = listOf(navArgument("equipoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getLong("equipoId") ?: 0L
            HistorialEquipoScreen(
                equipoId = equipoId,
                onVolver = { navController.navigateUp() }
            )
        }

        // ---- FORMALIZACIÓN ----
        composable(Ruta.Formalizacion.ruta) {
            FormalizacionScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }

        // ---- CIERRE DE VISITA ----
        composable(
            route = Ruta.CierreVisita.ruta,
            arguments = listOf(navArgument("protocoloId") { type = NavType.LongType })
        ) { backStackEntry ->
            val protocoloId = backStackEntry.arguments?.getLong("protocoloId") ?: 0L
            CierreVisitaScreen(
                protocoloId = protocoloId,
                onVolver = { navController.navigateUp() },
                onActaEntregada = {
                    navController.popBackStack(Ruta.Instalaciones.ruta, inclusive = false)
                }
            )
        }

    }
}