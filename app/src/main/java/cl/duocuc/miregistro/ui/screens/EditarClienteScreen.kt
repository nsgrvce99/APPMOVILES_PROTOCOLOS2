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
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarClienteScreen(
    cliente: Usuario,
    onVolver: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var nombre by remember { mutableStateOf(cliente.nombre) }
    var rut by remember { mutableStateOf(cliente.rut) }
    var giro by remember { mutableStateOf(cliente.giro ?: "") }
    var correo by remember { mutableStateOf(cliente.correo) }
    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(titulo = "Editar Cliente", onVolver = onVolver) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioMedio)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            CampoTexto(valor = nombre, onValorCambia = { nombre = it; error = null }, etiqueta = "Razón Social", icono = R.drawable.ic_persona)
            CampoTexto(valor = rut, onValorCambia = { rut = it; error = null }, etiqueta = "RUT Empresa", icono = R.drawable.ic_persona)
            CampoTexto(valor = giro, onValorCambia = { giro = it }, etiqueta = "Giro", icono = R.drawable.ic_persona)
            CampoTexto(valor = correo, onValorCambia = { correo = it }, etiqueta = "Correo de Contacto", icono = R.drawable.ic_correo, tipoTeclado = KeyboardType.Email)

            MensajeError(texto = error)
            if (exito) MensajeExito(titulo = "Cliente actualizado", detalles = listOf("Los datos se guardaron correctamente."))

            BotonPrincipal(
                texto = "Guardar Cambios",
                onClick = {
                    if (nombre.isBlank() || rut.isBlank() || correo.isBlank()) {
                        error = "Nombre, RUT y Correo son obligatorios"
                    } else {
                        dbHelper.actualizarDatosCliente(cliente.id, nombre, rut, giro, correo)
                        exito = true
                        error = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (exito) {
                OutlinedButton(onClick = onGuardadoExitoso, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver a la lista")
                }
            }
        }
    }
}