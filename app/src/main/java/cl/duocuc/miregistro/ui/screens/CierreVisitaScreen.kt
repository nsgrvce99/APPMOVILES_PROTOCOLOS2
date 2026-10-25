package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // Validación de RUT (RN-03: módulo 11)
    fun validarRut(rutCompleto: String): Boolean {
        val rutLimpio = rutCompleto.replace(".", "").replace("-", "").trim().uppercase()
        if (rutLimpio.length < 2) return false

        val dvIngresado = rutLimpio.last()
        val cuerpo = rutLimpio.dropLast(1)
        if (!cuerpo.all { it.isDigit() }) return false

        var suma = 0
        var multiplicador = 2
        for (i in cuerpo.length - 1 downTo 0) {
            suma += cuerpo[i].digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }

        val resto = suma % 11
        val dvEsperado = 11 - resto
        val dvString = when (dvEsperado) {
            11 -> "0"
            10 -> "K"
            else -> dvEsperado.toString()
        }
        return dvString == dvIngresado.toString()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Cierre de Visita y Acta",
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
                "Registre los datos de quien recibe el Acta de Terreno (RF-08)",
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

            // 👇 Usamos CampoMultilinea para el cargo
            CampoMultilinea(
                valor = cargo,
                onValorCambia = { cargo = it },
                etiqueta = "Cargo",
                minLines = 2
            )

            MensajeError(texto = error)

            if (exito) {
                MensajeExito(
                    titulo = "Acta entregada exitosamente",
                    detalles = listOf(
                        "Receptor: $nombre",
                        "RUT: $rut",
                        "Estado: Pendiente de formalización (RN-14)"
                    )
                )
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
                        } else if (!validarRut(rut)) {
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