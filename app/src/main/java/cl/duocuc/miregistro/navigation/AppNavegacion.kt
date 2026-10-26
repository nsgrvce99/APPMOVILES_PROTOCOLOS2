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
import cl.duocuc.miregistro.viewmodel.LoginViewModel
import cl.duocuc.miregistro.viewmodel.LoginViewModelFactory
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


    LaunchedEffect(Unit) {
        if (sesionPrefs.haySesionActiva()) {
            val usuarioGuardado = dbHelper.buscarPorId(sesionPrefs.leer())
            if (usuarioGuardado != null) {
                usuariosViewModel.iniciarSesionComo(usuarioGuardado)
            }
        }
    }

    val startDestination = if (sesionPrefs.haySesionActiva()) {
        if (sesionPrefs.leerRol() == 4L) Ruta.MisEquiposCliente.ruta else Ruta.Instalaciones.ruta
    } else {
        Ruta.Login.ruta
    }

    NavHost(navController = navController, startDestination = startDestination) {


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

                    val destino = if (rolId == 4L) Ruta.MisEquiposCliente.ruta else Ruta.Instalaciones.ruta
                    navController.navigate(destino) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                },
                onRegistroClick = { }
            )
        }


        composable(Ruta.Perfil.ruta) {
            PerfilScreen(
                usuario = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }


        composable(Ruta.MisEquiposCliente.ruta) {
            MisEquiposClienteScreen(
                usuarioActual = usuarioActual,
                onVolver = {

                    sesionPrefs.borrar()
                    usuariosViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerHistorialEquipo = { equipoId ->
                    navController.navigate(Ruta.HistorialEquipo.crear(equipoId))
                },
                onCerrarSesion = {
                    sesionPrefs.borrar()
                    usuariosViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerMiPerfil = {
                    navController.navigate(Ruta.Perfil.ruta)
                }
            )
        }


        composable(Ruta.Instalaciones.ruta) {
            InstalacionesScreen(
                usuarioActual = usuarioActual,
                onSeleccionarInstalacion = { instalacionId, nombre ->
                    navController.navigate(Ruta.ListaEquipos.crear(instalacionId, nombre))
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
                },
                onVerMiPerfil = {
                    navController.navigate(Ruta.Perfil.ruta)
                },
                onVerUsuarios = {
                    navController.navigate(Ruta.ListaEquipos.crear(1L, "Planta Lampa"))
                },
                onIrAInstrumentos = {
                    navController.navigate(Ruta.GestionInstrumentos.ruta)
                },
                onIrACrearCliente = {
                    navController.navigate(Ruta.CrearCliente.ruta)
                },
                onIrACrearInstalacion = {
                    navController.navigate(Ruta.CrearInstalacion.ruta)
                }
            )
        }



        composable(
            route = Ruta.ListaEquipos.ruta,
            arguments = listOf(
                navArgument("instalacionId") { type = NavType.LongType },
                navArgument("nombreInstalacion") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val instalacionId = backStackEntry.arguments?.getLong("instalacionId") ?: 0L
            val nombreInstalacion = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("nombreInstalacion") ?: "",
                "UTF-8"
            )

            ListaEquiposScreen(
                instalacionId = instalacionId,
                nombreInstalacion = nombreInstalacion,
                onVolver = { navController.navigateUp() },
                onSeleccionarEquipoParaPrueba = { equipoId ->
                    navController.navigate(Ruta.RegistroPrueba.crear(equipoId))
                },
                onVerHistorialEquipo = { equipoId ->
                    navController.navigate(Ruta.HistorialEquipo.crear(equipoId))
                },
                onRegistrarEquipoNuevo = {
                    navController.navigate(Ruta.RegistrarEquipo.crear(instalacionId, nombreInstalacion))
                }
            )
        }


        composable(
            route = Ruta.RegistrarEquipo.ruta,
            arguments = listOf(
                navArgument("instalacionId") { type = NavType.LongType },
                navArgument("nombreInstalacion") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val instalacionId = backStackEntry.arguments?.getLong("instalacionId") ?: 0L
            val nombreInstalacion = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("nombreInstalacion") ?: "",
                "UTF-8"
            )
            RegistrarEquipoScreen(
                instalacionId = instalacionId,
                nombreInstalacion = nombreInstalacion,
                onVolver = { navController.navigateUp() },
                onEquipoRegistrado = {
                    navController.popBackStack(
                        Ruta.ListaEquipos.crear(instalacionId, nombreInstalacion),
                        inclusive = false
                    )
                }
            )
        }

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


        composable(Ruta.Formalizacion.ruta) {
            FormalizacionScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }


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
        composable(Ruta.GestionInstrumentos.ruta) {
            GestionInstrumentosScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }
        composable(Ruta.CrearCliente.ruta) {
            CrearUsuarioClienteScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }
        composable(Ruta.CrearInstalacion.ruta) {
            CrearInstalacionScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }

    }
}