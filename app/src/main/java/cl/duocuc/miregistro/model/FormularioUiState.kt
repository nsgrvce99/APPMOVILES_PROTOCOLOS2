package cl.duocuc.miregistro.model

// =====================================================================
//  FormularioUiState.kt  —  ESTADO DE LA PANTALLA 1  (la "M" de MVVM)
//  ---------------------------------------------------------------------
//  Todo lo que la pantalla necesita para dibujarse: lo escrito en cada
//  campo, las opciones marcadas y los mensajes de error.
//  Es una data class INMUTABLE (val): para cambiarla se crea una copia
//  con .copy(...). Así Compose detecta el cambio y vuelve a dibujar.
//
//  CAMBIO (Bloque 2): campos "ciudad" (la elegida) y "ciudades" (opciones).
// =====================================================================

/** Opciones del grupo de RadioButton. "etiqueta" es el texto visible. */
enum class NivelExperiencia(val etiqueta: String) {
    PRINCIPIANTE("Principiante"),
    INTERMEDIO("Intermedio"),
    AVANZADO("Avanzado")
}

data class FormularioUiState(
    // Valores de los campos
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val ciudad: String = "Santiago",               // NUEVO (Bloque 2): valor inicial
    val ciudades: List<String> = listOf("Santiago", "Valparaíso", "Concepción", "Temuco"), // NUEVO (Bloque 2)
    val nivel: NivelExperiencia = NivelExperiencia.PRINCIPIANTE,
    val recibirNoticias: Boolean = false,
    val aceptaTerminos: Boolean = false,

    // Estado visual
    val contrasenaVisible: Boolean = false,

    // Errores de validación (null = sin error)
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorTerminos: String? = null,

    // Resultado
    val registroExitoso: Boolean = false
)
