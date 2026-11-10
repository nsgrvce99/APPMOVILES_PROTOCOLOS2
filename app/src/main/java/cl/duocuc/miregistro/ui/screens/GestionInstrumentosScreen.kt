package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Instrumento
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.BotonPrincipal
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.components.FilaInfo
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionInstrumentosScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    // 👇 VALIDACIÓN DE SEGURIDAD (RN-12: Solo Jefe Técnico)
    if (usuarioActual?.rolId != 3L) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Acceso denegado. Solo el Jefe Técnico puede gestionar instrumentos (RN-12).", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    var instrumentos by remember { mutableStateOf(dbHelper.listarInstrumentos()) }
    var nombre by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var serie by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(titulo = "Gestionar Instrumentos", onVolver = onVolver)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioMedio)
        ) {
            Text("Registrar Nuevo Instrumento", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Dimens.espacioChico))

            CampoTexto(valor = nombre, onValorCambia = { nombre = it }, etiqueta = "Nombre (ej. Megóhmetro)", icono = R.drawable.ic_persona)
            CampoTexto(valor = marca, onValorCambia = { marca = it }, etiqueta = "Marca", icono = R.drawable.ic_persona)
            CampoTexto(valor = serie, onValorCambia = { serie = it }, etiqueta = "Número de Serie", icono = R.drawable.ic_persona)
            CampoTexto(valor = fechaVencimiento, onValorCambia = { fechaVencimiento = it }, etiqueta = "Vencimiento (dd-MM-yyyy)", icono = R.drawable.ic_persona)

            Spacer(Modifier.height(Dimens.espacioChico))
            BotonPrincipal(
                texto = "Guardar Instrumento",
                onClick = {
                    if (nombre.isBlank() || fechaVencimiento.isBlank()) {
                        mensaje = "Nombre y fecha son obligatorios"
                    } else {
                        dbHelper.insertarInstrumento(nombre, marca, serie, fechaVencimiento)
                        instrumentos = dbHelper.listarInstrumentos() // Recargar
                        nombre = ""; marca = ""; serie = ""; fechaVencimiento = ""
                        mensaje = "Instrumento guardado exitosamente"
                    }
                }
            )
            if (mensaje != null) Text(mensaje!!, color = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.height(Dimens.espacioMedio))
            Text("Instrumentos Registrados:", style = MaterialTheme.typography.titleMedium)

            LazyColumn {
                items(instrumentos) { inst ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(Dimens.espacioChico)) {
                            FilaInfo("Nombre", inst.nombre)
                            FilaInfo("Vence", inst.fechaVencimiento)
                        }
                    }
                }
            }
        }
    }
}
