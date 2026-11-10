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
import cl.duocuc.miregistro.model.Equipo
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.CampoTexto
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
    onVerMiPerfil: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var textoBusqueda by remember { mutableStateOf("") }

    // 👇 AQUÍ USAMOS EL MÉTODO OPTIMIZADO
    // Cada vez que 'textoBusqueda' cambie, se volverá a ejecutar la consulta en SQLite
    val equiposFiltrados = remember(usuarioActual?.id, textoBusqueda) {
        if (usuarioActual != null) {
            dbHelper.buscarEquiposPorCliente(usuarioActual.id, textoBusqueda)
        } else {
            emptyList()
        }
    }

    MenuLateral(
        usuarioActual = usuarioActual,
        onCerrarSesion = onCerrarSesion,
        onVerMiPerfil = onVerMiPerfil
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
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioMedio)) {

                // Barra de búsqueda
                CampoTexto(
                    valor = textoBusqueda,
                    onValorCambia = { textoBusqueda = it },
                    etiqueta = "Buscar por TAG o Planta...",
                    icono = R.drawable.ic_persona
                )

                Spacer(Modifier.height(Dimens.espacioMedio))

                if (equiposFiltrados.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron equipos.")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                    ) {
                        items(equiposFiltrados) { equipo ->
                            TarjetaEquipo(
                                equipo = equipo,
                                onVerHistorial = { onVerHistorialEquipo(equipo.id.toLong()) },
                                onRegistrarPrueba = null
                            )
                        }
                    }
                }
            }
        }
    }
}