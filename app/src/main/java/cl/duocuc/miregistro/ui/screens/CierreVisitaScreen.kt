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
import cl.duocuc.miregistro.ui.components.BotonPrincipal
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CierreVisitaScreen(
    protocoloId: Long,
    onVolver: () -> Unit,
    onActaEntregada: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var nombre by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var cargo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    // Validación simple de RUT (formato XX.XXX.XXX-X o sin puntos)
    fun rutValido(r: String): Boolean {
        val limpio = r.replace(".", "").replace("-", "").trim()
        return limpio.length >= 8 && limpio.length <= 9
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cierre de Visita y Acta") },
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
                "Registre los datos de la persona que recibe el Acta de Terreno (RF-08)",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "El Acta se entregará marcada como 'Pendiente de formalización' (RN-14)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            CampoTexto(
                valor = nombre,
                onValorCambia = { nombre = it; error = null },
                etiqueta = "Nombre del receptor",
                icono = R.drawable.ic_persona
            )
            CampoTexto(
                valor = rut,
                onValorCambia = { rut = it; error = null },
                etiqueta = "RUT (ej. 14.226.791-8)",
                icono = R.drawable.ic_persona
            )
            CampoTexto(
                valor = cargo,
                onValorCambia = { cargo = it },
                etiqueta = "Cargo",
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
                        Text("✓ Acta entregada exitosamente", style = MaterialTheme.typography.titleMedium)
                        Text("Receptor: $nombre")
                        Text("RUT: $rut")
                        Text("Cargo: $cargo")
                        Spacer(Modifier.height(Dimens.espacioMedio))
                        Text(
                            "Estado: Pendiente de formalización (RN-14)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            BotonPrincipal(
                texto = if (exito) "Volver" else "Entregar Acta al Cliente",
                onClick = {
                    if (exito) {
                        onActaEntregada()
                    } else {
                        // Validaciones
                        if (nombre.isBlank() || rut.isBlank()) {
                            error = "Nombre y RUT son obligatorios (HU-08)"
                        } else if (!rutValido(rut)) {
                            error = "RUT inválido. Verifique el formato (RN-03)"
                        } else {
                            val fechaHora = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                            dbHelper.guardarRecepcionActa(
                                protocoloId = protocoloId,
                                nombre = nombre,
                                rut = rut,
                                cargo = cargo,
                                fechaHora = fechaHora
                            )
                            exito = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}