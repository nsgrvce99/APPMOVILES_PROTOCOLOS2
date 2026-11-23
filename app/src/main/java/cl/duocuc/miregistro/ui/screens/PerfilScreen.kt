package cl.duocuc.miregistro.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Perfil
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AvatarCircular
import cl.duocuc.miregistro.ui.components.FondoFooter
import cl.duocuc.miregistro.ui.components.TarjetaSeccion
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.viewmodel.PerfilViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    usuario: Usuario?,
    onVolver: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }


    var fotoActual by remember { mutableStateOf(usuario?.fotoUri) }
    var mostrarBottomSheet by remember { mutableStateOf(false) }
    var uriFotoTemporal by remember { mutableStateOf<Uri?>(null) }

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


    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {}
            fotoActual = it.toString()
            usuario?.id?.let { id -> dbHelper.actualizarFotoUsuario(id, it.toString()) }
        }
    }

    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            val uri = uriFotoTemporal
            if (uri != null) {
                fotoActual = uri.toString()
                usuario?.id?.let { id -> dbHelper.actualizarFotoUsuario(id, uri.toString()) }
            }
        }
    }

    val launcherPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val file = crearArchivoImagenTemporal(context)
            val nuevaUri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
            )
            uriFotoTemporal = nuevaUri
            launcherCamara.launch(nuevaUri)
        }
    }

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
        FondoFooter(
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                EncabezadoPerfil(
                    fotoUri = fotoActual,
                    onCambiarFotoClick = { mostrarBottomSheet = true }
                )

                Spacer(Modifier.height(Dimens.espacioChico))

                Text(
                    text = perfilActual?.nombre ?: "Sin nombre",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = perfilActual?.rolNombre ?: "Rol no asignado",
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
                        text = "RUT: ${perfilActual?.rut ?: "N/A"} · ${perfilActual?.correo ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = Dimens.espacioMini)
                    )
                }

                Column(
                    modifier = Modifier.padding(Dimens.espacioPantalla),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    TarjetaSeccion(titulo = "Detalles de Cuenta") {
                        Text(
                            text = "Estado: " + if (perfilActual?.activo == true) "Activo" else "Desactivado",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        perfilActual?.cargo?.let { cargo ->
                            Text(text = "Cargo: $cargo", style = MaterialTheme.typography.bodyMedium)
                        }
                        perfilActual?.giro?.let { giro ->
                            Text(text = "Giro Empresa: $giro", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (usuario?.rolId != 4L) {
                        TarjetaSeccion(titulo = "Normativa y Seguridad") {
                            Text(
                                text = "El personal de terreno debe utilizar obligatoriamente Equipo de Protección Personal (EPP) en salas eléctricas y subestaciones.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(Modifier.height(72.dp))
                }
            }
        }
    }


    if (mostrarBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { mostrarBottomSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.espacioMedio)
            ) {
                Text(
                    text = "Cambiar foto de perfil",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = {
                        mostrarBottomSheet = false
                        val permisoConcedido = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (permisoConcedido) {
                            val file = crearArchivoImagenTemporal(context)
                            val nuevaUri = FileProvider.getUriForFile(
                                context, "${context.packageName}.fileprovider", file
                            )
                            uriFotoTemporal = nuevaUri
                            launcherCamara.launch(nuevaUri)
                        } else {
                            launcherPermisoCamara.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tomar foto (Cámara)")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        mostrarBottomSheet = false
                        launcherGaleria.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Elegir de la Galería")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


@Composable
private fun EncabezadoPerfil(
    fotoUri: String?,
    onCambiarFotoClick: () -> Unit
) {
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


        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clickable { onCambiarFotoClick() }
        ) {
            AvatarCircular(
                fotoUri = fotoUri,
                descripcion = stringResource(R.string.cd_avatar),
                modifier = Modifier
            )

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .offset(x = 4.dp, y = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Cambiar foto",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

fun crearArchivoImagenTemporal(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = context.cacheDir
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}