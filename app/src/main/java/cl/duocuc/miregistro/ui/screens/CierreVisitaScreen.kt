package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CierreVisitaScreen(
    protocoloId: Long,
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onActaEntregada: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    // 1. Validación de permisos (Solo Técnico e Ingeniero)
    val puedeFirmarActa = usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L

    // 2. 👇 OBTENEMOS EL CLIENTE DUEÑO DEL EQUIPO USANDO EL PROTOCOLO
    val clienteDueno = remember(protocoloId) {
        dbHelper.obtenerClienteDeProtocolo(protocoloId)
    }

    var nombreReceptor by remember { mutableStateOf("") }
    var rutReceptor by remember { mutableStateOf("") }
    var cargoReceptor by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

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
        topBar = { AppTopBar(titulo = "Cierre de Visita y Acta", onVolver = onVolver) }
    ) { innerPadding ->
        if (!puedeFirmarActa) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Acceso denegado. Solo Técnicos e Ingenieros pueden entregar el Acta.", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Dimens.espacioPantalla)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                Text("Acta de Terreno", style = MaterialTheme.typography.titleMedium)

                // 👇 MOSTRAMOS LA EMPRESA CLIENTE (Pre-cargada)
                if (clienteDueno != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(Modifier.padding(Dimens.espacioMedio)) {
                            Text("Empresa Cliente (Dueña de la Planta):", style = MaterialTheme.typography.labelMedium)
                            Text(clienteDueno.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("RUT Empresa: ${clienteDueno.rut}")
                        }
                    }
                }

                Text(
                    "Datos de quien recibe el documento en representación de la empresa:",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Los campos del receptor siguen siendo manuales
                CampoTexto(
                    valor = nombreReceptor,
                    onValorCambia = { nombreReceptor = it; error = null },
                    etiqueta = "Nombre de quien recibe",
                    icono = R.drawable.ic_persona
                )
                CampoTexto(
                    valor = rutReceptor,
                    onValorCambia = { rutReceptor = it; error = null },
                    etiqueta = "RUT de quien recibe",
                    icono = R.drawable.ic_persona
                )
                CampoMultilinea(
                    valor = cargoReceptor,
                    onValorCambia = { cargoReceptor = it },
                    etiqueta = "Cargo de quien recibe",
                    minLines = 2
                )

                MensajeError(texto = error)
                if (exito) {
                    MensajeExito(
                        titulo = "Acta entregada exitosamente",
                        detalles = listOf(
                            "Empresa: ${clienteDueno?.nombre ?: "N/A"}",
                            "Receptor: $nombreReceptor",
                            "Estado: Pendiente de formalización"
                        )
                    )
                }

                BotonPrincipal(
                    texto = if (exito) "Volver" else "Entregar Acta al Cliente",
                    onClick = {
                        if (exito) {
                            onActaEntregada()
                        } else {
                            if (nombreReceptor.isBlank() || rutReceptor.isBlank()) {
                                error = "Nombre y RUT de quien recibe son obligatorios"
                            } else if (!validarRut(rutReceptor)) {
                                error = "RUT inválido"
                            } else {
                                val fechaHora = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                                dbHelper.guardarRecepcionActa(
                                    protocoloId = protocoloId,
                                    nombre = nombreReceptor,
                                    rut = rutReceptor,
                                    cargo = cargoReceptor,
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
}