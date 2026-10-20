package cl.duocuc.miregistro.navigation

// =====================================================================
//  Rutas.kt  —  LAS "DIRECCIONES" DE CADA PANTALLA  (NUEVO, Bloque 1)
//  ---------------------------------------------------------------------
//  Cada pantalla tiene un nombre único, como las URL de una web:
//      "formulario"   "perfil"   "usuarios"
//
//  ¿Por qué sealed class y no Strings sueltos?
//  Si escribo navigate("perfli") por error, compila y la app falla.
//  Con Ruta.Perfil.ruta el compilador revisa el nombre por nosotros.
//
//  CAMBIO (Bloque 4): ruta CON ARGUMENTO  "editar/{indice}".
//  {indice} es un "hueco" que se rellena al navegar: "editar/0", "editar/2"...
//  Así el dato (qué usuario editar) viaja DENTRO de la ruta, como en una URL.
// =====================================================================

sealed class Ruta(val ruta: String) {
    //NUEVA RUTA
    data object Login : Ruta("login")
    // NUEVA RUTA: Pantalla principal de solo lectura para el Cliente (Rol 4)
    data object HistorialCliente : Ruta("historial_cliente")

    data object Instalaciones : Ruta("instalaciones")       // Selección de planta/faena
    data object ListaEquipos : Ruta("equipos/{instalacionId}") {
        fun crear(id: Int) = "equipos/$id"
    }
    data object RegistroPrueba : Ruta("registro_prueba/{equipoId}") {
        fun crear(id: Int) = "registro_prueba/$id"
    }
    data object HistorialEquipo : Ruta("historial/{equipoId}") {
        fun crear(id: Int) = "historial/$id"
    }
    data object Formalizacion : Ruta("formalizacion")         // Rol Ingeniero
}
