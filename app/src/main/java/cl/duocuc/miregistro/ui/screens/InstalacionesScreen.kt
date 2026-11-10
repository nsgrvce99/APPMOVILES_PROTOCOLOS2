package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.MenuLateral
import cl.duocuc.miregistro.ui.components.SelectorDesplegable
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalacionesScreen(
    usuarioActual: Usuario?,
    onSeleccionarInstalacion: (Long, String) -> Unit,
    onIrAFormalizacion: () -> Unit,
    onCerrarSesion: () -> Unit,
    onVerMiPerfil: () -> Unit,
    onVerUsuarios: () -> Unit,
    onIrAInstrumentos: () -> Unit,
    onIrACrearCliente: () -> Unit,
    onIrACrearInstalacion: () -> Unit,
    onIrARegistrarCliente: () -> Unit // 👈 Asegúrate de tener este parámetro
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var instalaciones by remember { mutableStateOf(dbHelper.listarInstalaciones()) }
    var instalacionSeleccionada by remember { mutableStateOf<Pair<Long, String>?>(null) }

    MenuLateral(
        usuarioActual = usuarioActual,
        onCerrarSesion = onCerrarSesion,
        onVerMiPerfil = onVerMiPerfil,
        onVerUsuarios = onVerUsuarios
    ) { onAbrirMenu, snackbarHostState ->
        Scaffold(
            topBar = {
                AppTopBar(
                    titulo = "Induztek - Selección de Planta",
                    onAbrirMenu = onAbrirMenu
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenido, ${usuarioActual?.nombre ?: "Operador Terreno"} (${usuarioActual?.rolNombre ?: "Técnico"})",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Seleccione la instalación eléctrica a inspeccionar:")

                SelectorDesplegable(
                    etiqueta = "Plantas",
                    opciones = instalaciones,
                    seleccionada = instalacionSeleccionada,
                    onSeleccionar = { (id, nombre) ->
                        instalacionSeleccionada = Pair(id, nombre)
                        onSeleccionarInstalacion(id, nombre)
                    },
                    textoDe = { it.second }
                )

                // 👇 1. PERMISO PARA FORMALIZAR (Ingeniero y Jefe Técnico)
                if (usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L) {
                    Button(onClick = onIrAFormalizacion, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Ir a Formalización de Protocolos")
                    }
                }

                // 👇 2. PERMISOS PARA CREAR (Técnico, Ingeniero y Jefe - Anexo 7)
                val puedeCrear = usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L
                if (puedeCrear) {
                    Button(onClick = onIrACrearInstalacion, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Crear Instalación (RF-01)")
                    }
                    // 👇 NUEVO BOTÓN PARA REGISTRAR EMPRESA CLIENTE
                    Button(onClick = onIrARegistrarCliente, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Registrar Empresa Cliente (RF-01)")
                    }
                }

                // 👇 3. PERMISOS EXCLUSIVOS DEL JEFE TÉCNICO (Instrumentos y Cuentas)
                if (usuarioActual?.rolId == 3L) {
                    Button(onClick = onIrAInstrumentos, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Gestionar Instrumentos (RN-12)")
                    }
                    // Este botón ahora es para ACTIVAR la cuenta, no para crearla desde cero
                    Button(onClick = onIrACrearCliente, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Gestionar Cuentas de Cliente (RN-20)")
                    }
                }

                Spacer(modifier = Modifier.padding(bottom = 16.dp))
            }
        }
    }
}