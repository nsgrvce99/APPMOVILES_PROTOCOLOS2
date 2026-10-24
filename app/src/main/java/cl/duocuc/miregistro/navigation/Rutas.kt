package cl.duocuc.miregistro.navigation

sealed class Ruta(val ruta: String) {

    // ---- AUTENTICACIÓN ----
    data object Login : Ruta("login")

    // ---- PERFIL DE USUARIO ----
    data object Perfil : Ruta("perfil")   // 👈 NUEVO

    // ---- CLIENTE (Rol 4) ----
    data object HistorialCliente : Ruta("historial_cliente")
    data object MisEquiposCliente : Ruta("mis_equipos")

    // ---- OPERATIVAS ----
    data object Instalaciones : Ruta("instalaciones")

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

    data object RegistrarEquipo : Ruta("registrar_equipo/{instalacionId}") {
        fun crear(instalacionId: Long) = "registrar_equipo/$instalacionId"
    }
    data object GestionInstrumentos : Ruta("gestion_instrumentos")

    data object CrearCliente : Ruta("crear_cliente")


}
