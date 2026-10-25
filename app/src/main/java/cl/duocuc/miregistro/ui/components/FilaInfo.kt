package cl.duocuc.miregistro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FilaInfo(
    etiqueta: String,
    valor: String,
    colorValor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = "$etiqueta: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = colorValor
        )
    }
}