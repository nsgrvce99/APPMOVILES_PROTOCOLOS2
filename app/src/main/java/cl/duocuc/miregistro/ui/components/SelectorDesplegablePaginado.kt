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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectorDesplegablePaginado(
    etiqueta: String,
    itemSeleccionado: T?,
    textoDe: (T) -> String,
    onBuscar: (String, Int, Int) -> List<T>, // query, limit, offset
    onItemSeleccionado: (T) -> Unit
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    // Estados para la paginación
    var itemsList by remember { mutableStateOf<List<T>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var offset by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 20

    // Función para cargar datos (inicial o scroll)
    fun cargarDatos(reset: Boolean = false) {
        if (isLoading) return
        if (reset) {
            offset = 0
            hasMore = true
            itemsList = emptyList()
        }
        if (!hasMore) return

        isLoading = true
        val nuevosItems = onBuscar(query, limit, offset)

        itemsList = itemsList + nuevosItems
        offset += limit
        hasMore = nuevosItems.size == limit // Si trajo menos del límite, ya no hay más
        isLoading = false
    }

    // Cargar la primera vez o al cambiar la búsqueda
    LaunchedEffect(query) {
        cargarDatos(reset = true)
    }

    // El campo que se ve en la pantalla
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

    // El Diálogo con buscador y scroll infinito
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Seleccionar $etiqueta") },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    // Buscador
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Buscar...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))

                    // Lista con Scroll Infinito
                    val listState = rememberLazyListState()

                    // Detectar cuando llega al final de la lista
                    LaunchedEffect(listState) {
                        snapshotFlow {
                            val layoutInfo = listState.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            lastVisibleItem >= totalItems - 3 // Cargar cuando falten 3 para el final
                        }.collect { shouldLoadMore ->
                            if (shouldLoadMore) cargarDatos()
                        }
                    }

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