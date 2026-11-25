package cl.duocuc.miregistro.ui.screens

import cl.duocuc.miregistro.utils.Validaciones

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.network.CambioContrasena
import cl.duocuc.miregistro.network.recordarOperacion
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambiarContrasenaScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onContrasenaCambiada: () -> Unit,
    esClavePorDefecto: Boolean = false,
    cambiarRemoto: (suspend (CambioContrasena) -> Unit)? = null
) {
    val context = LocalContext.current
    val dbHelper = remember(cambiarRemoto == null) { if (cambiarRemoto == null) UsuarioDbHelper(context) else null }

    val usuarioDb = remember { dbHelper?.buscarPorId(usuarioActual?.id ?: 0L) }
    val contrasenaReal = usuarioDb?.contrasena ?: ""

    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    var actualVisible by remember { mutableStateOf(false) }
    var nuevaVisible by remember { mutableStateOf(false) }
    var confirmarVisible by remember { mutableStateOf(false) }

    var errorActual by remember { mutableStateOf<String?>(null) }
    var errorNueva by remember { mutableStateOf<String?>(null) }
    var errorConfirmar by remember { mutableStateOf<String?>(null) }
    val operacion = recordarOperacion()
    var exito by remember { mutableStateOf(false) }

    fun esContrasenaSegura(contrasena: String): Boolean {
        val tieneMinimo = contrasena.length in 6..18
        val tieneMayuscula = contrasena.any { it.isUpperCase() }
        val tieneEspecial = contrasena.any { !it.isLetterOrDigit() && !it.isWhitespace() }
        return tieneMinimo && tieneMayuscula && tieneEspecial
    }

    val onVolverSeguro = if (esClavePorDefecto) null else onVolver

    Scaffold(
        topBar = { AppTopBar(titulo = "Cambiar Contraseña", onVolver = onVolverSeguro) }
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
                Text(
                    text = if (esClavePorDefecto) {
                        "Su cuenta ha sido activada. Por seguridad, debe crear una nueva contraseña antes de continuar."
                    } else {
                        "Actualice su contraseña"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Mínimo 6 caracteres, al menos una mayúscula y un carácter especial.",
                    style = MaterialTheme.typography.bodySmall
                )

                if (!esClavePorDefecto || cambiarRemoto != null) {
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
                    MensajeExito(
                        titulo = "Contraseña actualizada",
                        detalles = listOf("Su contraseña se ha cambiado exitosamente.")
                    )
                }

                MensajeError(texto = operacion.error)
                BotonPrincipal(
                    habilitado = !operacion.ejecutando,
                    texto = if (operacion.ejecutando) "Guardando…" else if (exito) "Continuar" else "Cambiar Contraseña",
                    onClick = {
                        if (exito) {
                            onContrasenaCambiada()
                        } else {
                            errorActual = null
                            errorNueva = null
                            errorConfirmar = null

                            when {
                                (!esClavePorDefecto || cambiarRemoto != null) && actual.isBlank() -> errorActual = "La contraseña actual es obligatoria"
                                cambiarRemoto == null && !esClavePorDefecto && actual.trim() != contrasenaReal.trim() -> errorActual = "La contraseña actual es incorrecta"

                                nueva.isBlank() -> errorNueva = "La nueva contraseña es obligatoria"
                                !esContrasenaSegura(nueva) -> errorNueva = "Debe tener mínimo 6 caracteres, una mayúscula y un carácter especial"
                                Validaciones.esClaveTemporalLocal(nueva) -> errorNueva = "No puede usar la contraseña por defecto"
                                (!esClavePorDefecto || cambiarRemoto != null) && nueva == actual -> errorNueva = "La nueva contraseña no puede ser igual a la actual"

                                confirmar.isBlank() -> errorConfirmar = "Debe confirmar la contraseña"
                                nueva != confirmar -> errorConfirmar = "Las contraseñas no coinciden"

                                else -> {
                                    if (cambiarRemoto != null) {
                                        operacion.ejecutar { cambiarRemoto(CambioContrasena(actual, nueva, confirmar)) }
                                    } else {
                                        dbHelper?.cambiarContrasena(checkNotNull(usuarioActual).id, nueva)
                                        exito = true
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