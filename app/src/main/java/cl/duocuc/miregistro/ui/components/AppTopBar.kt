package cl.duocuc.miregistro.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cl.duocuc.miregistro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    onAbrirMenu: (() -> Unit)? = null,
    acciones: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            if (onAbrirMenu != null) {

                IconButton(onClick = onAbrirMenu) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu),
                        contentDescription = "Abrir menú"
                    )
                }
            } else if (onVolver != null) {

                IconButton(onClick = onVolver) {
                    Icon(
                        painter = painterResource(R.drawable.ic_volver),
                        contentDescription = stringResource(R.string.cd_volver)
                    )
                }
            }
        },
        actions = { acciones() }
    )
}