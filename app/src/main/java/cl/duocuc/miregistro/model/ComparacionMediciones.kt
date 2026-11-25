package cl.duocuc.miregistro.model

import java.math.BigDecimal
import java.math.MathContext

internal data class ComparacionMediciones(
    val etiqueta: String,
    val porcentaje: BigDecimal,
    val significativa: Boolean
)

internal fun compararMediciones(actual: Medicion, anterior: Medicion?): ComparacionMediciones? {
    if (anterior == null || anterior.tipoPrueba != actual.tipoPrueba) return null

    val (etiqueta, lecturaActual, lecturaAnterior) = when (actual.tipoPrueba) {
        "AISLACION", "TTR", "CONTACTOS" -> Triple("Lectura 1min", actual.lectura1Min, anterior.lectura1Min)
        "RELE" -> Triple("Tiempo Medido", actual.tiempoMedido, anterior.tiempoMedido)
        "DEVANADOS" -> Triple("Fase A", actual.devanadoA, anterior.devanadoA)
        else -> return null
    }
    val lectura = lecturaActual.toBigDecimalOrNull() ?: return null
    val referencia = lecturaAnterior.toBigDecimalOrNull()?.takeIf { it.signum() > 0 } ?: return null
    val diferencia = lectura.subtract(referencia)
    return ComparacionMediciones(
        etiqueta,
        diferencia.movePointRight(2).divide(referencia, MathContext.DECIMAL64),
        diferencia.abs().multiply(BigDecimal.valueOf(4)) > referencia
    )
}
