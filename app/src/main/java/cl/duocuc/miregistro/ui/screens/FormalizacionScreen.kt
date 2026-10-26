package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Protocolo
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.TarjetaProtocolo
import cl.duocuc.miregistro.ui.components.CampoMultilinea
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormalizacionScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var protocolos by remember { mutableStateOf<List<Protocolo>>(emptyList()) }


    var mostrarDialogoObservar by remember { mutableStateOf(false) }
    var protocoloAObservar by remember { mutableStateOf<Protocolo?>(null) }
    var motivoObservacion by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        protocolos = dbHelper.listarTodosLosProtocolos()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Formalización de Protocolos",
                onVolver = onVolver
            )
        }
    ) { innerPadding ->
        val esIngeniero = usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L

        if (!esIngeniero) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Acceso denegado. Solo Ingenieros o Jefes Técnicos.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            val pendientes = protocolos.filter { it.estado == "REGISTRADO_TERRENO" }

            if (pendientes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay protocolos pendientes de formalización.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(Dimens.espacioMedio),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    items(pendientes) { proto ->
                        TarjetaProtocolo(
                            protocolo = proto,
                            onFormalizar = {
                                dbHelper.actualizarEstadoProtocolo(proto.id, "FORMALIZADO")

                                protocolos = dbHelper.listarTodosLosProtocolos()
                            },
                            onObservar = {
                                protocoloAObservar = proto
                                motivoObservacion = ""
                                mostrarDialogoObservar = true
                            }
                        )
                    }
                }
            }
        }
    }


    if (mostrarDialogoObservar && protocoloAObservar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoObservar = false },
            title = { Text("Observar Protocolo #${protocoloAObservar?.id}") },
            text = {
                Column {
                    Text("Indique el motivo de la observación:")
                    Spacer(Modifier.height(Dimens.espacioMedio))
                    CampoMultilinea(
                        valor = motivoObservacion,
                        onValorCambia = { motivoObservacion = it },
                        etiqueta = "Motivo",
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dbHelper.observarProtocolo(protocoloAObservar!!.id, motivoObservacion)
                        protocolos = dbHelper.listarTodosLosProtocolos()
                        mostrarDialogoObservar = false
                    },
                    enabled = motivoObservacion.isNotBlank()
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoObservar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}