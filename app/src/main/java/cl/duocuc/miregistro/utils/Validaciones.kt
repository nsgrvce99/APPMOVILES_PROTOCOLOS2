package cl.duocuc.miregistro.utils

object Validaciones {
    const val CLAVE_ACTIVACION_CLIENTE = "Induztek2026!"

    fun esClaveTemporalLocal(valor: String?): Boolean =
        valor == CLAVE_ACTIVACION_CLIENTE || valor == "Induztek2026" || valor == "clave" ||
            valor?.startsWith("PENDIENTE_") == true


    fun esNumero(valor: String): Boolean = valor.isEmpty() || valor.matches(Regex("^[0-9]*$"))

    fun limitarTexto(texto: String, max: Int = 20): String = texto.take(max)


    fun esDecimal(valor: String, maxDigitos: Int = 5): Boolean {
        if (valor.isEmpty()) return true
        // Solo dígitos y como máximo un punto
        if (!valor.matches(Regex("^[0-9]*\\.?[0-9]*$"))) return false
        // Contar dígitos sin contar el punto
        val digitos = valor.count { it.isDigit() }
        return digitos <= maxDigitos
    }

    // 3. Correo electrónico
    fun esCorreoValido(correo: String): Boolean =
        correo.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))

    // 4. RUT
    fun esRutValido(rut: String): Boolean {
        val rutLimpio = rut.replace(".", "").replace("-", "").trim().uppercase()
        if (rutLimpio.length < 2) return false
        val dvIngresado = rutLimpio.last()
        val cuerpo = rutLimpio.dropLast(1)
        if (!cuerpo.all { it.isDigit() }) return false
        var suma = 0
        var multiplicador = 2
        for (i in cuerpo.length - 1 downTo 0) {
            suma += cuerpo[i].digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val resto = suma % 11
        val dvEsperado = 11 - resto
        val dvString = when (dvEsperado) {
            11 -> "0"
            10 -> "K"
            else -> dvEsperado.toString()
        }
        return dvString == dvIngresado.toString()
    }

    // 5. Texto general
    fun esTextoValido(texto: String): Boolean =
        texto.isEmpty() || texto.matches(Regex("^[A-Za-z0-9áéíóúÁÉÍÓÚñÑ\\s.,-]*$"))

    // 6. TAG
    fun esTagValido(tag: String): Boolean =
        tag.isEmpty() || tag.matches(Regex("^[A-Z0-9-]*$"))

    fun formatearRut(rut: String): String {
        val rutLimpio = rut.replace(".", "").replace("-", "").trim()
        if (rutLimpio.length < 2) return rut
        val cuerpo = rutLimpio.substring(0, rutLimpio.length - 1)
        val dv = rutLimpio.substring(rutLimpio.length - 1).uppercase()
        val cuerpoFormateado = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$cuerpoFormateado-$dv"
    }
}