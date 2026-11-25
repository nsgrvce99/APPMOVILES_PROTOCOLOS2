package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun EditarClienteScreen(
    cliente: Usuario,
    onVolver: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    val context = LocalContext.current
    val api = LocalApiTerreno.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }
    val operacion = recordarOperacion()

    var nombre by remember { mutableStateOf(cliente.nombre) }
    var rut by remember { mutableStateOf(cliente.rut) }
    var giro by remember { mutableStateOf(cliente.giro ?: "") }
    var correo by remember { mutableStateOf(cliente.correo) }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorRut by remember { mutableStateOf<String?>(null) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    var exito by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(titulo = "Editar Cliente", onVolver = onVolver) }
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
                CampoValidado(
                    valor = nombre,
                    onValorCambia = { nombre = it; errorNombre = null },
                    etiqueta = "Razón Social",
                    icono = R.drawable.ic_persona,
                    error = errorNombre
                )

                CampoValidado(
                    valor = rut,
                    onValorCambia = { rut = it; errorRut = null },
                    etiqueta = "RUT Empresa",
                    icono = R.drawable.ic_persona,
                    error = errorRut
                )

                CampoValidado(
                    valor = giro,
                    onValorCambia = { giro = it },
                    etiqueta = "Giro",
                    icono = R.drawable.ic_persona
                )

                CampoValidado(
                    valor = correo,
                    onValorCambia = { correo = it; errorCorreo = null },
                    etiqueta = "Correo Electrónico",
                    icono = R.drawable.ic_correo,
                    error = errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )

                if (errorGeneral != null) {
                    Text(
                        text = errorGeneral!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (exito) {
                    MensajeExito(
                        titulo = "Cliente actualizado",
                        detalles = listOf("Los datos se guardaron correctamente.")
                    )
                }

                MensajeError(texto = operacion.error)
                BotonPrincipal(
                    habilitado = !operacion.ejecutando,
                    texto = if (exito) "Volver" else "Guardar Cambios",
                    onClick = {
                        if (exito) {
                            onGuardadoExitoso()
                        } else {
                            errorNombre = null
                            errorRut = null
                            errorCorreo = null
                            errorGeneral = null

                            when {
                                nombre.isBlank() -> errorNombre = "La razón social es obligatoria"
                                rut.isBlank() -> errorRut = "El RUT es obligatorio"
                                !Validaciones.esRutValido(rut) -> errorRut = "RUT inválido."
                                correo.isBlank() -> errorCorreo = "El correo es obligatorio"
                                !Validaciones.esCorreoValido(correo) -> errorCorreo = "Correo inválido"
                                rut != cliente.rut && dbHelper?.existeRut(rut) == true -> errorRut = "Este RUT ya está registrado en otro cliente."
                                correo != cliente.correo && dbHelper?.existeCorreo(correo) == true -> errorCorreo = "Este correo ya está registrado en otro usuario."
                                else -> {
                                    if (api != null) {
                                        operacion.ejecutar {
                                            api.editarCliente(cliente.id, ClienteSolicitud(nombre.trim(), rut, correo.trim(), giro.trim()))
                                            exito = true
                                        }
                                        return@BotonPrincipal
                                    }
                                    try {
                                        checkNotNull(dbHelper).actualizarDatosCliente(
                                            idCliente = cliente.id,
                                            nuevoNombre = nombre,
                                            nuevoRut = rut,
                                            nuevoGiro = giro,
                                            nuevoCorreo = correo
                                        )
                                        exito = true
                                    } catch (e: Exception) {
                                        errorGeneral = if (e.message?.contains("UNIQUE constraint", ignoreCase = true) == true) {
                                            "El correo o RUT ya está registrado en otro usuario del sistema."
                                        } else {
                                            "Error al actualizar los datos. Por favor, verifique la información e intente nuevamente."
                                        }
                                    }
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