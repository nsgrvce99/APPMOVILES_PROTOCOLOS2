package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.BotonPrincipal
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUsuarioClienteScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var nombre by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var giro by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta Cliente (RN-20)") },
                navigationIcon = { IconButton(onClick = onVolver) { Text("←") } }
            )
        }
    ) { innerPadding ->
        // Validación de Rol: Solo Jefe Técnico (ID 3)
        if (usuarioActual?.rolId != 3L) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Acceso denegado. Solo el Jefe Técnico puede otorgar cuentas.", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Dimens.espacioMedio)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                Text("Datos de la Empresa Cliente", style = MaterialTheme.typography.titleMedium)

                CampoTexto(valor = nombre, onValorCambia = { nombre = it }, etiqueta = "Nombre del contacto", icono = R.drawable.ic_persona)
                CampoTexto(valor = rut, onValorCambia = { rut = it }, etiqueta = "RUT de la empresa", icono = R.drawable.ic_persona)
                CampoTexto(valor = giro, onValorCambia = { giro = it }, etiqueta = "Giro (ej. Agroindustria)", icono = R.drawable.ic_persona)
                CampoTexto(valor = correo, onValorCambia = { correo = it }, etiqueta = "Correo (será su login)", icono = R.drawable.ic_correo)
                CampoTexto(valor = contrasena, onValorCambia = { contrasena = it }, etiqueta = "Contraseña", icono = R.drawable.ic_candado)

                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                if (exito) Text("¡Cuenta cliente creada exitosamente!", color = MaterialTheme.colorScheme.primary)

                BotonPrincipal(
                    texto = "Crear Cuenta",
                    onClick = {
                        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                            error = "Nombre, correo y contraseña son obligatorios"
                        } else {
                            // Creamos el usuario con Rol 4 (CLIENTE)
                            val nuevoCliente = Usuario(
                                rut = rut,
                                nombre = nombre,
                                correo = correo,
                                contrasena = contrasena,
                                rolId = 4L,
                                giro = giro,
                                activo = true
                            )
                            val id = dbHelper.insertarUsuario(nuevoCliente)
                            if (id > 0) {
                                exito = true
                                error = null
                                nombre = ""; rut = ""; correo = ""; contrasena = ""; giro = ""
                            } else {
                                error = "Error al crear la cuenta. Verifique que el correo o RUT no existan."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}