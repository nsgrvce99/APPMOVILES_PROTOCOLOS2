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
import cl.duocuc.miregistro.ui.components.BotonPrincipal
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Función de validación de RUT (RN-03)
fun validarRut(rutCompleto: String): Boolean {
    val rutLimpio = rutCompleto.replace(".", "").replace("-", "").trim()
    if (rutLimpio.length < 8 || rutLimpio.length > 9) return false

    val cuerpo = rutLimpio.substring(0, rutLimpio.length - 1)
    val dvIngresado = rutLimpio.last().toString().uppercase()

    if (!cuerpo.all { it.isDigit() }) return false

    var suma = 0
    var multiplicador = 2
    for (i in cuerpo.reversed()) {
        suma += i.toString().toInt() * multiplicador
        multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
    }

    val dvCalculado = 11 - (suma % 11)
    val dvEsperado = when (dvCalculado) {
        11 -> "0"
        10 -> "K"
        else -> dvCalculado.toString()
    }

    return dvIngresado == dvEsperado
}

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
                        // Validaciones al presionar el botón
                        if (nombre.isBlank() || rut.isBlank()) {
                            error = "Nombre y RUT son obligatorios (HU-08)"
                        } else if (!validarRut(rut)) {
                            error = "RUT inválido. Verifique el dígito verificador (RN-03)"
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