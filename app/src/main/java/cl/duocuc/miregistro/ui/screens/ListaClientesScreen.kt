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
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaClientesScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onEditarCliente: (Usuario) -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var clientes by remember { mutableStateOf(dbHelper.listarClientes()) }
    var clienteAEliminar by remember { mutableStateOf<Usuario?>(null) }

    val puedeVer = usuarioActual?.rolId == 1L || usuarioActual?.rolId == 2L || usuarioActual?.rolId == 3L
    if (!puedeVer) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Acceso denegado.", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    Scaffold(
        topBar = { AppTopBar(titulo = "Directorio de Empresas", onVolver = onVolver) }
    ) { innerPadding ->
        if (clientes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No hay empresas registradas.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                items(clientes) { cliente ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimens.espacioMedio)) {
                            Text(cliente.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("RUT: ${cliente.rut} | Giro: ${cliente.giro ?: "N/A"}")
                            Text("Estado: " + if (cliente.activo) "ACTIVO" else "INACTIVO",
                                color = if (cliente.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                                OutlinedButton(
                                    onClick = { onEditarCliente(cliente) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Editar")
                                }
                                Button(
                                    onClick = { clienteAEliminar = cliente },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    if (clienteAEliminar != null) {
        AlertDialog(
            onDismissRequest = { clienteAEliminar = null },
            title = { Text("¿Eliminar Empresa Cliente?") },
            text = { Text("Se eliminará la empresa '${clienteAEliminar?.nombre}' y todos sus equipos/plantas asociados. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    dbHelper.eliminarCliente(clienteAEliminar!!.id)
                    clientes = dbHelper.listarClientes()
                    clienteAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { clienteAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}