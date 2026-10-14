package cl.duocuc.miregistro.model

data class Rol(
    val id: Long,
    val codigo: String,       // TECNICO, INGENIERO, JEFE_TECNICO, CLIENTE
    val nombre: String        // Descripción del perfil (Anexo 2)
) {
    companion object {
        // IDs precargados en UsuarioDbHelper para Induztek
        const val ID_TECNICO = 1L
        const val ID_INGENIERO = 2L
        const val ID_JEFE_TECNICO = 3L
        const val ID_CLIENTE = 4L

        // Rol por defecto asignado al registrar un nuevo técnico o cuenta en terreno
        const val ROL_DEFAULT_REGISTRO = ID_TECNICO
    }
}