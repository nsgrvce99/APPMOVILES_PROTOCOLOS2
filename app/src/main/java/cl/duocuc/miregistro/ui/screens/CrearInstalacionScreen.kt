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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearInstalacionScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    // Permiso para Técnico (1), Ingeniero (2) y Jefe (3)
    val puedeCrear = usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L
    if (!puedeCrear) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Acceso denegado.", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    // Cargamos la lista de clientes desde SQLite
    val clientes = remember { dbHelper.listarClientes() }

    var nombreInstalacion by remember { mutableStateOf("") }
    var clienteSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(titulo = "Crear Instalación", onVolver = onVolver) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioMedio)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Text("Nueva Planta / Instalación", style = MaterialTheme.typography.titleMedium)

            // 👇 SELECTOR DE CLIENTE (Obligatorio)
            if (clientes.isEmpty()) {
                Text(
                    "⚠️ No hay empresas clientes registradas. Primero debes registrar una empresa en 'Registrar Empresa Cliente'.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                SelectorDesplegable(
                    etiqueta = "Empresa Cliente Dueña de la Planta",
                    opciones = clientes,
                    seleccionada = clienteSeleccionado,
                    onSeleccionar = { clienteSeleccionado = it; error = null },
                    textoDe = { "${it.nombre} (${it.rut})" } // Muestra el nombre y RUT de la empresa
                )
            }

            // Nombre de la planta
            CampoTexto(
                valor = nombreInstalacion,
                onValorCambia = { nombreInstalacion = it; error = null },
                etiqueta = "Nombre de la instalación (ej. Planta Lampa)",
                icono = R.drawable.ic_persona
            )

            MensajeError(texto = error)
            if (exito) MensajeExito(titulo = "¡Instalación creada!", detalles = listOf("Ya está disponible en la lista."))

            BotonPrincipal(
                texto = "Guardar Instalación",
                onClick = {
                    if (nombreInstalacion.isBlank()) {
                        error = "El nombre de la planta es obligatorio"
                    } else if (clienteSeleccionado == null) {
                        error = "Debes asociar la planta a una empresa cliente"
                    } else {
                        // 👇 AHORA USAMOS EL ID DEL CLIENTE SELECCIONADO
                        val id = dbHelper.insertarInstalacion(nombreInstalacion, clienteSeleccionado!!.id)
                        if (id > 0) {
                            exito = true
                            nombreInstalacion = ""
                            clienteSeleccionado = null
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