package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.model.FormularioUiState
import cl.duocuc.miregistro.model.NivelExperiencia
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.BotonPrincipal
import cl.duocuc.miregistro.ui.components.BotonSecundario
import cl.duocuc.miregistro.ui.components.CampoContrasena
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.components.CasillaConTexto
import cl.duocuc.miregistro.ui.components.GrupoRadio
import cl.duocuc.miregistro.ui.components.ImagenEncabezado
import cl.duocuc.miregistro.ui.components.InterruptorConTexto
import cl.duocuc.miregistro.ui.components.TarjetaSeccion
import cl.duocuc.miregistro.ui.styles.EstilosCampo
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.ui.theme.Exito
import cl.duocuc.miregistro.ui.theme.ExitoFondo
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme
import cl.duocuc.miregistro.viewmodel.FormularioViewModel
import kotlinx.coroutines.launch

// =====================================================================
//  FormularioScreen.kt  —  PANTALLA 1: FORMULARIO DE REGISTRO  (la "V")
//  ---------------------------------------------------------------------
//  Árbol de CONTENEDORES de esta pantalla:
//
//  Scaffold                      <- esqueleto: barra superior + contenido
//   └─ Column (scroll)           <- todo en vertical, con desplazamiento
//       ├─ ImagenEncabezado      <- Box: imagen + degradado + textos apilados
//       └─ Column (padding)      <- zona del formulario
//           ├─ TarjetaSeccion    <- Card "Datos personales" (3 campos)
//           ├─ TarjetaSeccion    <- Card "Preferencias" (radio + switch)
//           ├─ CasillaConTexto   <- Row: checkbox + texto
//           ├─ Row               <- dos botones lado a lado (weight)
//           └─ Card (si éxito)   <- mensaje verde
//
//  La pantalla se divide en dos funciones:
//   1) FormularioScreen  -> CON estado: conoce el ViewModel.
//   2) FormularioContent -> SIN estado: recibe datos y lambdas.
//      Se puede ver en @Preview sin ViewModel.
//
//  CAMBIO (Bloque 1): nuevo parámetro onRegistroExitoso. La pantalla NO
//  navega: solo AVISA "me registré". AppNavegacion decide a dónde ir.
//
//  CAMBIO (Bloque 2): onRegistroExitoso entrega el Usuario y se agregó
//  la lista desplegable de ciudad (ExposedDropdownMenuBox).
//
//  CAMBIO (Bloque 3): MENÚ LATERAL (☰, "hamburguesa") con ModalNavigationDrawer.
//    ModalNavigationDrawer   <- envuelve la pantalla
//     ├─ drawerContent        <- el panel que se desliza desde la izquierda
//     │   └─ ModalDrawerSheet + NavigationDrawerItem x N
//     └─ contenido            <- FormularioContent (la pantalla de siempre)
//  Las opciones dependen de la SESIÓN: "Mi perfil" solo aparece si
//  usuarioActual != null. La pantalla no "recuerda" nada: dibuja lo que
//  el estado le dice.
//
//  CAMBIO (Bloque 4): opción "Cerrar sesión" en el menú ☰.
//   - AlertDialog: interrumpe y pide CONFIRMAR antes de cerrar la sesión.
//   - Snackbar:    aviso breve abajo ("Sesión cerrada"); NO interrumpe.
// =====================================================================

