package cl.duocuc.miregistro

import cl.duocuc.miregistro.network.esHostLocal
import org.junit.Assert.*
import org.junit.Test

class PermisoGatewayTest {
    @Test
    fun solicitaPermisoSoloParaDireccionesLocalesReconocidas() {
        listOf("10.0.2.2","172.16.0.2","172.31.0.2","192.168.1.9","127.0.0.1","localhost",
            "puerta.local","169.254.1.2","::1","fe80::1234","fd00::1").forEach { assertTrue(it, esHostLocal(it)) }
        listOf("api.example.invalid","172.32.0.2","192.169.1.1","169.253.0.1","10.300.2.2","fcdemo.example.invalid")
            .forEach { assertFalse(it, esHostLocal(it)) }
        assertFalse(esHostLocal(null))
    }
}
