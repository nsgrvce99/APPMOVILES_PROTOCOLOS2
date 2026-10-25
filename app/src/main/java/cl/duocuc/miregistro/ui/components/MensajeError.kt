package cl.duocuc.miregistro.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duocuc.miregistro.ui.theme.Dimens

@Composable
fun MensajeError(
    texto: String?,
    modifier: Modifier = Modifier
) {
    if (!texto.isNullOrBlank()) {
        Text(
            text = texto,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.padding(top = Dimens.espacioMini)
        )
    }
}