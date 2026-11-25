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
import cl.duocuc.miregistro.network.*
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
    val api = LocalApiTerreno.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }
    val operacion = recordarOperacion()

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
        topBar = { AppTopBar(titulo = "Registrar Cliente", onVolver = onVolver) }
    ) { innerPadding ->
        FondoFooter(
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.espacioMedio)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                Text("Datos de la Empresa ", style = MaterialTheme.typography.titleMedium)
                Text("La cuenta quedará inactiva. El Jefe Técnico la habilitará luego.", style = MaterialTheme.typography.bodySmall)

                CampoValidado(
                    valor = razonSocial,
                    onValorCambia = { razonSocial = it; errorRazonSocial = null },
                    etiqueta = "Nombre Del Cliente",
                    icono = R.drawable.ic_persona,
                    error = errorRazonSocial
                )

                CampoValidado(
                    valor = rut,
                    onValorCambia = { rut = it; errorRut = null },
                    etiqueta = "RUT (ej. 26.482.915-8)",
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
                    MensajeExito(titulo = "Cliente Registrado", detalles = listOf("Pendiente de activación por el Jefe Técnico."))
                }

                MensajeError(texto = operacion.error)
                BotonPrincipal(
                    habilitado = !operacion.ejecutando,
                    texto = "Registrar Cliente",
                    onClick = {

                        errorRazonSocial = null
                        errorRut = null
                        errorCorreo = null
                        errorGeneral = null

                        when {
                            razonSocial.isBlank() -> errorRazonSocial = "El nombre es obligatorio"
                            rut.isBlank() -> errorRut = "El RUT es obligatorio"
                            !Validaciones.esRutValido(rut) -> errorRut = "RUT inválido "
                            correo.isBlank() -> errorCorreo = "El correo es obligatorio"
                            !Validaciones.esCorreoValido(correo) -> errorCorreo = "Correo inválido"
                            dbHelper?.existeRut(rut) == true -> errorRut = "Este RUT ya está registrado"
                            dbHelper?.existeCorreo(correo) == true -> errorCorreo = "Este correo ya está registrado"
                            else -> {
                                if (api != null) {
                                    operacion.ejecutar {
                                        api.crearCliente(ClienteSolicitud(razonSocial.trim(), rut, correo.trim(), giro.trim()))
                                        exito = true
                                        razonSocial = ""; rut = ""; giro = ""; correo = ""
                                    }
                                    return@BotonPrincipal
                                }
                                val nuevoCliente = Usuario(
                                    rut = rut,
                                    nombre = razonSocial,
                                    correo = correo,
                                    contrasena = "PENDIENTE_${java.util.UUID.randomUUID()}",
                                    rolId = 4L,
                                    activo = false,
                                    giro = giro
                                )
                                val id = checkNotNull(dbHelper).insertarUsuario(nuevoCliente)
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
}