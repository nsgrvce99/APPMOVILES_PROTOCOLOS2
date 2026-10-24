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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
    onCerrarSesion: () -> Unit,
    onVerMiPerfil: () -> Unit,
    onVerUsuarios: () -> Unit,
    onIrAInstrumentos: () -> Unit,
    onIrACrearCliente: () -> Unit
) {
    MenuLateral(
        usuarioActual = usuarioActual,
        onCerrarSesion = onCerrarSesion,
        onVerMiPerfil = onVerMiPerfil,
        onVerUsuarios = onVerUsuarios
    ) { onAbrirMenu, snackbarHostState ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Induztek - Selección de Planta") },
                    navigationIcon = {
                        IconButton(onClick = onAbrirMenu) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = "Abrir menú"
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
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

                // Plantas
                Button(onClick = { onSeleccionarInstalacion(1L) }) { Text("Planta Lampa") }
                Button(onClick = { onSeleccionarInstalacion(2L) }) { Text("Planta Elevadora Sur") }
                Button(onClick = { onSeleccionarInstalacion(3L) }) { Text("Faena Til Til") }
                Button(onClick = { onSeleccionarInstalacion(4L) }) { Text("Edificio Clínico") }
                Button(onClick = { onSeleccionarInstalacion(5L) }) { Text("Planta Molienda") }
                Button(onClick = { onSeleccionarInstalacion(6L) }) { Text("Site Huechuraba") }

                // Botón de Formalización (Ingeniero y Jefe Técnico)
                if (usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L) {
                    Button(
                        onClick = onIrAFormalizacion,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Ir a Formalización de Protocolos")
                    }
                }

                // 👇 BOTONES EXCLUSIVOS DEL JEFE TÉCNICO (Rol 3)
                if (usuarioActual?.rolId == 3L) {
                    Button(onClick = onIrAInstrumentos, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Gestionar Instrumentos (RN-12)")
                    }

                    Button(onClick = onIrACrearCliente, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Crear Cuenta Cliente (RN-20)")
                    }
                }
            }
        }
    }
}