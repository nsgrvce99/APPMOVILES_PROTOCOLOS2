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
    var correo by remember { mutableStateOf("") } // 👈 NUEVO CAMPO
    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(titulo = "Registrar Empresa Cliente", onVolver = onVolver) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioMedio).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Text("Datos de la Empresa (RF-01)", style = MaterialTheme.typography.titleMedium)
            Text("La cuenta quedará inactiva. El Jefe Técnico le asignará la clave (RN-20).", style = MaterialTheme.typography.bodySmall)

            CampoTexto(valor = razonSocial, onValorCambia = { razonSocial = it }, etiqueta = "Razón Social", icono = R.drawable.ic_persona)
            CampoTexto(valor = rut, onValorCambia = { rut = it }, etiqueta = "RUT Empresa", icono = R.drawable.ic_persona)
            CampoTexto(valor = giro, onValorCambia = { giro = it }, etiqueta = "Giro", icono = R.drawable.ic_persona)
            // 👇 NUEVO CAMPO DE CORREO
            CampoTexto(
                valor = correo,
                onValorCambia = { correo = it },
                etiqueta = "Correo Electrónico de Contacto",
                icono = R.drawable.ic_correo,
                tipoTeclado = KeyboardType.Email
            )

            MensajeError(texto = error)
            if (exito) MensajeExito(titulo = "Empresa Registrada", detalles = listOf("Correo registrado. Pendiente de clave por el Jefe Técnico."))

            BotonPrincipal(
                texto = "Registrar Empresa",
                onClick = {
                    if (razonSocial.isBlank() || rut.isBlank() || correo.isBlank()) {
                        error = "Razón Social, RUT y Correo son obligatorios"
                    } else {
                        val nuevoCliente = Usuario(
                            rut = rut,
                            nombre = razonSocial,
                            correo = correo, // 👈 Guardamos el correo real
                            contrasena = "PENDIENTE_${rut}_${System.currentTimeMillis()}",
                            rolId = 4L,
                            activo = false,
                            giro = giro
                        )
                        val id = dbHelper.insertarUsuario(nuevoCliente)
                        if (id > 0) {
                            exito = true
                            razonSocial = ""; rut = ""; giro = ""; correo = ""
                        } else {
                            error = "Error al registrar. Verifique que el correo no exista."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}