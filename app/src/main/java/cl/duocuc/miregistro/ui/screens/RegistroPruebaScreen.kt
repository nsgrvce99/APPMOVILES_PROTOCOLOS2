package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroPruebaScreen(
    equipoId: Int,
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onPruebaGuardadaExitosamente: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Protocolo Terreno - Equipo ID: $equipoId") },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Text("←") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Text("Formulario de Pruebas Eléctricas (RF-02 / RN-04)", style = MaterialTheme.typography.titleMedium)
            Text("Técnico responsable: ${usuarioActual?.nombre ?: "N/A"}")

            Button(onClick = onPruebaGuardadaExitosamente) {
                Text("Guardar Protocolo (Modo Offline)")
            }
        }
    }
}