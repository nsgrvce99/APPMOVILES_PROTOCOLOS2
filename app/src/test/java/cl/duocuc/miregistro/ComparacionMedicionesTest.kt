package cl.duocuc.miregistro

import cl.duocuc.miregistro.model.Medicion
import cl.duocuc.miregistro.model.compararMediciones
import org.junit.Assert.*
import org.junit.Test

class ComparacionMedicionesTest {
    @Test
    fun usaElValorAnteriorComoBaseYElUmbralEsEstrictoEnAmbosSentidos() {
        for ((lectura, esperada, significativa) in listOf(
            Triple("125", 25.0, false),
            Triple("125.01", 25.01, true),
            Triple("75", -25.0, false),
            Triple("74.99", -25.01, true)
        )) {
            val comparacion = checkNotNull(compararMediciones(medicion(lectura), medicion("100")))
            assertEquals(esperada, comparacion.porcentaje.toDouble(), 0.00001)
            assertEquals(significativa, comparacion.significativa)
        }
    }

    @Test
    fun seleccionaTiempoMedidoParaReleYFaseAParaDevanados() {
        val base = medicion("1").copy(tipoPrueba = "RELE", tiempoMedido = "50")
        val rele = checkNotNull(compararMediciones(base.copy(tiempoMedido = "75"), base))
        assertEquals("Tiempo Medido", rele.etiqueta)
        assertTrue(rele.significativa)

        val devanados = base.copy(tipoPrueba = "DEVANADOS", devanadoA = "4")
        val fases = checkNotNull(compararMediciones(devanados.copy(devanadoA = "5"), devanados))
        assertEquals("Fase A", fases.etiqueta)
        assertFalse(fases.significativa)
    }

    @Test
    fun omiteReferenciasIncompatiblesOInvalidas() {
        val actual = medicion("50")
        assertNull(compararMediciones(actual, null))
        assertNull(compararMediciones(actual, medicion("10").copy(tipoPrueba = "TTR")))
        assertNull(compararMediciones(actual, medicion("0")))
        assertNull(compararMediciones(actual, medicion("-1")))
        assertNull(compararMediciones(medicion("NaN"), medicion("10")))
    }

    private fun medicion(lectura: String) = Medicion(
        protocoloId = 7, tipoPrueba = "AISLACION", lectura1Min = lectura,
        lectura10Min = "", indicePolarizacion = ""
    )
}
