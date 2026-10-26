package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duocuc.miregistro.ui.components.CampoContrasena
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.viewmodel.LoginViewModel
import cl.duocuc.miregistro.R

@Composable
fun LoginClienteScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (Long) -> Unit,
    onRegistroClick: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Induztek Ingeniería SpA",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Portal de Protocolos en Terreno",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))


            CampoTexto(
                valor = uiState.correo,
                onValorCambia = { viewModel.onCorreoChanged(it) },
                etiqueta = "Correo electrónico corporativo",
                icono = R.drawable.ic_correo

            )

            Spacer(modifier = Modifier.height(16.dp))


            CampoContrasena(
                valor = uiState.contrasena,
                onValorCambia = { viewModel.onContrasenaChanged(it) },
                etiqueta = "Contraseña",
                visible = uiState.contrasenaVisible,
                onAlternarVisible = { viewModel.onAlternarVisibilidadContrasena() },
                modifier = Modifier.fillMaxWidth(),
                error = uiState.errorMensaje
            )




            Spacer(modifier = Modifier.height(24.dp))


            Button(
                onClick = {
                    viewModel.autenticar { rolId ->

                        onLoginSuccess(rolId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.cargando
            ) {
                Text(text = "Iniciar Sesión")
            }
        }
    }
}