package cl.duocuc.miregistro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.model.Protocolo
import cl.duocuc.miregistro.ui.theme.Dimens

@Composable
fun TarjetaProtocolo(
    protocolo: Protocolo,
    onFormalizar: () -> Unit,
    onObservar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Dimens.espacioMedio)) {
            Text(
                "Protocolo #${protocolo.id} — Equipo ID: ${protocolo.equipoId}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(Dimens.espacioMini))
            FilaInfo("Fecha", protocolo.fecha)
            FilaInfo("Resultado", protocolo.resultadoGlobal)
            FilaInfo("T° / Humedad", "${protocolo.temperatura}°C / ${protocolo.humedad}%")
            FilaInfo("Estado", protocolo.estado)

            Spacer(Modifier.height(Dimens.espacioMedio))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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