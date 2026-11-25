package cl.duocuc.miregistro.network

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import cl.duocuc.miregistro.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

private const val RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK"

@Composable
fun recordarPermisoGateway(): Pair<Boolean, () -> Unit> {
    val host = BuildConfig.INDUZTEK_BASE_URL.toHttpUrlOrNull()?.host
    val requiere = Build.VERSION.SDK_INT >= 37 && (BuildConfig.RED_LOCAL || esHostLocal(host))
    val contexto = LocalContext.current
    var concedido by remember(requiere) {
        mutableStateOf(!requiere || ContextCompat.checkSelfPermission(contexto, RED_LOCAL) == PackageManager.PERMISSION_GRANTED)
    }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido = it }
    return concedido to { permiso.launch(RED_LOCAL) }
}

internal fun esHostLocal(host: String?): Boolean {
    if (host == null) return false
    if (host == "localhost" || host.endsWith(".local") || host == "::1") return true
    if (host.contains(':')) return host.take(2) in listOf("fc", "fd") || host.take(3) in listOf("fe8", "fe9", "fea", "feb")
    val numeros = host.split('.').map { it.toIntOrNull() }
    if (numeros.size != 4 || numeros.any { it == null || it !in 0..255 }) return false
    val segundo = numeros[1] ?: return false
    return when (numeros[0]) {
        10, 127 -> true
        169 -> segundo == 254
        172 -> segundo in 16..31
        192 -> segundo == 168
        else -> false
    }
}
