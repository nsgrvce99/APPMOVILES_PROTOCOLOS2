package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Protocolo
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormalizacionScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }
    var protocolos by remember { mutableStateOf<List<Protocolo>>(dbHelper.listarTodosLosProtocolos()) }

    // 👇 Estados para el diálogo de observación (RN-04)
    var protocoloAObservar by remember { mutableStateOf<Long?>(null) }
    var motivoObservacion by remember { mutableStateOf("") }

    // 👇 Diálogo para pedir el motivo de la observación
    if (protocoloAObservar != null) {
        AlertDialog(
            onDismissRequest = { protocoloAObservar = null; motivoObservacion = "" },
            title = { Text("Observar Protocolo") },
            text = {
                Column {
                    Text("Indique el motivo de la observación (RN-04):")
                    OutlinedTextField(
                        value = motivoObservacion,
                        onValueChange = { motivoObservacion = it },
                        label = { Text("Motivo (obligatorio)") },
                        isError = motivoObservacion.isBlank(),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (motivoObservacion.isNotBlank()) {
                            // 👇 Usamos el nuevo método que guarda estado + motivo
                            dbHelper.observarProtocolo(protocoloAObservar!!, motivoObservacion)
                            protocolos = dbHelper.listarTodosLosProtocolos()
                            protocoloAObservar = null
                            motivoObservacion = ""
                        }
                    },
                    enabled = motivoObservacion.isNotBlank()
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { protocoloAObservar = null; motivoObservacion = "" }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Formalización de Protocolos") },
                navigationIcon = { IconButton(onClick = onVolver) { Text("←") } }
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
                    "Acceso denegado. Solo Ingenieros o Jefes Técnicos (Anexo 7).",
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
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(Dimens.espacioMedio)) {
                                Text(
                                    "Protocolo #${proto.id} — Equipo ID: ${proto.equipoId}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("Fecha: ${proto.fecha}")
                                Text("Resultado Terreno: ${proto.resultadoGlobal}")
                                Text("Condiciones: ${proto.temperatura}°C / ${proto.humedad}%")

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Button(onClick = {
                                        dbHelper.actualizarEstadoProtocolo(proto.id, "FORMALIZADO")
                                        protocolos = dbHelper.listarTodosLosProtocolos()
                                    }) {
                                        Text("Formalizar")
                                    }

                                    // 👇 Ahora abre el diálogo en lugar de cambiar el estado directo
                                    OutlinedButton(onClick = {
                                        protocoloAObservar = proto.id
                                        motivoObservacion = ""
                                    }) {
                                        Text("Observar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}