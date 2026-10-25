package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Instrumento
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.BotonPrincipal
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionInstrumentosScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var instrumentos by remember { mutableStateOf(dbHelper.listarInstrumentos()) }

    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var serie by remember { mutableStateOf("") }
    var vencimiento by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Gestión de Instrumentos",
                onVolver = onVolver
            )
        }
    )  { innerPadding ->
        // Validación de Rol: Solo Jefe Técnico (ID 3)
        if (usuarioActual?.rolId != 3L) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Acceso denegado. Solo el Jefe Técnico puede gestionar instrumentos.", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Dimens.espacioMedio)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                Text("Registrar Nuevo Instrumento", style = MaterialTheme.typography.titleMedium)

                CampoTexto(valor = nombre, onValorCambia = { nombre = it }, etiqueta = "Nombre (ej. Megóhmetro 5kV)", icono = cl.duocuc.miregistro.R.drawable.ic_persona)
                CampoTexto(valor = marca, onValorCambia = { marca = it }, etiqueta = "Marca", icono = cl.duocuc.miregistro.R.drawable.ic_persona)
                CampoTexto(valor = serie, onValorCambia = { serie = it }, etiqueta = "N° Serie", icono = cl.duocuc.miregistro.R.drawable.ic_persona)
                CampoTexto(valor = vencimiento, onValorCambia = { vencimiento = it }, etiqueta = "Vencimiento (ej. 15-03-2027)", icono = cl.duocuc.miregistro.R.drawable.ic_persona)

                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)

                BotonPrincipal(
                    texto = "Guardar Instrumento",
                    onClick = {
                        if (nombre.isBlank() || vencimiento.isBlank()) {
                            error = "Nombre y fecha de vencimiento son obligatorios (RN-12)"
                        } else {
                            dbHelper.insertarInstrumento(nombre, marca, serie, vencimiento)
                            instrumentos = dbHelper.listarInstrumentos() // Refrescar lista
                            nombre = ""; marca = ""; serie = ""; vencimiento = ""; error = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(Dimens.espacioMedio))
                Text("Instrumentos Registrados", style = MaterialTheme.typography.titleMedium)

                instrumentos.forEach { inst ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(Dimens.espacioMedio)) {
                            Text(inst.nombre, style = MaterialTheme.typography.titleSmall)
                            Text("Marca: ${inst.marca} | Serie: ${inst.serie}")
                            Text("Vence: ${inst.fechaVencimiento}", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}