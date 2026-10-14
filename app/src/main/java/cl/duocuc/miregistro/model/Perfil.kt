package cl.duocuc.miregistro.model

// =====================================================================
//  Perfil.kt  —  MODELO DE DATOS de un usuario (pantalla 2)
//  y PerfilUiState — estado de la pantalla de perfil.
//  ---------------------------------------------------------------------
//  Diferencia importante:
//   - Perfil        = los DATOS del negocio (vendrían de una BD o API).
//   - PerfilUiState = lo que la PANTALLA necesita (datos + estado visual,
//                     como si el usuario presionó "Seguir").
// =====================================================================

data class Perfil(
    val id: Long,
    val rut: String,
    val nombre: String,
    val correo: String,
    val rolNombre: String,          // Ej: "P-01 Técnico de terreno" o "Cliente"
    val activo: Boolean,
    val giro: String? = null,       // Solo si es rol CLIENTE (Ej: Agroindustria)
    val cargo: String? = null       // Cargo en la empresa cliente o en Induztek
)

data class PerfilUiState(
    val perfil: Perfil?,
    val cargando: Boolean = false,
    val mensajeError: String? = null
)
