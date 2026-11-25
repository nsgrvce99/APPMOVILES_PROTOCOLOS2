package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.network.*
import kotlinx.coroutines.delay
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.components.FondoFooter
import cl.duocuc.miregistro.ui.components.MenuLateral
import cl.duocuc.miregistro.ui.components.TarjetaEquipo
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisEquiposClienteScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onVerHistorialEquipo: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    onVerMiPerfil: () -> Unit,
    onCambiarContrasena: () -> Unit,
    api: InduztekApi? = null
) {
    val context = LocalContext.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }

    var textoBusqueda by remember { mutableStateOf("") }

    var paginas by remember(textoBusqueda) { mutableIntStateOf(1) }
    var revision by remember { mutableIntStateOf(0) }
    val carga by cargar(usuarioActual?.id, textoBusqueda, paginas, revision, api) {
        if (api != null) {
            if (textoBusqueda.isNotBlank()) delay(250)
            val primera = api.misEquipos(textoBusqueda, 0)
            val equipos = buildList {
                addAll(primera.content)
                for (pagina in 1 until minOf(paginas, primera.page.totalPages)) {
                    addAll(api.misEquipos(textoBusqueda, pagina).content)
                }
            }
            equipos.distinctBy { it.id } to (paginas < primera.page.totalPages)
        } else {
            val equipos = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                usuarioActual?.let { dbHelper?.buscarEquiposPorCliente(it.id, textoBusqueda) }.orEmpty()
            }
            equipos to false
        }
    }

    MenuLateral(
        usuarioActual = usuarioActual,
        onCerrarSesion = onCerrarSesion,
        onVerMiPerfil = onVerMiPerfil,
        onVerUsuarios = { textoBusqueda = "" },
        onCambiarContrasena = onCambiarContrasena
    ) { onAbrirMenu, snackbarHostState ->
        Scaffold(
            topBar = {
                AppTopBar(
                    titulo = "Mis Equipos",
                    onAbrirMenu = onAbrirMenu
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            FondoFooter(
                modifier = Modifier.padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(Dimens.espacioMedio)) {

                    CampoTexto(
                        valor = textoBusqueda,
                        onValorCambia = { textoBusqueda = it },
                        etiqueta = "Buscar por TAG o Planta...",
                        icono = R.drawable.ic_persona
                    )

                    Spacer(Modifier.height(Dimens.espacioMedio))

                    ContenidoRemoto(carga, { revision++ }) { (equiposFiltrados, hayMas) ->
                    if (equiposFiltrados.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se encontraron equipos.")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                        ) {
                            items(equiposFiltrados, key = { it.id }) { equipo ->
                                TarjetaEquipo(
                                    equipo = equipo,
                                    onVerHistorial = { onVerHistorialEquipo(equipo.id.toLong()) },
                                    onRegistrarPrueba = null
                                )
                            }
                            if (hayMas) item {
                                TextButton(onClick = { paginas++ }) { Text("Cargar más equipos") }
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}