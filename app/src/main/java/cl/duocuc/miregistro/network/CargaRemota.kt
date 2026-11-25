package cl.duocuc.miregistro.network

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response

val LocalApiTerreno = staticCompositionLocalOf<InduztekApi?> { null }

sealed interface EstadoCarga<out T> {
    data object Cargando : EstadoCarga<Nothing>
    data class Lista<T>(val valor: T) : EstadoCarga<T>
    data class Error(val mensaje: String) : EstadoCarga<Nothing>
}

@Composable
fun <T> cargar(vararg llaves: Any?, consulta: suspend () -> T): State<EstadoCarga<T>> =
    produceState<EstadoCarga<T>>(EstadoCarga.Cargando, *llaves) {
        value = EstadoCarga.Cargando
        try {
            value = EstadoCarga.Lista(consulta())
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            value = EstadoCarga.Error(mensajeRed(error))
        }
    }

@Composable
fun <T> ContenidoRemoto(estado: EstadoCarga<T>, alReintentar: () -> Unit, contenido: @Composable (T) -> Unit) {
    when (estado) {
        EstadoCarga.Cargando -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is EstadoCarga.Error -> Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = alReintentar) { Text("Reintentar") }
        }
        is EstadoCarga.Lista -> contenido(estado.valor)
    }
}

class OperacionUi(private val scope: CoroutineScope) {
    var ejecutando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun ejecutar(bloque: suspend () -> Unit) {
        if (ejecutando) return
        ejecutando = true
        error = null
        scope.launch {
            try {
                bloque()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                this@OperacionUi.error = mensajeRed(error)
            } finally {
                ejecutando = false
            }
        }
    }
}

@Composable
fun recordarOperacion(): OperacionUi {
    val scope = rememberCoroutineScope()
    return remember(scope) { OperacionUi(scope) }
}

suspend fun <T> todasLasPaginas(consultar: suspend (Int) -> Pagina<T>): List<T> {
    val primera = consultar(0)
    return buildList {
        addAll(primera.content)
        for (pagina in 1 until primera.page.totalPages) addAll(consultar(pagina).content)
    }
}

fun Response<*>.verificar() {
    if (!isSuccessful) throw HttpException(this)
}
