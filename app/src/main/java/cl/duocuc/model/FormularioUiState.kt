package cl.duocuc.model
//opciones del radioButton de nivel de experiencia
enum class NivelExperiencia(val etiqueta: String){
    PRINCIPIANTE("Principiante"),
    INTERMEDIO("Intermedio"),
    AVANZADO("Avanzado")
}

//estructura de datos para todo el formulario
data class FormularioUiState(
    //variables para los campos del formulario
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val nivel: NivelExperiencia = NivelExperiencia.PRINCIPIANTE,
    val recibirNoticias: Boolean = false,
    val aceptaTerminos: Boolean = false,

    //estado visual de la clave
    val contrasenaVisible: Boolean = false,
    //errores de validación de cada campo
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorTerminos: String? = null,
    //exitoso
    val registroExitoso: Boolean = false
)
