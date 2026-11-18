package cl.duocuc.miregistro.utils

object Validaciones {

    // 1. Solo números (para temperatura, humedad, lecturas)
    fun esNumero(valor: String): Boolean = valor.isEmpty() || valor.matches(Regex("^[0-9]*$"))

    // 2. Solo números decimales (por si acaso)
    fun esDecimal(valor: String): Boolean = valor.isEmpty() || valor.matches(Regex("^[0-9]*\\.?[0-9]*$"))

    // 3. Correo electrónico
    fun esCorreoValido(correo: String): Boolean =
        correo.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))

    // 4. RUT (formato simple: números, puntos, guion y K)
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

    // 5. Texto general (letras, números, guiones, puntos, espacios)
    fun esTextoValido(texto: String): Boolean =
        texto.isEmpty() || texto.matches(Regex("^[A-Za-z0-9áéíóúÁÉÍÓÚñÑ\\s.,-]*$"))

    // 6. TAG (letras mayúsculas, números y guiones)
    fun esTagValido(tag: String): Boolean =
        tag.isEmpty() || tag.matches(Regex("^[A-Z0-9-]*$"))

    fun formatearRut(rut: String): String {
        // 1. Limpiamos el RUT: quitamos puntos, guiones y espacios
        val rutLimpio = rut.replace(".", "").replace("-", "").trim()

        // 2. Si está vacío o es muy corto, lo devolvemos tal cual
        if (rutLimpio.length < 2) return rut

        // 3. Separamos el cuerpo (dígitos) y el dígito verificador (último caracter)
        val cuerpo = rutLimpio.substring(0, rutLimpio.length - 1)
        val dv = rutLimpio.substring(rutLimpio.length - 1).uppercase()

        // 4. Formateamos el cuerpo con puntos
        val cuerpoFormateado = cuerpo.reversed().chunked(3).joinToString(".").reversed()

        // 5. Retornamos el RUT con el formato final
        return "$cuerpoFormateado-$dv"
    }
}