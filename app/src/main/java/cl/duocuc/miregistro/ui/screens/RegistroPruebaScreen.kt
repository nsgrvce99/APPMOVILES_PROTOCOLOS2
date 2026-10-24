package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.viewmodel.RegistroPruebaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroPruebaScreen(
    equipoId: Long,
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onPruebaGuardadaExitosamente: (Long) -> Unit,
    dbHelper: UsuarioDbHelper = UsuarioDbHelper(LocalContext.current)
) {
    val viewModel: RegistroPruebaViewModel = viewModel(
        key = "reg_$equipoId",
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RegistroPruebaViewModel(dbHelper) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val instrumentos = remember { dbHelper.listarInstrumentos() } // Cargamos los instrumentos

    LaunchedEffect(uiState.protocoloGuardadoId) {
        uiState.protocoloGuardadoId?.let { id ->
            onPruebaGuardadaExitosamente(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Protocolo Equipo ID: $equipoId") },
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
            Text("Técnico: ${usuarioActual?.nombre ?: "N/A"}")

            OutlinedTextField(
                value = uiState.temperaturaAmbiente,
                onValueChange = viewModel::actualizarTemperatura,
                label = { Text("Temperatura Ambiente (°C)") },
                isError = uiState.errorTemperatura != null,
                supportingText = { uiState.errorTemperatura?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.humedadRelativa,
                onValueChange = viewModel::actualizarHumedad,
                label = { Text("Humedad Relativa (%)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Prueba de Aislación (RN-06)", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = uiState.lectura1Min,
                onValueChange = viewModel::actualizarLectura1Min,
                label = { Text("Lectura 1 min (MΩ)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.lectura10Min,
                onValueChange = viewModel::actualizarLectura10Min,
                label = { Text("Lectura 10 min (MΩ)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 👇 MENÚ DESPLEGABLE DE INSTRUMENTOS (RF-05)
            var expandido by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = it }
            ) {
                OutlinedTextField(
                    value = uiState.instrumentoSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Instrumento utilizado (RF-05)") },
                    isError = uiState.errorMedicion != null,
                    supportingText = { uiState.errorMedicion?.let { Text(it) } },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false }
                ) {
                    instrumentos.forEach { inst ->
                        DropdownMenuItem(
                            text = { Text("${inst.nombre} - ${inst.marca}") },
                            onClick = {
                                viewModel.actualizarInstrumento("${inst.nombre} - ${inst.marca}")
                                expandido = false
                            }
                        )
                    }
                }
            }

            if (uiState.resultadoPrueba != "Pendiente") {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        "Resultado IP: ${uiState.resultadoPrueba}",
                        modifier = Modifier.padding(Dimens.espacioMedio),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.guardarProtocolo(
                        equipoId = equipoId,
                        tecnicoId = usuarioActual?.id ?: 1L
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Protocolo (Offline)")
            }
        }
    }
}