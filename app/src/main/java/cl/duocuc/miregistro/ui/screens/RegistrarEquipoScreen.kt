package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Equipo
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarEquipoScreen(
    instalacionId: Long,
    nombreInstalacion: String,
    onVolver: () -> Unit,
    onEquipoRegistrado: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var tag by remember { mutableStateOf("") }
    var tipoEquipo by remember { mutableStateOf("Transformador") }
    var marca by remember { mutableStateOf("") }
    var serie by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    val tiposEquipo = listOf("Transformador", "Interruptor", "Celda MT", "Cable de poder", "Relé de protección")

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Registrar Nuevo Equipo",
                onVolver = onVolver
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioPantalla)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Text(
                "Complete los datos del equipo",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "Instalación: $nombreInstalacion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            CampoTexto(
                valor = tag,
                onValorCambia = { tag = it; error = null },
                etiqueta = "TAG del equipo",
                icono = R.drawable.ic_candado
            )


            SelectorDesplegable(
                etiqueta = "Tipo de equipo",
                opciones = tiposEquipo,
                seleccionada = tipoEquipo,
                onSeleccionar = { tipoEquipo = it },
                textoDe = { it }
            )

            CampoTexto(
                valor = marca,
                onValorCambia = { marca = it },
                etiqueta = "Marca",
                icono = R.drawable.ic_candado
            )

            CampoTexto(
                valor = serie,
                onValorCambia = { serie = it },
                etiqueta = "Número de serie",
                icono = R.drawable.ic_candado
            )

            MensajeError(texto = error)

            if (exito) {
                MensajeExito(
                    titulo = "Equipo registrado exitosamente",
                    detalles = listOf("TAG: $tag", "Tipo: $tipoEquipo", "Instalación: $nombreInstalacion")
                )
            }

            BotonPrincipal(
                texto = if (exito) "Volver a la lista" else "Registrar Equipo",
                onClick = {
                    if (exito) {
                        onEquipoRegistrado()
                    } else {
                        when {
                            tag.isBlank() -> error = "El TAG es obligatorio"
                            marca.isBlank() -> error = "La marca es obligatoria"
                            serie.isBlank() -> error = "El número de serie es obligatorio"
                            dbHelper.existeTagEnInstalacion(tag, nombreInstalacion) ->
                                error = "El TAG '$tag' ya existe en la instalación '$nombreInstalacion'"
                            else -> {
                                val nuevoEquipo = Equipo(
                                    tag = tag.trim(),
                                    tipoEquipo = tipoEquipo,
                                    clasificacion = "Primario",
                                    instalacion = nombreInstalacion,
                                    marca = marca.trim(),
                                    serie = serie.trim(),
                                    potenciaTension = "",
                                    idCliente = 5L
                                )
                                val idInsertado = dbHelper.insertarEquipo(nuevoEquipo)
                                if (idInsertado > 0) {
                                    exito = true
                                } else {
                                    error = "Error al guardar el equipo. Intente nuevamente."
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