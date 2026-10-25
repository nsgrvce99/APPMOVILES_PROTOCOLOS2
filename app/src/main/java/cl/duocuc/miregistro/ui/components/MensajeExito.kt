package cl.duocuc.miregistro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.ui.theme.Exito
import cl.duocuc.miregistro.ui.theme.ExitoFondo

@Composable
fun MensajeExito(
    titulo: String,
    detalles: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ExitoFondo, contentColor = Exito)
    ) {
        Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Exito,
                    modifier = Modifier.size(Dimens.tamanoIcono)
                )
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = Dimens.espacioChico)
                )
            }
            Spacer(Modifier.height(Dimens.espacioChico))
            detalles.forEach { detalle ->
                Text(text = detalle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}