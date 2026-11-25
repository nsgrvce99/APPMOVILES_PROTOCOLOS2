package cl.duocuc.miregistro.network

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

const val PREFIJO_FOTO = "data:image/jpeg;base64,"
const val MAXIMO_FOTO = 44_000

suspend fun codificarFoto(resolver: ContentResolver, uri: Uri): String = withContext(Dispatchers.IO) {
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }
    require(medidas.outWidth > 0 && medidas.outHeight > 0)
    var muestra = 1
    while ((maxOf(medidas.outWidth, medidas.outHeight).toLong() + muestra - 1) / muestra > 512) muestra *= 2
    val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
    val foto = requireNotNull(resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) })
    try {
        for (calidad in listOf(70, 50, 30, 15)) {
            val salida = ByteArrayOutputStream()
            check(foto.compress(Bitmap.CompressFormat.JPEG, calidad, salida))
            val fotoUri = PREFIJO_FOTO + Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
            if (fotoUri.length <= MAXIMO_FOTO) return@withContext fotoUri
        }
        throw IllegalArgumentException("La foto excede el tamaño permitido")
    } finally {
        foto.recycle()
    }
}
