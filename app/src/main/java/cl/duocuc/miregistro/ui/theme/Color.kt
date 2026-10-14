package cl.duocuc.miregistro.ui.theme

import androidx.compose.ui.graphics.Color

// =====================================================================
//  Color.kt  —  "PALETA DE COLORES" (equivale a las variables de CSS)
//  ---------------------------------------------------------------------
//  En CSS:        :root { --primario: #5B3FD9; }
//  En Compose:    val Primario = Color(0xFF5B3FD9)
//
//  Formato: 0xAARRGGBB  ->  AA = opacidad (FF = 100 %), luego rojo, verde, azul.
//  Regla de oro: NUNCA escribir colores sueltos en las pantallas;
//  siempre referenciarlos desde este archivo o desde MaterialTheme.
// =====================================================================

// --- Colores de marca (diseño propio) ---
val Primario = Color(0xFF5B3FD9)        // Morado principal
val PrimarioOscuro = Color(0xFF3A22A8)
val PrimarioClaro = Color(0xFFC9BEFF)
val Secundario = Color(0xFF00A6A6)      // Turquesa
val Acento = Color(0xFFFF6B4A)          // Coral (llamados a la acción)
val AcentoOscuro = Color(0xFFE0482A)

// --- Neutros ---
val Blanco = Color(0xFFFFFFFF)
val FondoClaro = Color(0xFFF6F5FB)
val SuperficieClara = Color(0xFFFFFFFF)
val TextoOscuro = Color(0xFF1C1B22)
val FondoOscuro = Color(0xFF14131A)
val SuperficieOscura = Color(0xFF1F1D27)
val TextoClaro = Color(0xFFECEAF4)
val GrisDeshabilitado = Color(0xFFBDBAC7)

// --- Colores de estado (mensajes al usuario) ---
val Exito = Color(0xFF2E9E5B)           // Verde: operación correcta
val ExitoFondo = Color(0xFFE3F5EA)