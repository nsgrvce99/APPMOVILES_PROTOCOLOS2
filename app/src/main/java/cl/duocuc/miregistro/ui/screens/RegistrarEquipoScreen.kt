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
import cl.duocuc.miregistro.ui.components.BotonPrincipal
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarEquipoScreen(
    instalacionId: Long,
    onVolver: () -> Unit,
    onEquipoRegistrado: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var tag by remember { mutableStateOf("") }
    var tipoEquipo by remember { mutableStateOf("Transformador") }
    var marca by remember { mutableStateOf("") }
    var serie by remember { mutableStateOf("") }
    var instalacion by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    // Lista de tipos de equipo según RN-05
    val tiposEquipo = listOf("Transformador", "Interruptor", "Celda MT", "Cable de poder", "Relé de protección")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Nuevo Equipo") },
                navigationIcon = { IconButton(onClick = onVolver) { Text("←") } }
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
                "Complete los datos del equipo (RF-02 / HU-01)",
                style = MaterialTheme.typography.titleMedium
            )

            CampoTexto(
                valor = tag,
                onValorCambia = { tag = it; error = null },
                etiqueta = "TAG del equipo (ej. TR-03)",
                icono = R.drawable.ic_persona
            )

            // Selector de tipo de equipo
            var expandido by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = it }
            ) {
                OutlinedTextField(
                    value = tipoEquipo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de equipo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false }
                ) {
                    tiposEquipo.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                tipoEquipo = tipo
                                expandido = false
                            }
                        )
                    }
                }
            }

            CampoTexto(
                valor = marca,
                onValorCambia = { marca = it },
                etiqueta = "Marca",
                icono = R.drawable.ic_persona
            )

            CampoTexto(
                valor = serie,
                onValorCambia = { serie = it },
                etiqueta = "Número de serie",
                icono = R.drawable.ic_persona
            )

            CampoTexto(
                valor = instalacion,
                onValorCambia = { instalacion = it; error = null },
                etiqueta = "Instalación (ej. Planta Lampa)",
                icono = R.drawable.ic_persona
            )

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            if (exito) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(Dimens.espacioMedio)) {
                        Text("✓ Equipo registrado exitosamente", style = MaterialTheme.typography.titleMedium)
                        Text("TAG: $tag")
                        Text("Tipo: $tipoEquipo")
                        Text("Instalación: $instalacion")
                        Spacer(Modifier.height(Dimens.espacioMedio))
                        Text(
                            "Ya puede registrar pruebas para este equipo.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            BotonPrincipal(
                texto = if (exito) "Volver a la lista" else "Registrar Equipo",
                onClick = {
                    if (exito) {
                        onEquipoRegistrado()
                    } else {
                        // Validaciones (RN-01)
                        when {
                            tag.isBlank() -> error = "El TAG es obligatorio (RN-01)"
                            instalacion.isBlank() -> error = "La instalación es obligatoria"
                            marca.isBlank() -> error = "La marca es obligatoria"
                            serie.isBlank() -> error = "El número de serie es obligatorio"
                            dbHelper.existeTagEnInstalacion(tag, instalacion) ->
                                error = "El TAG '$tag' ya existe en la instalación '$instalacion' (RN-01)"
                            else -> {
                                val nuevoEquipo = Equipo(
                                    tag = tag.trim(),
                                    tipoEquipo = tipoEquipo,
                                    instalacion = instalacion.trim(),
                                    marca = marca.trim(),
                                    serie = serie.trim(),
                                    idCliente = 5L  // Cliente de prueba (Marcela)
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