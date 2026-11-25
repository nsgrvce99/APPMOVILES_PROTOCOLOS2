package cl.duocuc.miregistro.network

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.google.gson.Gson
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AlmacenSesionSeguro(context: Context) : AlmacenSesion {
    private val preferencias = context.getSharedPreferences("sesion_gateway", Context.MODE_PRIVATE)
    private val gson = Gson()

    override fun leer(): SesionGuardada? {
        val datos = preferencias.getString("datos", null) ?: return null
        return try {
            val bytes = Base64.decode(datos, Base64.NO_WRAP)
            check(bytes.size > IV_LONGITUD)
            val cifrado = Cipher.getInstance("AES/GCM/NoPadding")
            cifrado.init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(128, bytes.copyOfRange(0, IV_LONGITUD)))
            val json = cifrado.doFinal(bytes.copyOfRange(IV_LONGITUD, bytes.size))
            gson.fromJson(json.toString(Charsets.UTF_8), SesionGuardada::class.java)
                ?.takeIf { !it.token.isNullOrBlank() && !it.correo.isNullOrBlank() }
        } catch (_: Exception) {
            guardar(null)
            null
        }
    }

    override fun guardar(sesion: SesionGuardada?) {
        if (sesion == null) {
            check(preferencias.edit().remove("datos").commit())
            return
        }
        val cifrado = Cipher.getInstance("AES/GCM/NoPadding")
        cifrado.init(Cipher.ENCRYPT_MODE, clave())
        check(cifrado.iv.size == IV_LONGITUD)
        val bytes = cifrado.iv + cifrado.doFinal(gson.toJson(sesion).toByteArray(Charsets.UTF_8))
        check(preferencias.edit().putString("datos", Base64.encodeToString(bytes, Base64.NO_WRAP)).commit())
    }

    private fun clave(): SecretKey {
        val llavero = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val actual = llavero.getKey(ALIAS, null) as? SecretKey
        if (actual != null) return actual
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build())
            generateKey()
        }
    }

    private companion object {
        const val ALIAS = "induztek_sesion_gateway"
        const val IV_LONGITUD = 12
    }
}
