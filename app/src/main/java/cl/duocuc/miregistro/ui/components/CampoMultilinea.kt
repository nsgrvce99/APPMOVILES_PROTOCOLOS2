package cl.duocuc.miregistro.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.ui.styles.EstilosCampo
import cl.duocuc.miregistro.ui.theme.Dimens

@Composable
fun CampoMultilinea(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    minLines: Int = 3
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        minLines = minLines,
        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores()
    )
}