package cl.duocuc.miregistro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.miregistro.BuildConfig
import cl.duocuc.miregistro.network.AlmacenSesionSeguro
import cl.duocuc.miregistro.network.CambioContrasena
import cl.duocuc.miregistro.network.EstadoSesion
import cl.duocuc.miregistro.network.RetrofitClient
import cl.duocuc.miregistro.network.SesionRepositorio
import kotlinx.coroutines.launch

class SesionGatewayViewModel(app: Application) : AndroidViewModel(app) {
    private val repositorio = SesionRepositorio(AlmacenSesionSeguro(app))
    private val configuracion = runCatching {
        RetrofitClient.crear(repositorio::token, onSesionInvalida = repositorio::invalidar)
    }

    val errorConfiguracion = configuracion.exceptionOrNull()?.let {
        if (BuildConfig.PERMITE_HTTP_GATEWAY) "Configura una dirección local o HTTPS válida para el Gateway."
        else "Configura la dirección HTTPS del Gateway para esta versión."
    }
    val api = configuracion.getOrNull()
    val estado = repositorio.estado

    init { restaurar() }

    fun restaurar() {
        val servicio = api ?: return
        viewModelScope.launch { repositorio.restaurar(servicio) }
    }

    suspend fun iniciar(correo: String, clave: String): LoginViewModel.ResultadoIngreso {
        repositorio.entrar(checkNotNull(api), correo, clave)
        val sesion = estado.value
        return LoginViewModel.ResultadoIngreso(
            (sesion as? EstadoSesion.Activa)?.usuario?.rolId ?: 0,
            sesion == EstadoSesion.CambioObligatorio
        )
    }

    suspend fun cambiar(datos: CambioContrasena) = repositorio.cambiarContrasena(checkNotNull(api), datos)
    suspend fun recargarPerfil() = repositorio.actualizarPerfil(checkNotNull(api))
    fun salir() { viewModelScope.launch { repositorio.cerrar() } }
}
