package cl.duocuc.miregistro

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URI

@RunWith(AndroidJUnit4::class)
class GatewayClienteInstrumentadoTest {
    private val instrumentacion = InstrumentationRegistry.getInstrumentation()
    private val interfaz by lazy { instrumentacion.uiAutomation }

    @Test
    fun ingresaYVeElEquipoRealConSuHistorialFormalizado() {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue("Se requiere una cuenta temporal de integración",
            args.containsKey("correo") && args.containsKey("clave"))
        val contexto = instrumentacion.targetContext

        etapa("red local") {
            if (android.os.Build.VERSION.SDK_INT >= 37) {
                check(contexto.checkSelfPermission("android.permission.ACCESS_LOCAL_NETWORK") == 0)
            }
            val conexion = URI.create(BuildConfig.INDUZTEK_BASE_URL + "actuator/health").toURL()
                .openConnection() as HttpURLConnection
            conexion.connectTimeout = 7_000
            conexion.readTimeout = 7_000
            try {
                check(conexion.responseCode == 200) { "Estado HTTP " + conexion.responseCode }
            } finally {
                conexion.disconnect()
            }
        }

        etapa("entrada de credenciales") {
            instrumentacion.startActivitySync(
                Intent(contexto, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            escribir(esperar(2) { it.isEditable }[0], checkNotNull(args.getString("correo")))
            escribir(esperar(2) { it.isEditable }[1], checkNotNull(args.getString("clave")))
            pulsar(esperar { it.text?.toString() == "Iniciar Sesión" }.first())
        }

        etapa("listado propio") {
            esperar { it.text?.contains("TR-INTEGRACION") == true }
        }
        etapa("historial formalizado") {
            pulsar(esperar { it.text?.contains("Ver Historial") == true }.first())
            esperar { it.text?.contains("Formalizado") == true }
            capturarPantalla("cliente")
        }
    }

    @Test
    fun tecnicoConsultaLaInstalacionYAbreElRegistroDePrueba() {
        entrarDespuesDeCerrarLaSesionActual("Técnico")
        etapa("planta del recorrido") {
            pulsar(esperar { it.text?.toString() == "Instalaciones" }.first())
            pulsar(esperar { it.text?.toString() == "Plantas" }.first())
            escribir(esperar { it.isEditable }.first(), "Planta Integracion")
            pulsar(esperar { !it.isEditable && it.text?.toString() == "Planta Integracion" }.first())
        }
        etapa("registro de la prueba") {
            esperar { it.text?.contains("TR-INTEGRACION") == true }
            pulsar(esperar { it.text?.toString() == "Registrar Prueba" }.first())
            esperar { it.text?.toString() == "Protocolo de Equipo: TR-INTEGRACION" }
            esperar {
                it.text?.contains("Temperatura Ambiente") == true ||
                    it.hintText?.contains("Temperatura Ambiente") == true
            }
            capturarPantalla("tecnico")
        }
        if (opcion("escrituraMovil")) etapa("guardado y entrega desde Android") {
            escribirCampo("Temperatura Ambiente", "18.25")
            escribirCampo("Humedad Relativa", "22.0")
            escribirCampo("Lectura 1 min", "15.00")
            escribirCampo("Lectura 10 min", "30")
            pulsar(esperar(desplazar = true) { etiqueta(it, "Instrumento utilizado") }.first())
            pulsar(esperar(desplazar = true) { it.text?.startsWith("Megohmetro Demo -") == true }.first())
            interfaz.waitForIdle(500, 5_000)
            tocar(esperar(desplazar = true) { it.text?.toString() == "Guardar Protocolo" && controlPulsable(it) != null }.first())
            val resultado = esperar(desplazar = true) {
                val texto = it.text?.toString().orEmpty()
                texto == "Acta de Terreno" || texto.startsWith("Temperatura y humedad") ||
                    texto.startsWith("Debe seleccionar") || texto.startsWith("El instrumento") ||
                    texto.startsWith("Ingresa una temperatura") || texto.startsWith("No se pudo") ||
                    texto.startsWith("Revisa los datos") || texto.startsWith("No tienes permiso") ||
                    texto.startsWith("El servicio no está disponible") || texto.startsWith("La operación está protegida")
            }.first().text?.toString()
            check(resultado == "Acta de Terreno") { "El formulario informó: $resultado" }
            tocar(esperar(desplazar = true) {
                it.text?.toString() == "Editar protocolo" && controlPulsable(it) != null
            }.first())
            esperar { it.text?.toString() == "Protocolo de Equipo: TR-INTEGRACION" }
            esperar { it.isEditable && it.text?.toString() == "22.0" }
            esperar { it.isEditable && it.text?.toString() == "15.00" }
            escribirCampo("Humedad Relativa", "55.0")
            tocar(esperar(desplazar = true) {
                it.text?.toString() == "Actualizar Protocolo" && controlPulsable(it) != null
            }.first())
            esperar { it.text?.toString() == "Acta de Terreno" }
            tocar(esperar(desplazar = true) { it.text?.toString() == "Confirmar Entrega de Acta" && controlPulsable(it) != null }.first())
            esperar(desplazar = true) { it.text?.contains("Acta entregada exitosamente") == true }
            capturarPantalla("acta-entregada")
        }
    }

    @Test
    fun ingenieroAbreLaBandejaRemotaDeFormalizacion() {
        entrarDespuesDeCerrarLaSesionActual("Ingeniero")
        etapa("bandeja de formalización del Ingeniero") {
            pulsar(esperar { it.text?.toString() == "Reportes y Pruebas" }.first())
            pulsar(esperar { it.text?.toString() == "Ir a Formalización de Protocolos" }.first())
            esperar { it.text?.toString() == "Formalización de Protocolos" }
            if (opcion("escrituraMovil")) {
                pulsar(esperar(desplazar = true) { it.text?.toString() == "Formalizar" }.first())
                esperar { it.text?.contains("Variación Significativa Detectada") == true }
                capturarPantalla("ingeniero")
                escribir(esperar { it.isEditable }.first(), "Validacion movil QA")
                tocar(esperar { it.text?.toString() == "Formalizar con Comentario" && controlPulsable(it) != null }.first())
                esperar { it.text?.toString() == "No hay protocolos pendientes de formalización." }
                capturarPantalla("formalizacion")
            } else {
                esperar { it.text?.toString() == "No hay protocolos pendientes de formalización." }
                capturarPantalla("ingeniero")
            }
        }
    }

    @Test
    fun jefeTecnicoEncuentraElInstrumentoEnElCatalogoRemoto() {
        entrarDespuesDeCerrarLaSesionActual("Jefe técnico")
        etapa("catálogo remoto del Jefe Técnico") {
            pulsar(esperar { it.text?.toString() == "Administración" }.first())
            pulsar(esperar { it.text?.toString() == "Gestionar Instrumentos." }.first())
            pulsar(esperar { it.text?.toString() == "Instrumentos Registrados" }.first())
            escribir(esperar { it.isEditable }.first(), "Megohmetro Demo")
            esperar { !it.isEditable && it.text?.contains("Megohmetro Demo") == true }
            capturarPantalla("jefe-tecnico")
        }
    }

    private fun opcion(nombre: String): Boolean =
        InstrumentationRegistry.getArguments().getString(nombre) == "true"

    private fun etiqueta(nodo: AccessibilityNodeInfo, inicio: String): Boolean =
        nodo.text?.startsWith(inicio) == true || nodo.hintText?.startsWith(inicio) == true

    private fun escribirCampo(nombre: String, valor: String) = escribir(campo(nombre), valor)

    private fun campo(nombre: String): AccessibilityNodeInfo {
        val rotulo = esperar(desplazar = true) { etiqueta(it, nombre) }.first()
        val areaRotulo = Rect().also(rotulo::getBoundsInScreen)
        return esperar {
            val area = Rect().also(it::getBoundsInScreen)
            it.actionList.any { accion -> accion.id == AccessibilityNodeInfo.ACTION_SET_TEXT } &&
                Rect.intersects(area, areaRotulo)
        }.first()
    }

    private fun capturarPantalla(nombre: String) {
        if (InstrumentationRegistry.getArguments().getString("evidenciaVisual") != "true") return
        val salida = interfaz.executeShellCommand("screencap -p /data/local/tmp/induztek-$nombre.png")
        ParcelFileDescriptor.AutoCloseInputStream(salida).use { it.readBytes() }
    }

    private fun entrarDespuesDeCerrarLaSesionActual(rol: String) {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue("Se requiere una sesión previa del recorrido",
            args.getString("sesionPreviaActiva") == "true")
        etapa("cierre de la sesión previa") {
            val contexto = instrumentacion.targetContext
            instrumentacion.startActivitySync(
                Intent(contexto, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            pulsar(esperar { it.contentDescription?.toString() == "Abrir menú" }.first())
            pulsar(esperar { it.text?.toString() == "Cerrar sesión" }.first())
            esperar { it.text?.toString() == "¿Cerrar sesión?" }
            pulsar(esperar { it.text?.toString() == "Cerrar sesión" }.first())
        }
        etapa("entrada de $rol") {
            escribir(esperar(2) { it.isEditable }[0], checkNotNull(args.getString("correo")))
            escribir(esperar(2) { it.isEditable }[1], checkNotNull(args.getString("clave")))
            pulsar(esperar { it.text?.toString() == "Iniciar Sesión" }.first())
            esperar { it.text?.startsWith("Rol: $rol", ignoreCase = true) == true }
        }
    }

    private fun esperar(
        cantidad: Int = 1,
        desplazar: Boolean = false,
        coincide: (AccessibilityNodeInfo) -> Boolean
    ): List<AccessibilityNodeInfo> {
        val limite = SystemClock.uptimeMillis() + 40_000
        do {
            val nodos = mutableListOf<AccessibilityNodeInfo>()
            var scroll: AccessibilityNodeInfo? = null
            fun buscar(nodo: AccessibilityNodeInfo) {
                if (desplazar && nodo.isVisibleToUser && nodo.isScrollable) scroll = nodo
                if (nodo.isVisibleToUser && coincide(nodo)) nodos.add(nodo)
                for (i in 0 until nodo.childCount) nodo.getChild(i)?.let(::buscar)
            }
            interfaz.rootInActiveWindow?.let(::buscar)
            if (nodos.size >= cantidad) return nodos
            if (desplazar) scroll?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            SystemClock.sleep(if (desplazar) 550 else 250)
        } while (SystemClock.uptimeMillis() < limite)
        throw AssertionError("Se agotó la espera de un control de la interfaz")
    }

    private fun escribir(nodo: AccessibilityNodeInfo, valor: String) {
        val argumentos = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, valor)
        }
        check(nodo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, argumentos))
    }

    private fun controlPulsable(nodo: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var actual: AccessibilityNodeInfo? = nodo
        while (actual != null) {
            if (actual.isEnabled && actual.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK }) return actual
            actual = actual.parent
        }
        return null
    }

    private fun tocar(nodo: AccessibilityNodeInfo) {
        val boton = checkNotNull(controlPulsable(nodo))
        val area = Rect().also(boton::getBoundsInScreen)
        check(!area.isEmpty)
        val salida = interfaz.executeShellCommand("input tap ${area.centerX()} ${area.centerY()}")
        ParcelFileDescriptor.AutoCloseInputStream(salida).use { it.readBytes() }
    }

    private fun pulsar(nodo: AccessibilityNodeInfo) {
        val control = controlPulsable(nodo)
        check(control?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
            "El control no admite un clic"
        }
    }

    private fun etapa(nombre: String, prueba: () -> Unit) {
        try {
            prueba()
        } catch (error: Exception) {
            throw AssertionError("Etapa $nombre: ${error.javaClass.simpleName}", error)
        } catch (error: AssertionError) {
            throw AssertionError("Etapa $nombre: ${error.javaClass.simpleName}", error)
        }
    }
}