@Composable
fun FormularioScreen(
    onRegistroExitoso: (Usuario) -> Unit,
    usuarioActual: Usuario?,
    onVerMiPerfil: () -> Unit,
    onVerUsuarios: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: FormularioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var mostrarDialogo by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val textoSesionCerrada = stringResource(R.string.snackbar_sesion_cerrada)

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(stringResource(R.string.dialogo_titulo)) },
            text = { Text(stringResource(R.string.dialogo_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    onCerrarSesion()
                    scope.launch { snackbarHostState.showSnackbar(textoSesionCerrada) }
                }) { Text(stringResource(R.string.menu_cerrar_sesion)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text(stringResource(R.string.dialogo_cancelar))
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Encabezado del menú adaptado para Induztek
                Text(
                    text = if (usuarioActual != null) stringResource(R.string.saludo, usuarioActual.nombre)
                    else "Induztek Terreno",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
                HorizontalDivider()

                if (usuarioActual != null) {
                    NavigationDrawerItem(
                        label = { Text("Mi Perfil / Datos de Técnico") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onVerMiPerfil()
                        }
                    )
                }

                NavigationDrawerItem(
                    label = { Text("Consultar Equipos e Historial (RF-10)") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onVerUsuarios()
                    }
                )

                if (usuarioActual != null) {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_cerrar_sesion)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            mostrarDialogo = true
                        }
                    )
                }
            }
        }
    ) {
        FormularioContent(
            uiState = uiState,
            onNombreCambia = viewModel::onNombreCambia,
            onCorreoCambia = viewModel::onCorreoCambia,
            onContrasenaCambia = viewModel::onContrasenaCambia,
            onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
            onNivelSeleccionado = viewModel::onNivelSeleccionado,
            onNoticiasCambia = viewModel::onNoticiasCambia,
            onTerminosCambia = viewModel::onTerminosCambia,
            onRegistrar = { viewModel.onRegistrar()?.let { usuario -> onRegistroExitoso(usuario) } },
            onLimpiar = viewModel::onLimpiar,
            onCiudadSeleccionada = viewModel::onCiudadSeleccionada,
            usuarioActual = usuarioActual,
            onAbrirMenu = { scope.launch { drawerState.open() } },
            snackbarHostState = snackbarHostState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContent(
    uiState: FormularioUiState,
    onNombreCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onNivelSeleccionado: (NivelExperiencia) -> Unit,
    onNoticiasCambia: (Boolean) -> Unit,
    onTerminosCambia: (Boolean) -> Unit,
    onRegistrar: () -> Unit,
    onLimpiar: () -> Unit,
    onCiudadSeleccionada: (String) -> Unit = {},
    usuarioActual: Usuario? = null,
    onAbrirMenu: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (usuarioActual != null) "Protocolo Terreno - ${usuarioActual.nombre}"
                        else "Induztek - Registro en Terreno"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = stringResource(R.string.cd_menu)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ImagenEncabezado(
                imagen = R.drawable.banner_registro,
                descripcion = stringResource(R.string.cd_banner),
                titulo = "Formalización de Pruebas",
                subtitulo = "Induztek Ingeniería SpA - Offline Mode"
            )

            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                // ---------- Card 1: Identificación del Equipo y Ubicación (RF-02) ----------
                TarjetaSeccion(titulo = "1. Identificación del Equipo (RF-02)") {
                    CampoTexto(
                        valor = uiState.nombre, // Reutilizado para TAG del equipo (ej: TR-01)
                        onValorCambia = onNombreCambia,
                        etiqueta = "TAG del Equipo (ej. TR-01)",
                        icono = R.drawable.ic_persona,
                        error = uiState.errorNombre
                    )
                    CampoTexto(
                        valor = uiState.correo, // Reutilizado para número de serie / placa
                        onValorCambia = onCorreoCambia,
                        etiqueta = "Nº de Serie / Marca",
                        icono = R.drawable.ic_correo,
                        error = uiState.errorCorreo
                    )

                    // Selector de tipo de equipo (Transformador, Relé, Celda, etc.)
                    var expandido by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expandido,
                        onExpandedChange = { expandido = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.ciudad, // Reutilizado para tipo de equipo / instalación
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Instalación / Tipo de Equipo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                            shape = EstilosCampo.forma,
                            colors = EstilosCampo.colores(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expandido,
                            onDismissRequest = { expandido = false }
                        ) {
                            uiState.ciudades.forEach { instalacion ->
                                DropdownMenuItem(
                                    text = { Text(instalacion) },
                                    onClick = {
                                        onCiudadSeleccionada(instalacion)
                                        expandido = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ---------- Card 2: Condiciones Ambientales obligatorias (RN-04) ----------
                TarjetaSeccion(titulo = "2. Condiciones Ambientales (Obligatorias RN-04)") {
                    CampoTexto(
                        valor = uiState.contrasena, // Reutilizado para Temperatura Amb. (°C)
                        onValorCambia = onContrasenaCambia,
                        etiqueta = "Temperatura Ambiente (°C)",
                        icono = R.drawable.ic_candado,
                        error = uiState.errorContrasena,
                        tipoTeclado = KeyboardType.Number
                    )
                    Text(
                        text = "Asegure registrar la humedad relativa y temperatura antes de pasar a 'Registrado en terreno'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // ---------- Card 3: Opciones y Cierre de Visita ----------
                TarjetaSeccion(titulo = "3. Opciones de Visita y Mantenimiento") {
                    Text(
                        text = "Tipo de Servicio en Terreno:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    GrupoRadio(
                        opciones = NivelExperiencia.entries,
                        seleccionada = uiState.nivel,
                        onSeleccionar = onNivelSeleccionado,
                        textoDe = {
                            when(it) {
                                NivelExperiencia.PRINCIPIANTE -> "Solo Pruebas"
                                NivelExperiencia.INTERMEDIO -> "Pruebas y Mantenimiento"
                                NivelExperiencia.AVANZADO -> "Revisión Extraordinaria"
                            }
                        }
                    )
                    InterruptorConTexto(
                        texto = "Instrumento con calibración vigente verificada (RN-11)",
                        activado = uiState.recibirNoticias,
                        onCambio = onNoticiasCambia
                    )
                }

                // ---------- Términos / Conformidad del Acta (checkbox) + error ----------
                Column {
                    CasillaConTexto(
                        texto = "Conforme con Acta preliminar de prueba en terreno (Pendiente de formalización)",
                        marcado = uiState.aceptaTerminos,
                        onCambio = onTerminosCambia
                    )
                    uiState.errorTerminos?.let { mensaje ->
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Dimens.espacioChico)
                        )
                    }
                }

                // ---------- ROW: Botones de Acción ----------
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                    BotonSecundario(
                        texto = "Limpiar",
                        onClick = onLimpiar,
                        modifier = Modifier.weight(1f)
                    )
                    BotonPrincipal(
                        texto = "Registrar Protocolo",
                        onClick = onRegistrar,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ---------- Mensaje de éxito ----------
                if (uiState.registroExitoso) {
                    MensajeExito(nombre = uiState.nombre)
                }

                Spacer(Modifier.height(Dimens.espacioGrande))
            }
        }
    }
}

@Composable
private fun MensajeExito(nombre: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ExitoFondo, contentColor = Exito)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Exito,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
            Text(
                text = "Protocolo guardado exitosamente para el equipo TAG: $nombre (Pendiente de formalización oficina)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = Dimens.espacioChico)
            )
        }
    }
}

// ----------------------------- PREVIEWS -----------------------------

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FormularioVacioInduztekPreview() {
    MiRegistroTheme {
        FormularioContent(
            uiState = FormularioUiState(),
            onNombreCambia = {}, onCorreoCambia = {}, onContrasenaCambia = {},
            onAlternarVisibilidad = {}, onNivelSeleccionado = {}, onNoticiasCambia = {},
            onTerminosCambia = {}, onRegistrar = {}, onLimpiar = {}
        )
    }
}