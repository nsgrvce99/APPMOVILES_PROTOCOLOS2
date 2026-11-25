package cl.duocuc.miregistro.network

import cl.duocuc.miregistro.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    fun crear(
        token: () -> String?,
        url: String = BuildConfig.INDUZTEK_BASE_URL,
        permitirHttp: Boolean = BuildConfig.PERMITE_HTTP_GATEWAY,
        onSesionInvalida: (String) -> Unit = {}
    ): InduztekApi {
        val base = (url.trimEnd('/') + "/").toHttpUrl()
        require(base.isHttps || (permitirHttp && esHostLocal(base.host))) {
            "El Gateway exige HTTPS fuera de la red local académica"
        }

        val http = OkHttpClient.Builder()
            .followRedirects(false)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { cadena ->
                val solicitud = cadena.request()
                val jwt = if (solicitud.url.encodedPath.endsWith("/api/auth/login")) null else token()
                val autenticada = if (jwt.isNullOrBlank()) solicitud else solicitud.newBuilder()
                    .header("Authorization", "Bearer $jwt")
                    .build()
                val respuesta = cadena.proceed(autenticada)
                if (jwt != null && respuesta.code == 401) onSesionInvalida(jwt)
                respuesta
            }
            .build()

        val servicio = Retrofit.Builder()
            .baseUrl(base)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InduztekApi::class.java)
        // Compose usa este cliente como llave. El proxy de Retrofit no es igual a sí mismo.
        return object : InduztekApi by servicio {}
    }
}
