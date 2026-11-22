package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.utils.Validaciones

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarClienteScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    val puedeRegistrar = usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L
    if (!puedeRegistrar) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Acceso denegado.", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    var razonSocial by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var giro by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }

    var errorRazonSocial by remember { mutableStateOf<String?>(null) }
    var errorRut by remember { mutableStateOf<String?>(null) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    var exito by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(titulo = "Registrar Empresa Cliente", onVolver = onVolver) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioMedio)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Text("Datos de la Empresa (RF-01)", style = MaterialTheme.typography.titleMedium)
            Text("La cuenta quedará inactiva. El Jefe Técnico la habilitará luego (RN-20).", style = MaterialTheme.typography.bodySmall)

            CampoValidado(
                valor = razonSocial,
                onValorCambia = { razonSocial = it; errorRazonSocial = null },
                etiqueta = "Razón Social",
                icono = R.drawable.ic_persona,
                error = errorRazonSocial
            )

            CampoValidado(
                valor = rut,
                onValorCambia = { rut = it; errorRut = null },
                etiqueta = "RUT Empresa (ej. 76.482.915-8)",
                icono = R.drawable.ic_persona,
                error = errorRut
            )

            CampoValidado(
                valor = giro,
                onValorCambia = { giro = it },
                etiqueta = "Giro (ej. Agroindustria)",
                icono = R.drawable.ic_persona
            )

            CampoValidado(
                valor = correo,
                onValorCambia = { correo = it; errorCorreo = null },
                etiqueta = "Correo Electrónico de Contacto",
                icono = R.drawable.ic_correo,
                error = errorCorreo,
                tipoTeclado = KeyboardType.Email
            )

            if (errorGeneral != null) {
                Text(errorGeneral!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            if (exito) {
                MensajeExito(titulo = "Empresa Registrada", detalles = listOf("Pendiente de activación por el Jefe Técnico."))
            }

            BotonPrincipal(
                texto = "Registrar Empresa",
                onClick = {

                    errorRazonSocial = null
                    errorRut = null
                    errorCorreo = null
                    errorGeneral = null

                    when {
                        razonSocial.isBlank() -> errorRazonSocial = "La razón social es obligatoria"
                        rut.isBlank() -> errorRut = "El RUT es obligatorio"
                        !Validaciones.esRutValido(rut) -> errorRut = "RUT inválido (RN-03)"
                        correo.isBlank() -> errorCorreo = "El correo es obligatorio"
                        !Validaciones.esCorreoValido(correo) -> errorCorreo = "Correo inválido"
                        dbHelper.existeRut(rut) -> errorRut = "Este RUT ya está registrado"
                        dbHelper.existeCorreo(correo) -> errorCorreo = "Este correo ya está registrado"
                        else -> {
                            val nuevoCliente = Usuario(
                                rut = rut,
                                nombre = razonSocial,
                                correo = correo,
                                contrasena = "PENDIENTE_${java.util.UUID.randomUUID()}",
                                rolId = 4L,
                                activo = false,
                                giro = giro
                            )
                            val id = dbHelper.insertarUsuario(nuevoCliente)
                            if (id > 0) {
                                exito = true
                                razonSocial = ""; rut = ""; giro = ""; correo = ""
                            } else {
                                errorGeneral = "Error al registrar. Intente nuevamente."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}