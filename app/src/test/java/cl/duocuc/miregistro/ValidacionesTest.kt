package cl.duocuc.miregistro

import cl.duocuc.miregistro.utils.Validaciones
import org.junit.Assert.*
import org.junit.Test

class ValidacionesTest {
    @Test
    fun normalizaRutYValidaSuDigito() {
        assertEquals("12.345.678-5", Validaciones.formatearRut("123456785"))
        assertEquals("6-K", Validaciones.formatearRut("6k"))
        assertEquals("", Validaciones.formatearRut(""))
        assertTrue(Validaciones.esRutValido("12.345.678-5"))
        assertTrue(Validaciones.esRutValido("6-k"))
        assertTrue(Validaciones.esRutValido("14-0"))
        assertFalse(Validaciones.esRutValido("12.345.678-9"))
        assertFalse(Validaciones.esRutValido("A-7"))
        assertFalse(Validaciones.esRutValido("K"))
    }

    @Test
    fun verificaLimitesYFormatoDeNumeros() {
        assertTrue(Validaciones.esDecimal(""))
        assertTrue(Validaciones.esDecimal("123.45"))
        assertFalse(Validaciones.esDecimal("123.456"))
        assertFalse(Validaciones.esDecimal("1.2.3"))
        assertTrue(Validaciones.esNumero(""))
        assertTrue(Validaciones.esNumero("145"))
        assertFalse(Validaciones.esNumero("1.5"))
    }

    @Test
    fun aplicaValidacionesDeFormulario() {
        assertTrue(Validaciones.esCorreoValido("usuario+demo@example.invalid"))
        assertFalse(Validaciones.esCorreoValido("usuario sin dominio"))
        assertTrue(Validaciones.esTextoValido("Planta 3, módulo-4"))
        assertFalse(Validaciones.esTextoValido("Planta@4"))
        assertTrue(Validaciones.esTagValido("TR-01"))
        assertFalse(Validaciones.esTagValido("tr-01"))
        assertEquals("ABC", Validaciones.limitarTexto("ABCDE", 3))
    }
}