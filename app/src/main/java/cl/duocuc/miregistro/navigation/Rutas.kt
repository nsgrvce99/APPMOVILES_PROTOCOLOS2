package cl.duocuc.miregistro.navigation

sealed class Ruta(val ruta: String) {


    data object Login : Ruta("login")


    data object Perfil : Ruta("perfil")


    data object HistorialCliente : Ruta("historial_cliente")
    data object MisEquiposCliente : Ruta("mis_equipos")


    data object Instalaciones : Ruta("instalaciones")

    data object ListaEquipos : Ruta("equipos/{instalacionId}/{nombreInstalacion}") {
        fun crear(instalacionId: Long, nombre: String): String {

            val nombreCodificado = java.net.URLEncoder.encode(nombre, "UTF-8")
            return "equipos/$instalacionId/$nombreCodificado"
        }
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

    data object RegistrarEquipo : Ruta("registrar_equipo/{instalacionId}/{nombreInstalacion}") {
        fun crear(instalacionId: Long, nombre: String): String {
            val nombreCodificado = java.net.URLEncoder.encode(nombre, "UTF-8")
            return "registrar_equipo/$instalacionId/$nombreCodificado"
        }
    }
    data object GestionInstrumentos : Ruta("gestion_instrumentos")

    data object CrearCliente : Ruta("crear_cliente")
    data object CrearInstalacion : Ruta("crear_instalacion")
    data object RegistrarCliente : Ruta("registrar_cliente")
    data object MisProtocolos : Ruta("mis_protocolos")
    data object EditarProtocolo : Ruta("editar_protocolo/{protocoloId}") {
        fun crear(protocoloId: Long) = "editar_protocolo/$protocoloId"
    }

    data object ListaClientes : Ruta("lista_clientes")
    data object EditarCliente : Ruta("editar_cliente/{clienteId}") {
        fun crear(clienteId: Long) = "editar_cliente/$clienteId"
    }
    data object DetalleProtocolo : Ruta("detalle_protocolo/{protocoloId}") {
        fun crear(protocoloId: Long) = "detalle_protocolo/$protocoloId"
    }
    data object ResumenVisita : Ruta("resumen_visita")

}
