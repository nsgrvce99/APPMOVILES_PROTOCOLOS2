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
fun CrearUsuarioClienteScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onEditarCliente: (Usuario) -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    if (usuarioActual?.rolId != 3L) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Acceso denegado. Solo el Jefe Técnico puede gestionar clientes (RN-20).", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    var clientes by remember { mutableStateOf(dbHelper.listarUsuarios().filter { it.rolId == 4L }) }
    var clienteAEliminar by remember { mutableStateOf<Usuario?>(null) }

    fun recargar() {
        clientes = dbHelper.listarUsuarios().filter { it.rolId == 4L }
    }

    Scaffold(
        topBar = { AppTopBar(titulo = "Gestión de Clientes", onVolver = onVolver) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            item { Text("Clientes registrados en el sistema:", style = MaterialTheme.typography.titleMedium) }
            items(clientes) { cliente ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Dimens.espacioMedio)) {
                        Text(cliente.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("RUT: ${cliente.rut} | Correo: ${cliente.correo}")
                        Text(
                            "Estado: " + if (cliente.activo) "ACTIVO" else "INACTIVO",
                            color = if (cliente.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )


                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                        ) {
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


                        if (!cliente.activo) {
                            Button(
                                onClick = {

                                    dbHelper.activarCliente(cliente.id, "Induztek2026")
                                    recargar()
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Text("Activar Cuenta (Clave: Induztek2026)")
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
            title = { Text("¿Eliminar cliente?") },
            text = { Text("Se eliminará la empresa '${clienteAEliminar?.nombre}'. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    dbHelper.eliminarCliente(clienteAEliminar!!.id)
                    recargar()
                    clienteAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { clienteAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}
