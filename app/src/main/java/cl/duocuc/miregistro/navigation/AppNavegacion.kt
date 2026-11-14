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
                    val destino = if (rolId == 4L) Ruta.MisEquiposCliente.ruta else Ruta.Instalaciones.ruta
                    navController.navigate(destino) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                },
                onRegistroClick = { }
            )
        }

        // ---- PERFIL ----
        composable(Ruta.Perfil.ruta) {
            PerfilScreen(
                usuario = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }

        // ---- CLIENTE (Rol 4) ----
        composable(Ruta.MisEquiposCliente.ruta) {
            MisEquiposClienteScreen(
                usuarioActual = usuarioActual,
                onVolver = {
                    sesionPrefs.borrar()
                    usuariosViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                },
                onVerHistorialEquipo = { equipoId ->
                    navController.navigate(Ruta.HistorialEquipo.crear(equipoId))
                },
                onCerrarSesion = {
                    sesionPrefs.borrar()
                    usuariosViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                },
                onVerMiPerfil = { navController.navigate(Ruta.Perfil.ruta) }
            )
        }

        // ---- INSTALACIONES ----
        composable(Ruta.Instalaciones.ruta) {
            InstalacionesScreen(
                usuarioActual = usuarioActual,
                onSeleccionarInstalacion = { instalacionId, nombre ->
                    navController.navigate(Ruta.ListaEquipos.crear(instalacionId, nombre))
                },
                onIrAFormalizacion = { navController.navigate(Ruta.Formalizacion.ruta) },
                onCerrarSesion = {
                    sesionPrefs.borrar()
                    usuariosViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                },
                onVerMiPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onVerUsuarios = { navController.navigate(Ruta.ListaEquipos.crear(1L, "Planta Lampa")) },
                onIrAInstrumentos = { navController.navigate(Ruta.GestionInstrumentos.ruta) },
                onIrACrearCliente = { navController.navigate(Ruta.CrearCliente.ruta) },
                onIrACrearInstalacion = { navController.navigate(Ruta.CrearInstalacion.ruta) },
                onIrARegistrarCliente = { navController.navigate(Ruta.RegistrarCliente.ruta) },
                onVerMisPruebas = { navController.navigate(Ruta.MisProtocolos.ruta) },
                onVerListaClientes = { navController.navigate(Ruta.ListaClientes.ruta) },

                onVerResumenVisita = { navController.navigate(Ruta.ResumenVisita.ruta) }
            )
        }

        // ---- LISTA DE EQUIPOS ----
        composable(
            route = Ruta.ListaEquipos.ruta,
            arguments = listOf(
                navArgument("instalacionId") { type = NavType.LongType },
                navArgument("nombreInstalacion") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val instalacionId = backStackEntry.arguments?.getLong("instalacionId") ?: 0L
            val nombreInstalacion = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("nombreInstalacion") ?: "", "UTF-8"
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

        // ---- REGISTRAR EQUIPO ----
        composable(
            route = Ruta.RegistrarEquipo.ruta,
            arguments = listOf(
                navArgument("instalacionId") { type = NavType.LongType },
                navArgument("nombreInstalacion") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val instalacionId = backStackEntry.arguments?.getLong("instalacionId") ?: 0L
            val nombreInstalacion = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("nombreInstalacion") ?: "", "UTF-8"
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

        // ---- REGISTRO PRUEBA ----
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
                usuarioActual = usuarioActual,
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

        // ---- CIERRE VISITA ----
        composable(
            route = Ruta.CierreVisita.ruta,
            arguments = listOf(navArgument("protocoloId") { type = NavType.LongType })
        ) { backStackEntry ->
            val protocoloId = backStackEntry.arguments?.getLong("protocoloId") ?: 0L
            CierreVisitaScreen(
                protocoloId = protocoloId,
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() },
                onActaEntregada = {
                    navController.popBackStack(Ruta.Instalaciones.ruta, inclusive = false)
                }
            )
        }

        // ---- RESUMEN VISITA ----
        composable(Ruta.ResumenVisita.ruta) {
            ResumenVisitaScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }

        // ---- MIS PROTOCOLOS ----
        composable(Ruta.MisProtocolos.ruta) {
            MisProtocolosScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() },
                onEditarProtocolo = { protocoloId ->
                    navController.navigate(Ruta.EditarProtocolo.crear(protocoloId))
                },
                onVerDetalleProtocolo = { protocoloId ->
                    navController.navigate(Ruta.DetalleProtocolo.crear(protocoloId))
                }
            )
        }

        // ---- EDITAR PROTOCOLO ----
        composable(
            route = Ruta.EditarProtocolo.ruta,
            arguments = listOf(navArgument("protocoloId") { type = NavType.LongType })
        ) { backStackEntry ->
            val protocoloId = backStackEntry.arguments?.getLong("protocoloId") ?: 0L
            val protocolo = dbHelper.obtenerProtocoloPorId(protocoloId)
            val equipoId = protocolo?.equipoId ?: 0L

            RegistroPruebaScreen(
                equipoId = equipoId,
                usuarioActual = usuarioActual,
                protocoloId = protocoloId,
                onVolver = { navController.navigateUp() },
                onPruebaGuardadaExitosamente = {
                    navController.navigate(Ruta.MisProtocolos.ruta) {
                        popUpTo(Ruta.MisProtocolos.ruta) { inclusive = true }
                    }
                }
            )
        }

        // ---- GESTIÓN INSTRUMENTOS ----
        composable(Ruta.GestionInstrumentos.ruta) {
            GestionInstrumentosScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }

        // ---- CREAR CLIENTE ----
        composable(Ruta.CrearCliente.ruta) {
            CrearUsuarioClienteScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() },
                onEditarCliente = { cliente ->
                    navController.navigate(Ruta.EditarCliente.crear(cliente.id))
                }
            )
        }

        // ---- EDITAR CLIENTE ----
        composable(
            route = Ruta.EditarCliente.ruta,
            arguments = listOf(navArgument("clienteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getLong("clienteId") ?: 0L
            val cliente = dbHelper.buscarPorId(clienteId)
            if (cliente != null) {
                EditarClienteScreen(
                    cliente = cliente,
                    onVolver = { navController.navigateUp() },
                    onGuardadoExitoso = { navController.navigateUp() }
                )
            }
        }

        // ---- LISTA CLIENTES ----
        composable(Ruta.ListaClientes.ruta) {
            ListaClientesScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() },
                onEditarCliente = { cliente ->
                    navController.navigate(Ruta.EditarCliente.crear(cliente.id))
                }
            )
        }

        // ---- CREAR INSTALACION ----
        composable(Ruta.CrearInstalacion.ruta) {
            CrearInstalacionScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }

        // ---- REGISTRAR EMPRESA CLIENTE ----
        composable(Ruta.RegistrarCliente.ruta) {
            RegistrarClienteScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }

        // ---- DETALLE PROTOCOLO ----
        composable(
            route = Ruta.DetalleProtocolo.ruta,
            arguments = listOf(navArgument("protocoloId") { type = NavType.LongType })
        ) { backStackEntry ->
            val protocoloId = backStackEntry.arguments?.getLong("protocoloId") ?: 0L
            DetalleProtocoloScreen(
                protocoloId = protocoloId,
                onVolver = { navController.navigateUp() }
            )
        }

    }
}