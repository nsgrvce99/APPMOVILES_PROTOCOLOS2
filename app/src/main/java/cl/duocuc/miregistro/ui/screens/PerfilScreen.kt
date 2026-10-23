package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.model.Perfil
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AvatarCircular
import cl.duocuc.miregistro.ui.components.TarjetaSeccion
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.viewmodel.PerfilViewModel

@Composable
fun PerfilScreen(
    usuario: Usuario?,
    onVolver: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Convertimos el Usuario de la sesión a Perfil para mostrarlo
    val perfilActual = if (usuario != null) {
        Perfil(
            id = usuario.id,
            rut = usuario.rut,
            nombre = usuario.nombre,
            correo = usuario.correo,
            rolNombre = usuario.rolNombre ?: "Personal Induztek",
            activo = usuario.activo,
            giro = usuario.giro,
            cargo = usuario.cargo
        )
    } else {
        uiState.perfil
    }

    PerfilContent(
        perfil = perfilActual,
        rolId = usuario?.rolId, // 👈 Pasamos el rolId para saber si es cliente
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilContent(
    perfil: Perfil?,
    rolId: Long?, // 👈 NUEVO PARÁMETRO
    onVolver: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.titulo_perfil)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.cd_volver)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EncabezadoPerfil()

            Spacer(Modifier.height(Dimens.espacioChico))

            // Nombre y Rol
            Text(
                text = perfil?.nombre ?: "Sin nombre",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = perfil?.rolNombre ?: "Rol no asignado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = Dimens.espacioMini)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_persona),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "RUT: ${perfil?.rut ?: "N/A"} · ${perfil?.correo ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = Dimens.espacioMini)
                )
            }

            // ---------- Secciones de información ----------
            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                TarjetaSeccion(titulo = "Detalles de Cuenta / Terreno") {
                    Text(
                        text = "Estado: " + if (perfil?.activo == true) "Activo (Habilitado para operar)" else "Desactivado",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    perfil?.cargo?.let { cargo ->
                        Text(text = "Cargo: $cargo", style = MaterialTheme.typography.bodyMedium)
                    }
                    perfil?.giro?.let { giro ->
                        Text(text = "Giro Empresa: $giro", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // 👇 CONDICIÓN: Mostrar solo si NO es cliente (rolId != 4)
                if (rolId != 4L) {
                    TarjetaSeccion(titulo = "Normativa y Seguridad") {
                        Text(
                            text = "El personal de terreno debe utilizar obligatoriamente Equipo de Protección Personal (EPP) en salas eléctricas y subestaciones (Anexo 2).",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun EncabezadoPerfil() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.altoPortada + Dimens.tamanoAvatar / 2)
    ) {
        Image(
            painter = painterResource(R.drawable.portada_perfil),
            contentDescription = stringResource(R.string.cd_portada),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.altoPortada)
                .align(Alignment.TopCenter)
        )
        AvatarCircular(
            imagen = R.drawable.avatar_usuario,
            descripcion = stringResource(R.string.cd_avatar),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
