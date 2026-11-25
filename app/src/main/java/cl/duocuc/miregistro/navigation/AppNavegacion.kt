package cl.duocuc.miregistro.navigation

import cl.duocuc.miregistro.utils.Validaciones

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duocuc.miregistro.BuildConfig
import cl.duocuc.miregistro.network.*
import cl.duocuc.miregistro.viewmodel.SesionGatewayViewModel
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
    if (BuildConfig.DATOS_LOCALES) {
        NavegacionTerreno()
        return
    }
    val (tieneRedLocal, solicitarRedLocal) = recordarPermisoGateway()
    if (!tieneRedLocal) {
        EstadoPortal("Autoriza la red local para comunicarte con el Gateway de desarrollo.", solicitarRedLocal,
            textoAccion = "Permitir acceso a red local")
        return
    }
    val sesionGateway: SesionGatewayViewModel = viewModel()
    val api = sesionGateway.api
    val estado by sesionGateway.estado.collectAsStateWithLifecycle()
    if (api == null) {
        EstadoPortal(sesionGateway.errorConfiguracion ?: "Gateway no disponible")
        return
    }
    when (val actual = estado) {
        EstadoSesion.Restaurando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is EstadoSesion.SinConexion -> EstadoPortal(actual.mensaje, sesionGateway::restaurar, sesionGateway::salir)
        is EstadoSesion.Cerrada -> {
            val login: LoginViewModel = viewModel(key = "login-gateway", factory = viewModelFactory {
                initializer { LoginViewModel(sesionGateway::iniciar) }
            })
            LoginClienteScreen(login, {}, {}, {}, avisoSesion = actual.mensaje)
        }
        EstadoSesion.CambioObligatorio -> Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                CambiarContrasenaScreen(null, {}, {}, esClavePorDefecto = true, cambiarRemoto = sesionGateway::cambiar)
            }
            TextButton(onClick = sesionGateway::salir, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Cerrar sesión")
            }
        }
        is EstadoSesion.Activa -> key(actual.usuario.id, actual.usuario.rolId) {
            CompositionLocalProvider(LocalApiTerreno provides api) {
                NavegacionTerreno(actual.usuario, sesionGateway)
            }
        }
    }
}

@Composable
private fun EstadoPortal(mensaje: String, reintentar: (() -> Unit)? = null, salir: (() -> Unit)? = null, textoAccion: String = "Reintentar") {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(mensaje)
        reintentar?.let { TextButton(onClick = it) { Text(textoAccion) } }
        salir?.let { TextButton(onClick = it) { Text("Cerrar sesión") } }
    }
}

