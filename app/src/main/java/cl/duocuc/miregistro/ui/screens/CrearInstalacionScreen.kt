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
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.components.BotonPrincipal
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearInstalacionScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var nombre by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Crear Instalación",
                onVolver = onVolver
            )
        }
    ) { innerPadding ->
        // Solo Jefe Técnico puede crear instalaciones (o puedes dejarlo para Técnicos también)
        if (usuarioActual?.rolId != 3L) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Acceso denegado. Solo el Jefe Técnico puede crear instalaciones.", color = MaterialTheme.colorScheme.error)
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
                Text("Nueva Planta / Instalación", style = MaterialTheme.typography.titleMedium)

                CampoTexto(
                    valor = nombre,
                    onValorCambia = { nombre = it; error = null },
                    etiqueta = "Nombre de la instalación (ej. Planta Sur)",
                    icono = R.drawable.ic_persona
                )

                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                if (exito) Text("¡Instalación creada exitosamente!", color = MaterialTheme.colorScheme.primary)

                BotonPrincipal(
                    texto = "Guardar Instalación",
                    onClick = {
                        if (nombre.isBlank()) {
                            error = "El nombre es obligatorio"
                        } else {
                            // Asignamos la instalación al cliente por defecto (ID 5 - Marcela) para el MVP
                            val id = dbHelper.insertarInstalacion(nombre, 5L)
                            if (id > 0) {
                                exito = true
                                nombre = ""
                                error = null
                            } else {
                                error = "Error al crear la instalación."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}