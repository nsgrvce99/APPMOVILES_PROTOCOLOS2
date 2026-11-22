package cl.duocuc.miregistro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.model.Medicion
import cl.duocuc.miregistro.model.ProtocoloDetallado
import cl.duocuc.miregistro.ui.theme.Dimens

@Composable
fun TarjetaProtocoloDetalle(
    protocolo: ProtocoloDetallado,
    medicion: Medicion?,
    onFormalizar: () -> Unit,
    onObservar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Dimens.espacioMedio)) {

            Text(
                "${protocolo.equipoTipo} ${protocolo.equipoTag}",
                style = MaterialTheme.typography.titleLarge
            )
            Text("Planta: ${protocolo.instalacion}", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(Dimens.espacioChico))
            HorizontalDivider()
            Spacer(Modifier.height(Dimens.espacioChico))


            Text(" Técnico: ${protocolo.tecnicoNombre}", style = MaterialTheme.typography.bodyMedium)
            Text(" Fecha: ${protocolo.fecha}", style = MaterialTheme.typography.bodyMedium)

            val colorResultado = if (protocolo.resultadoGlobal == "Aprobado") MaterialTheme.colorScheme.primary
            else if (protocolo.resultadoGlobal == "Precaucion") MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.error

            Text(" Resultado: ${protocolo.resultadoGlobal}", color = colorResultado, style = MaterialTheme.typography.titleMedium)


            if (medicion != null) {
                Spacer(Modifier.height(Dimens.espacioChico))
                Text("Mediciones registradas:", style = MaterialTheme.typography.labelLarge)

                when (medicion.tipoPrueba) {
                    "AISLACION" -> {
                        Text("Lectura 1 min: ${medicion.lectura1Min} MΩ", style = MaterialTheme.typography.bodyMedium)
                        Text("Lectura 10 min: ${medicion.lectura10Min} MΩ", style = MaterialTheme.typography.bodyMedium)
                    }
                    "TTR" -> {
                        Text("Relación Teórica: ${medicion.lectura1Min}", style = MaterialTheme.typography.bodyMedium)
                        Text("Relación Medida: ${medicion.lectura10Min}", style = MaterialTheme.typography.bodyMedium)
                    }
                    "CONTACTOS" -> {
                        Text("Resistencia Medida: ${medicion.lectura1Min} μΩ", style = MaterialTheme.typography.bodyMedium)
                        Text("Máximo Placa: ${medicion.lectura10Min} μΩ", style = MaterialTheme.typography.bodyMedium)
                    }
                    "RELE" -> {
                        Text("Corriente de Ajuste: ${medicion.corrienteAjuste} A", style = MaterialTheme.typography.bodyMedium)
                        Text("Corriente de Operación: ${medicion.corrienteOperacion} A", style = MaterialTheme.typography.bodyMedium)
                        Text("Tiempo Esperado: ${medicion.tiempoEsperado} ms", style = MaterialTheme.typography.bodyMedium)
                        Text("Tiempo Medido: ${medicion.tiempoMedido} ms", style = MaterialTheme.typography.bodyMedium)
                    }
                    "DEVANADOS" -> {
                        Text("Fase A: ${medicion.devanadoA} mΩ", style = MaterialTheme.typography.bodyMedium)
                        Text("Fase B: ${medicion.devanadoB} mΩ", style = MaterialTheme.typography.bodyMedium)
                        Text("Fase C: ${medicion.devanadoC} mΩ", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }


            Spacer(Modifier.height(Dimens.espacioMedio))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onFormalizar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Formalizar")
                }
                OutlinedButton(
                    onClick = onObservar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Observar")
                }
            }
        }
    }
}