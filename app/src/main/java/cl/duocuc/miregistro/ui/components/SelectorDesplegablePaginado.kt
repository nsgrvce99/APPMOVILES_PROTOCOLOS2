package cl.duocuc.miregistro.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.network.mensajeRed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectorDesplegablePaginado(
    etiqueta: String,
    itemSeleccionado: T?,
    textoDe: (T) -> String,
    onBuscar: suspend (String, Int, Int) -> List<T>,
    onItemSeleccionado: (T) -> Unit
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }


    var itemsList by remember { mutableStateOf<List<T>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var offset by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 20
    val alcance = rememberCoroutineScope()
    var errorCarga by remember { mutableStateOf<String?>(null) }


    suspend fun cargarDatos(reset: Boolean = false) {
        if (isLoading) return
        if (reset) {
            offset = 0
            hasMore = true
            itemsList = emptyList()
        }
        if (!hasMore) return

        isLoading = true
        errorCarga = null
        try {
            val nuevosItems = onBuscar(query, limit, offset)
            itemsList = itemsList + nuevosItems
            offset += limit
            hasMore = nuevosItems.size == limit
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            errorCarga = mensajeRed(error)
        } finally {
            isLoading = false
        }
    }


    LaunchedEffect(query, mostrarDialogo) {
        if (mostrarDialogo) {
            if (query.isNotEmpty()) delay(250)
            cargarDatos(reset = true)
        }
    }


    OutlinedCard(
        onClick = { mostrarDialogo = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(Dimens.espacioMedio)) {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(
                text = itemSeleccionado?.let { textoDe(it) } ?: "Seleccionar...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }


    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Seleccionar $etiqueta") },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Buscar...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))


                    val listState = rememberLazyListState()




                    LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
                        items(itemsList) { item ->
                            ListItem(
                                headlineContent = { Text(textoDe(item)) },
                                modifier = Modifier.clickable {
                                    onItemSeleccionado(item)
                                    mostrarDialogo = false
                                }
                            )
                        }
                        if (!isLoading && errorCarga != null) item {
                            Text(checkNotNull(errorCarga), color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = { alcance.launch { cargarDatos() } }) { Text("Reintentar") }
                        }
                        if (!isLoading && errorCarga == null && itemsList.isEmpty()) item { Text("Sin resultados.") }
                        if (!isLoading && errorCarga == null && hasMore && itemsList.isNotEmpty()) item {
                            TextButton(onClick = { alcance.launch { cargarDatos() } }) { Text("Cargar más") }
                        }
                        if (isLoading) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cerrar") }
            }
        )
    }
}