package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.theme.Dimens

// =====================================================================
//  EditarScreen.kt  —  PANTALLA 4: FORMULARIO DE MODIFICACIÓN  (NUEVO, Bloque 4)
//  ---------------------------------------------------------------------
//  Recibe el Usuario que se tocó en la lista y muestra sus datos en
//  campos editables. Al presionar ✓ entrega el usuario MODIFICADO.
//
//  Estado del formulario: LOCAL, con rememberSaveable.
//   - remember          -> el valor sobrevive a la recomposición.
//   - rememberSaveable  -> además sobrevive a la ROTACIÓN.
//   - mutableStateOf(usuario.nombre) -> el campo PARTE con el dato recibido.
//  Para 3 campos simples basta; en una app más grande se usaría un
//  EditarViewModel (ahí sí podría necesitarse un factory o SavedStateHandle).
//
//  Barra superior:  [ ← ]  Editar usuario  [ ✓ ]   <- ✓ = ícono de ACCIÓN
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)   // TopAppBar aún es "experimental"
@Composable
fun EditarScreen(
    usuario: Usuario,                 // Datos que llegan desde la lista
    onVolver: () -> Unit,
    onGuardar: (Usuario) -> Unit      // Devuelve el usuario modificado
) {
    // Estados editables para los campos permitidos del usuario
    var nombre by rememberSaveable { mutableStateOf(usuario.nombre) }
    var correo by rememberSaveable { mutableStateOf(usuario.correo) }
    var rut by rememberSaveable { mutableStateOf(usuario.rut) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_editar)) },
                navigationIcon = {                               // ← volver sin guardar
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), stringResource(R.string.cd_volver))
                    }
                },
                actions = {                                      // ✓ guardar (ícono de ACCIÓN)
                    IconButton(
                        onClick = {
                            // Reconstruimos el objeto Usuario preservando su ID, rol y estado original
                            onGuardar(
                                usuario.copy(
                                    nombre = nombre,
                                    correo = correo,
                                    rut = rut
                                )
                            )
                        },
                        enabled = nombre.isNotBlank() && rut.isNotBlank() // Validaciones mínimas
                    ) {
                        Icon(painterResource(R.drawable.ic_check), stringResource(R.string.cd_guardar))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            CampoTexto(
                valor = nombre,
                onValorCambia = { nombre = it },
                etiqueta = stringResource(R.string.campo_nombre),
                icono = R.drawable.ic_persona
            )
            CampoTexto(
                valor = correo,
                onValorCambia = { correo = it },
                etiqueta = stringResource(R.string.campo_correo),
                icono = R.drawable.ic_correo,
                tipoTeclado = KeyboardType.Email
            )
            CampoTexto(
                valor = rut,
                onValorCambia = { rut = it },
                etiqueta = "RUT (ej. 12.345.678-9)",
                icono = R.drawable.ic_persona
            )
        }
    }
}
