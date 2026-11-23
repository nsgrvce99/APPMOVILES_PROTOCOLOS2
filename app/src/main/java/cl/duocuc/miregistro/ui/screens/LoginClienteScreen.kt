package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duocuc.miregistro.ui.components.CampoContrasena
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.components.FondoFooter
import cl.duocuc.miregistro.viewmodel.LoginViewModel
import cl.duocuc.miregistro.R

@Composable
fun LoginClienteScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (Long) -> Unit,
    onRequiereCambio: () -> Unit,
    onRegistroClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        FondoFooter {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_induztek),
                    contentDescription = "Logo Induztek Ingeniería SpA",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(152.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))
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
                    modifier = Modifier.fillMaxWidth()
                )


                if (uiState.errorMensaje != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMensaje!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.autenticar(
                            onSuccess = { rolId ->
                                onLoginSuccess(rolId)
                            },
                            onRequiereCambio = {
                                onRequiereCambio()
                            }
                        )
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
}