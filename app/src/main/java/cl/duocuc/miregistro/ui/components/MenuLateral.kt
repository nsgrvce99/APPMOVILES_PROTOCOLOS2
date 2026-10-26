package cl.duocuc.miregistro.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuLateral(
    usuarioActual: Usuario?,
    onCerrarSesion: () -> Unit,
    onVerMiPerfil: () -> Unit = {},
    onVerUsuarios: () -> Unit = {},
    content: @Composable (onAbrirMenu: () -> Unit, snackbarHostState: SnackbarHostState) -> Unit
) {
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
                    label = { Text("Consultar Equipos e Historial") },
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

        content(
            { scope.launch { drawerState.open() } },
            snackbarHostState
        )
    }
}