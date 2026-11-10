package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun CrearUsuarioClienteScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    if (usuarioActual?.rolId != 3L) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Acceso denegado. Solo el Jefe Técnico puede activar cuentas (RN-20).", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    var clientes by remember { mutableStateOf(dbHelper.listarUsuarios().filter { it.rolId == 4L }) }
    var clienteAEditar by remember { mutableStateOf<Usuario?>(null) }
    var nuevaContrasena by remember { mutableStateOf("") }

    Scaffold(
        topBar = { AppTopBar(titulo = "Gestionar Cuentas de Cliente", onVolver = onVolver) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            item { Text("Clientes registrados por terreno:", style = MaterialTheme.typography.titleMedium) }
            items(clientes) { cliente ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Dimens.espacioMedio)) {
                        Text(cliente.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("RUT: ${cliente.rut} | Correo: ${cliente.correo}")

                        if (cliente.activo) {
                            Text("Estado: ACTIVO", color = MaterialTheme.colorScheme.primary)
                        } else {
                            Text("Estado: INACTIVO (Pendiente de clave)", color = MaterialTheme.colorScheme.error)
                            Button(onClick = { clienteAEditar = cliente; nuevaContrasena = "" }) {
                                Text("Asignar Clave y Activar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (clienteAEditar != null) {
        AlertDialog(
            onDismissRequest = { clienteAEditar = null },
            title = { Text("Activar cuenta para ${clienteAEditar?.nombre}") },
            text = {
                Column {
                    Text("El correo ya está registrado: ${clienteAEditar?.correo}")
                    Spacer(Modifier.height(Dimens.espacioChico))
                    // 👇 SOLO PEDIMOS LA CLAVE
                    CampoTexto(
                        valor = nuevaContrasena,
                        onValorCambia = { nuevaContrasena = it },
                        etiqueta = "Contraseña",
                        icono = R.drawable.ic_candado
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nuevaContrasena.isNotBlank()) {
                            dbHelper.activarCliente(clienteAEditar!!.id, nuevaContrasena)
                            // Refrescamos la lista
                            clientes = dbHelper.listarUsuarios().filter { it.rolId == 4L }
                            clienteAEditar = null
                        }
                    },
                    enabled = nuevaContrasena.isNotBlank()
                ) {
                    Text("Guardar y Activar")
                }
            },
            dismissButton = {
                TextButton(onClick = { clienteAEditar = null }) { Text("Cancelar") }
            }
        )
    }
}