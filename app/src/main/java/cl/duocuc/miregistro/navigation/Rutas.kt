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

    // ---- AUTENTICACIÓN ----
    data object Login : Ruta("login")

    // ---- CLIENTE (Rol 4) ----
    data object HistorialCliente : Ruta("historial_cliente")

    // ---- OPERATIVAS ----
    data object Instalaciones : Ruta("instalaciones")

    // 👇 AQUÍ ESTABA EL ERROR: cambiar Int por Long
    data object ListaEquipos : Ruta("equipos/{instalacionId}") {
        fun crear(instalacionId: Long) = "equipos/$instalacionId"
    }

    data object RegistroPrueba : Ruta("registro_prueba/{equipoId}") {
        fun crear(equipoId: Long) = "registro_prueba/$equipoId"
    }

    data object HistorialEquipo : Ruta("historial/{equipoId}") {
        fun crear(equipoId: Long) = "historial/$equipoId"
    }

    data object Formalizacion : Ruta("formalizacion")

    data object CierreVisita : Ruta("cierre_visita/{protocoloId}") {
        fun crear(protocoloId: Long) = "cierre_visita/$protocoloId"
    }
}
