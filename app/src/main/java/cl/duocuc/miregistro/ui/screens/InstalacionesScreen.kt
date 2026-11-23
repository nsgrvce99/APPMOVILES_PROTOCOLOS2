package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AcordeonSeccion
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.FondoFooter
import cl.duocuc.miregistro.ui.components.MenuLateral
import cl.duocuc.miregistro.ui.components.SelectorDesplegablePaginado
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
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var instalacionesParaGestionar by remember { mutableStateOf(dbHelper.listarInstalaciones()) }

    var expandirInstalaciones by remember { mutableStateOf(false) }
    var expandirGestionActivos by remember { mutableStateOf(false) }
    var expandirReportes by remember { mutableStateOf(false) }
    var expandirAdmin by remember { mutableStateOf(false) }

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
                    titulo = "Induztek - Panel General",
                    onAbrirMenu = onAbrirMenu
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            FondoFooter(
                modifier = Modifier.padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(Dimens.espacioPantalla),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    Text(
                        text = "Bienvenido, ${usuarioActual?.nombre ?: "Operador"}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rol: ${usuarioActual?.rolNombre ?: "Técnico"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AcordeonSeccion(
                        titulo = "Instalaciones",
                        icono = Icons.Default.LocationOn,
                        expandido = expandirInstalaciones,
                        onToggle = { expandirInstalaciones = !expandirInstalaciones }
                    ) {
                        Text(
                            text = "Seleccione la planta a inspeccionar:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
                    }

                    val puedeCrear = usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L
                    if (puedeCrear) {
                        AcordeonSeccion(
                            titulo = "Gestión de Activos",
                            icono = Icons.Default.Build,
                            expandido = expandirGestionActivos,
                            onToggle = { expandirGestionActivos = !expandirGestionActivos }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = onIrACrearInstalacion,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Crear Instalación.")
                                }
                                OutlinedButton(
                                    onClick = onIrARegistrarCliente,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Registrar Cliente.")
                                }
                                OutlinedButton(
                                    onClick = onVerListaClientes,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Editar Clientes")
                                }
                                Button(
                                    onClick = {
                                        instalacionesParaGestionar = dbHelper.listarInstalaciones()
                                        mostrarDialogoGestionPlantas = true
                                        mensajeError = null
                                        mensajeExito = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Gestionar Plantas (Eliminar)")
                                }
                            }
                        }
                    }

                    if (usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L) {
                        AcordeonSeccion(
                            titulo = "Reportes y Pruebas",
                            icono = Icons.Default.Assessment,
                            expandido = expandirReportes,
                            onToggle = { expandirReportes = !expandirReportes }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L) {
                                    Button(
                                        onClick = onIrAFormalizacion,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Ir a Formalización de Protocolos")
                                    }
                                }

                                if (usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L) {
                                    Button(
                                        onClick = onVerMisPruebas,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Ver Mis Pruebas Registradas")
                                    }
                                    OutlinedButton(
                                        onClick = onVerResumenVisita,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Ver Resumen de Visita.")
                                    }
                                }
                            }
                        }
                    }

                    if (usuarioActual?.rolId == 3L) {
                        AcordeonSeccion(
                            titulo = "Administración",
                            icono = Icons.Default.AdminPanelSettings,
                            expandido = expandirAdmin,
                            onToggle = { expandirAdmin = !expandirAdmin }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onIrAInstrumentos,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Gestionar Instrumentos.")
                                }
                                OutlinedButton(
                                    onClick = onIrACrearCliente,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Gestionar Cuentas de Cliente.")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (mostrarDialogoGestionPlantas) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoGestionPlantas = false
                mensajeExito = null
            },
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
                        instalacionesParaGestionar.forEach { (id, nombre) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(nombre, modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = {
                                        try {
                                            dbHelper.eliminarInstalacion(nombre)
                                            instalacionesParaGestionar = dbHelper.listarInstalaciones()
                                            if (instalacionSeleccionada?.second == nombre) {
                                                instalacionSeleccionada = null
                                            }
                                            mensajeError = null
                                            mensajeExito = "Planta eliminada correctamente"
                                        } catch (e: Exception) {
                                            mensajeError = e.message
                                            mensajeExito = null
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "Eliminar",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }

                    if (mensajeExito != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = mensajeExito!!,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoGestionPlantas = false
                    mensajeExito = null
                }) { Text("Cerrar") }
            }
        )
    }
}