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
    onVolver: (() -> Unit)? = null,           // Para las pantallas con flecha ←
    onAbrirMenu: (() -> Unit)? = null,        // 👈 Para las pantallas con menú ☰
    acciones: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            if (onAbrirMenu != null) {
                // Botón de menú (☰)
                IconButton(onClick = onAbrirMenu) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu),
                        contentDescription = "Abrir menú"
                    )
                }
            } else if (onVolver != null) {
                // Botón de volver (←)
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