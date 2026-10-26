package cl.duocuc.miregistro.model

data class Rol(
    val id: Long,
    val codigo: String,
    val nombre: String
) {
    companion object {

        const val ID_TECNICO = 1L
        const val ID_INGENIERO = 2L
        const val ID_JEFE_TECNICO = 3L
        const val ID_CLIENTE = 4L
        const val ROL_DEFAULT_REGISTRO = ID_TECNICO
    }
}