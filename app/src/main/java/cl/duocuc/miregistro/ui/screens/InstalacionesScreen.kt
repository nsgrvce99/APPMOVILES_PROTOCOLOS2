package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.MenuLateral
import cl.duocuc.miregistro.ui.components.SelectorDesplegablePaginado // 👈 Nueva importación
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
    onIrARegistrarCliente: () -> Unit,
    onVerMisPruebas: () -> Unit,
    onVerListaClientes: () -> Unit,
    onVerResumenVisita: () -> Unit,
    onCambiarContrasena: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var instalacionSeleccionada by remember { mutableStateOf<Pair<Long, String>?>(null) }

    var mostrarDialogoGestionPlantas by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    // 👇 Mantenemos esta lista SOLO para el diálogo de eliminar plantas (no para el desplegable)
    var instalacionesParaGestionar by remember { mutableStateOf(dbHelper.listarInstalaciones()) }

    MenuLateral(
        usuarioActual = usuarioActual,
        onCerrarSesion = onCerrarSesion,
        onVerMiPerfil = onVerMiPerfil,
        onVerUsuarios = onVerUsuarios,
        onCambiarContrasena = onCambiarContrasena
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

                // 👇 REEMPLAZAMOS EL SELECTOR VIEJO POR EL PAGINADO
                SelectorDesplegablePaginado(
                    etiqueta = "Plantas",
                    itemSeleccionado = instalacionSeleccionada,
                    textoDe = { it.second },
                    onBuscar = { query, limit, offset ->
                        dbHelper.listarInstalacionesPaginado(query, limit, offset)
                    },
                    onItemSeleccionado = { planta ->
                        instalacionSeleccionada = planta
                        onSeleccionarInstalacion(planta.first, planta.second)
                    }
                )

                if (usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L) {
                    Button(onClick = onIrAFormalizacion, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Ir a Formalización de Protocolos")
                    }
                }

                val puedeCrear = usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L
                if (puedeCrear) {
                    Button(onClick = onIrACrearInstalacion, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Crear Instalación (RF-01)")
                    }
                    Button(onClick = onIrARegistrarCliente, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Registrar Empresa Cliente (RF-01)")
                    }
                    Button(onClick = onVerListaClientes, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Editar Empresas Clientes")
                    }
                    Button(
                        onClick = {
                            // Refrescamos la lista antes de abrir el diálogo
                            instalacionesParaGestionar = dbHelper.listarInstalaciones()
                            mostrarDialogoGestionPlantas = true
                            mensajeError = null
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Gestionar Plantas (Eliminar)")
                    }
                }

                if (usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L) {
                    Button(onClick = onVerMisPruebas, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Ver Mis Pruebas Registradas")
                    }
                    Button(onClick = onVerResumenVisita, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Ver Resumen de Visita (RF-18)")
                    }
                }

                if (usuarioActual?.rolId == 3L) {
                    Button(onClick = onIrAInstrumentos, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Gestionar Instrumentos (RN-12)")
                    }
                    Button(onClick = onIrACrearCliente, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Gestionar Cuentas de Cliente (RN-20)")
                    }
                }

                Spacer(modifier = Modifier.padding(bottom = 16.dp))
            }
        }
    }

    if (mostrarDialogoGestionPlantas) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoGestionPlantas = false },
            title = { Text("Gestionar Plantas") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (mensajeError != null) {
                        Text(mensajeError!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (instalacionesParaGestionar.isEmpty()) {
                        Text("No hay plantas registradas.")
                    } else {
                        // 👇 Usamos la lista separada para el diálogo
                        instalacionesParaGestionar.forEach { (id, nombre) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(nombre, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    try {
                                        dbHelper.eliminarInstalacion(nombre)
                                        // Refrescamos la lista del diálogo
                                        instalacionesParaGestionar = dbHelper.listarInstalaciones()
                                        // Limpiamos la selección si se eliminó la planta seleccionada
                                        if (instalacionSeleccionada?.second == nombre) {
                                            instalacionSeleccionada = null
                                        }
                                        mensajeError = null
                                    } catch (e: Exception) {
                                        mensajeError = e.message
                                    }
                                }) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(cl.duocuc.miregistro.R.drawable.ic_volver),
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoGestionPlantas = false }) { Text("Cerrar") }
            }
        )
    }
}