@Composable
private fun NavegacionTerreno(
    usuarioGateway: cl.duocuc.miregistro.model.Usuario? = null,
    sesionGateway: SesionGatewayViewModel? = null
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val api = LocalApiTerreno.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }
    val sesionPrefs = remember(api) { if (api == null) SesionPreferencias(context) else null }

    val usuariosViewModel: UsuariosViewModel = viewModel()
    val usuarioLocal by usuariosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val usuarioActual = usuarioGateway ?: usuarioLocal

    LaunchedEffect(Unit) {
        if (sesionPrefs?.haySesionActiva() == true) {
            val usuarioGuardado = dbHelper?.buscarPorId(checkNotNull(sesionPrefs).leer())
            if (usuarioGuardado != null) {
                usuariosViewModel.iniciarSesionComo(usuarioGuardado)
            }
        }
    }


    val usuarioDbInicial = if (sesionPrefs?.haySesionActiva() == true) dbHelper?.buscarPorId(checkNotNull(sesionPrefs).leer()) else null
    val startDestination = if (usuarioGateway != null) {
        if (usuarioGateway.rolId == 4L) Ruta.MisEquiposCliente.ruta else Ruta.Instalaciones.ruta
    } else if (sesionPrefs?.haySesionActiva() == true) {

        if (usuarioDbInicial != null && Validaciones.esClaveTemporalLocal(usuarioDbInicial.contrasena)) {
            Ruta.CambiarContrasena.crear(esObligatorio = true)
        } else if (sesionPrefs.leerRol() == 4L) {
            Ruta.MisEquiposCliente.ruta
        } else {
            Ruta.Instalaciones.ruta
        }
    } else {
        Ruta.Login.ruta
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // ---- LOGIN ----
        if (api == null) composable(Ruta.Login.ruta) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(checkNotNull(dbHelper), checkNotNull(sesionPrefs))
            )
            LoginClienteScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { rolId ->
                    val usuarioLogueado = dbHelper?.buscarPorId(checkNotNull(sesionPrefs).leer())
                    if (usuarioLogueado != null) {
                        usuariosViewModel.iniciarSesionComo(usuarioLogueado)
                    }
                    val destino = if (rolId == 4L) Ruta.MisEquiposCliente.ruta else Ruta.Instalaciones.ruta
                    navController.navigate(destino) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                },
                onRequiereCambio = {

                    navController.navigate(Ruta.CambiarContrasena.crear(esObligatorio = true)) {
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
                recargarRemoto = sesionGateway?.let { it::recargarPerfil },
                onVolver = { navController.navigateUp() }
            )
        }

        // ---- CAMBIO DE CONTRASEÑA ----
        composable(
            route = Ruta.CambiarContrasena.ruta,
            arguments = listOf(
                navArgument("esObligatorio") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->

            val esObligatorio = backStackEntry.arguments?.getBoolean("esObligatorio") ?: false


            if (esObligatorio) {
                BackHandler {

                }
            }

            CambiarContrasenaScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() },
                onContrasenaCambiada = {
                    val destino = if (usuarioActual?.rolId == 4L) Ruta.MisEquiposCliente.ruta else Ruta.Instalaciones.ruta
                    navController.navigate(destino) {

                        popUpTo(0) { inclusive = true }
                    }
                },
                cambiarRemoto = sesionGateway?.let { it::cambiar },
                esClavePorDefecto = esObligatorio
            )
        }

        // ---- CLIENTE (Rol 4) ----
        composable(Ruta.MisEquiposCliente.ruta) {
            MisEquiposClienteScreen(
                usuarioActual = usuarioActual,
                api = api,
                onVolver = {
                    if (sesionGateway != null) sesionGateway.salir() else {
                        sesionPrefs?.borrar()
                        usuariosViewModel.cerrarSesion()
                        navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                    }
                },
                onVerHistorialEquipo = { equipoId ->
                    navController.navigate(Ruta.HistorialEquipo.crear(equipoId))
                },
                onCerrarSesion = {
                    if (sesionGateway != null) sesionGateway.salir() else {
                        sesionPrefs?.borrar()
                        usuariosViewModel.cerrarSesion()
                        navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                    }
                },
                onVerMiPerfil = { navController.navigate(Ruta.Perfil.ruta) },

                onCambiarContrasena = { navController.navigate(Ruta.CambiarContrasena.crear(esObligatorio = false)) }
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
                    if (sesionGateway != null) sesionGateway.salir() else {
                        sesionPrefs?.borrar()
                        usuariosViewModel.cerrarSesion()
                        navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                    }
                },
                onVerMiPerfil = { navController.navigate(Ruta.Perfil.ruta) },

                onIrAInstrumentos = { navController.navigate(Ruta.GestionInstrumentos.ruta) },
                onIrACrearCliente = { navController.navigate(Ruta.CrearCliente.ruta) },
                onIrACrearInstalacion = { navController.navigate(Ruta.CrearInstalacion.ruta) },
                onIrARegistrarCliente = { navController.navigate(Ruta.RegistrarCliente.ruta) },
                onVerMisPruebas = { navController.navigate(Ruta.MisProtocolos.ruta) },
                onVerListaClientes = { navController.navigate(Ruta.ListaClientes.ruta) },
                onVerResumenVisita = { navController.navigate(Ruta.ResumenVisita.ruta) },

                onCambiarContrasena = { navController.navigate(Ruta.CambiarContrasena.crear(esObligatorio = false)) }
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
                api = api,
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
                onEditarProtocolo = { navController.navigate(Ruta.EditarProtocolo.crear(protocoloId)) },
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
            var revision by remember { mutableIntStateOf(0) }
            val detalle by cargar(protocoloId, api, revision) {
                if (api != null) api.protocolo(protocoloId).protocolo.equipoId
                else dbHelper?.obtenerProtocoloPorId(protocoloId)?.equipoId ?: 0L
            }
            ContenidoRemoto(detalle, { revision++ }) { equipoId ->


            RegistroPruebaScreen(
                equipoId = equipoId,
                usuarioActual = usuarioActual,
                protocoloId = protocoloId,
                onVolver = { navController.navigateUp() },
                onPruebaGuardadaExitosamente = { navController.popBackStack() }
            )
        }

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
            var revision by remember { mutableIntStateOf(0) }
            val carga by cargar(clienteId, revision, api) {
                if (api != null) api.usuario(clienteId).aUsuario() else dbHelper?.buscarPorId(clienteId)
            }
            ContenidoRemoto(carga, { revision++ }) { cliente ->
                if (cliente != null) EditarClienteScreen(
                    cliente = cliente,
                    onVolver = { navController.navigateUp() },
                    onGuardadoExitoso = { navController.navigateUp() }
                ) else EstadoPortal("Cliente no disponible")
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