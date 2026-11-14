package cl.duocuc.miregistro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.model.ProtocoloDetallado
import cl.duocuc.miregistro.ui.theme.Dimens

@Composable
fun TarjetaMisProtocolos(
    protocolo: ProtocoloDetallado,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onVerDetalle: () -> Unit,
    onCerrarBorrador: () -> Unit
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

            Text("📅 Fecha: ${protocolo.fecha}", style = MaterialTheme.typography.bodyMedium)


            val (textoEstado, colorEstado) = when (protocolo.estado) {
                "BORRADOR" -> "Borrador (Sin cerrar)" to MaterialTheme.colorScheme.secondary // 👈 NUEVO ESTADO
                "REGISTRADO_TERRENO" -> "Pendiente de formalización" to MaterialTheme.colorScheme.primary
                "FORMALIZADO" -> "Formalizado" to MaterialTheme.colorScheme.tertiary
                "PRECAUCION", "OBSERVADO" -> "Precaución (Requiere revisión)" to MaterialTheme.colorScheme.error
                else -> protocolo.estado to MaterialTheme.colorScheme.onSurface
            }
            Text("Estado: $textoEstado", color = colorEstado, style = MaterialTheme.typography.bodyMedium)

            val colorResultado = if (protocolo.resultadoGlobal == "Aprobado") MaterialTheme.colorScheme.primary
            else if (protocolo.resultadoGlobal == "Precaucion") MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.error
            Text("Resultado: ${protocolo.resultadoGlobal}", color = colorResultado, style = MaterialTheme.typography.titleMedium)


            if (protocolo.estado != "FORMALIZADO" && protocolo.motivoObservacion != null && protocolo.motivoObservacion!!.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "📝 Motivo: ${protocolo.motivoObservacion}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(Dimens.espacioMedio))


            if (protocolo.estado == "BORRADOR") {

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCerrarBorrador,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cerrar y Enviar")
                    }
                    Button(
                        onClick = onEliminar,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Eliminar")
                    }
                }
            } else if (protocolo.estado != "FORMALIZADO") {

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onEditar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Editar")
                    }
                    Button(
                        onClick = onEliminar,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Eliminar")
                    }
                }
            } else {

                Text(
                    "🔒 Protocolo Formalizado (No editable)",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onVerDetalle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver Detalle de la Prueba")
            }

        }
    }
}