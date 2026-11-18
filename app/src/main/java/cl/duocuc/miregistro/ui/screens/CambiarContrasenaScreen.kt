package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambiarContrasenaScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onContrasenaCambiada: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    // Obtenemos la contraseña real de la BD una sola vez
    val usuarioDb = remember { dbHelper.buscarPorId(usuarioActual?.id ?: 0L) }
    val contrasenaReal = usuarioDb?.contrasena ?: ""

    // 👇 DETECTAMOS SI ES UNA CUENTA CON CONTRASEÑA POR DEFECTO
    // Si la contraseña es "Induztek2026" o empieza con "PENDIENTE_", no pedimos la actual
    val esClavePorDefecto = contrasenaReal == "Induztek2026" || contrasenaReal.startsWith("PENDIENTE_")

    // Si es clave por defecto, ocultamos el campo "Contraseña Actual"
    var actual by remember { mutableStateOf(if (esClavePorDefecto) contrasenaReal else "") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    var actualVisible by remember { mutableStateOf(false) }
    var nuevaVisible by remember { mutableStateOf(false) }
    var confirmarVisible by remember { mutableStateOf(false) }

    var errorActual by remember { mutableStateOf<String?>(null) }
    var errorNueva by remember { mutableStateOf<String?>(null) }
    var errorConfirmar by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    fun esContrasenaSegura(contrasena: String): Boolean {
        val tieneMinimo = contrasena.length >= 6
        val tieneMayuscula = contrasena.any { it.isUpperCase() }
        val tieneEspecial = contrasena.any { !it.isLetterOrDigit() }
        return tieneMinimo && tieneMayuscula && tieneEspecial
    }

    Scaffold(
        topBar = { AppTopBar(titulo = "Cambiar Contraseña", onVolver = onVolver) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioMedio)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Text("Actualice su contraseña", style = MaterialTheme.typography.titleMedium)
            Text("Mínimo 6 caracteres, al menos una mayúscula y un carácter especial.", style = MaterialTheme.typography.bodySmall)

            // 👇 MOSTRAMOS EL CAMPO ACTUAL SOLO SI NO ES CLAVE POR DEFECTO
            if (!esClavePorDefecto) {
                CampoContrasena(
                    valor = actual,
                    onValorCambia = { actual = it; errorActual = null },
                    etiqueta = "Contraseña Actual",
                    visible = actualVisible,
                    onAlternarVisible = { actualVisible = !actualVisible },
                    modifier = Modifier.fillMaxWidth(),
                    error = errorActual
                )
            }

            CampoContrasena(
                valor = nueva,
                onValorCambia = { nueva = it; errorNueva = null },
                etiqueta = "Nueva Contraseña",
                visible = nuevaVisible,
                onAlternarVisible = { nuevaVisible = !nuevaVisible },
                modifier = Modifier.fillMaxWidth(),
                error = errorNueva
            )

            CampoContrasena(
                valor = confirmar,
                onValorCambia = { confirmar = it; errorConfirmar = null },
                etiqueta = "Confirmar Nueva Contraseña",
                visible = confirmarVisible,
                onAlternarVisible = { confirmarVisible = !confirmarVisible },
                modifier = Modifier.fillMaxWidth(),
                error = errorConfirmar
            )

            if (exito) {
                MensajeExito(titulo = "Contraseña actualizada", detalles = listOf("Su contraseña se ha cambiado exitosamente."))
            }

            BotonPrincipal(
                texto = if (exito) "Volver" else "Cambiar Contraseña",
                onClick = {
                    if (exito) {
                        onContrasenaCambiada()
                    } else {
                        errorActual = null
                        errorNueva = null
                        errorConfirmar = null

                        when {
                            // Si NO es clave por defecto, validamos la actual
                            !esClavePorDefecto && actual.isBlank() -> errorActual = "La contraseña actual es obligatoria"
                            !esClavePorDefecto && actual.trim() != contrasenaReal.trim() -> errorActual = "La contraseña actual es incorrecta"
                            nueva.isBlank() -> errorNueva = "La nueva contraseña es obligatoria"
                            !esContrasenaSegura(nueva) -> errorNueva = "Debe tener mínimo 6 caracteres, una mayúscula y un carácter especial"
                            nueva == actual -> errorNueva = "La nueva contraseña no puede ser igual a la actual"
                            confirmar.isBlank() -> errorConfirmar = "Debe confirmar la contraseña"
                            nueva != confirmar -> errorConfirmar = "Las contraseñas no coinciden"
                            else -> {
                                dbHelper.cambiarContrasena(usuarioActual!!.id, nueva)
                                exito = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}