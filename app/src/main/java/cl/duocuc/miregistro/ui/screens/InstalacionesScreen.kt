package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.MenuLateral
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalacionesScreen(
    usuarioActual: Usuario?,
    onSeleccionarInstalacion: (Long) -> Unit,
    onIrAFormalizacion: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    // 👇 Envolvemos todo con el MenuLateral
    MenuLateral(
        usuarioActual = usuarioActual,
        onCerrarSesion = onCerrarSesion
    ) { onAbrirMenu, snackbarHostState ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Induztek - Selección de Instalación") },
                    navigationIcon = {
                        // 👇 El botón ☰ para abrir el menú
                        IconButton(onClick = onAbrirMenu) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = "Abrir menú"
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) } // 👈 Para el aviso de sesión cerrada
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenido, ${usuarioActual?.nombre ?: "Operador Terreno"} (${usuarioActual?.rolNombre ?: "Técnico"})",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Seleccione la instalación eléctrica a inspeccionar:")

                // 👇 Las 6 plantas reales del Anexo 9
                Button(onClick = { onSeleccionarInstalacion(1L) }) {
                    Text("Planta Lampa (Envasadora del Pacífico)")
                }
                Button(onClick = { onSeleccionarInstalacion(2L) }) {
                    Text("Planta Elevadora Sur (Sanitaria Valle Central)")
                }
                Button(onClick = { onSeleccionarInstalacion(3L) }) {
                    Text("Faena Til Til (Minera Cerro Alto)")
                }
                Button(onClick = { onSeleccionarInstalacion(4L) }) {
                    Text("Edificio Clínico (Clínica Los Aromos)")
                }
                Button(onClick = { onSeleccionarInstalacion(5L) }) {
                    Text("Planta Molienda (Cementos Lomas Blancas)")
                }
                Button(onClick = { onSeleccionarInstalacion(6L) }) {
                    Text("Site Huechuraba (Data Center Andes Norte)")
                }

                // Si es Ingeniero o Jefe Técnico, puede ir al módulo de formalización
                if (usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L) {
                    Button(
                        onClick = onIrAFormalizacion,
                        modifier = Modifier.padding(top = Dimens.espacioMedio)
                    ) {
                        Text("Ir a Formalización de Protocolos (Ingeniero)")
                    }
                }
            }
        }
    }
}