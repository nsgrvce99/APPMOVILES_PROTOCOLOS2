package cl.duocuc.miregistro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.model.ProtocoloDetallado
import cl.duocuc.miregistro.ui.theme.Dimens

@Composable
fun TarjetaProtocoloDetalle(
    protocolo: ProtocoloDetallado,
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

            // Detalles del técnico y la prueba
            Text(" Técnico: ${protocolo.tecnicoNombre}", style = MaterialTheme.typography.bodyMedium)
            Text(" Fecha: ${protocolo.fecha}", style = MaterialTheme.typography.bodyMedium)

            val colorResultado = if (protocolo.resultadoGlobal == "Aprobado") MaterialTheme.colorScheme.primary
            else if (protocolo.resultadoGlobal == "Precaucion") MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.error

            Text(" Resultado: ${protocolo.resultadoGlobal}", color = colorResultado, style = MaterialTheme.typography.titleMedium)

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