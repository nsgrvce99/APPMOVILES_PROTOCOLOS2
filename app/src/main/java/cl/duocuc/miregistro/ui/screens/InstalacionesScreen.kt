package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalacionesScreen(
    usuarioActual: Usuario?,
    onSeleccionarInstalacion: (Int) -> Unit,
    onIrAFormalizacion: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Induztek - Selección de Planta / Faena") }
            )
        }
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

            Text("Seleccione la instalación eléctrica o subestación a inspeccionar:")

            // Botón de ejemplo para planta ID 1
            Button(onClick = { onSeleccionarInstalacion(1) }) {
                Text("Planta Industrial Lo Espejo (ID: 1)")
            }

            Button(onClick = { onSeleccionarInstalacion(2) }) {
                Text("Subestación Norte Quilicura (ID: 2)")
            }

            // Si es Ingeniero o Jefe Técnico, puede ir al módulo de formalización
            if (usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L) {
                Button(onClick = onIrAFormalizacion) {
                    Text("Ir a Formalización de Protocolos (Ingeniero)")
                }
            }
        }
    }
}