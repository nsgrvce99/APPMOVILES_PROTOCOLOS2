package cl.duocuc.ui.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import cl.duocuc.ui.theme.Dimens
import cl.duocuc.ui.theme.GrisDeshabilitado

object EstilosCampo {
    val forma = RoundedCornerShape(Dimens.radioCampo)
    @Composable
    fun colores(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary, //input:focus
        unfocusedBorderColor = GrisDeshabilitado,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        errorBorderColor = MaterialTheme.colorScheme.error,
        cursorColor = MaterialTheme.colorScheme.primary
    )
}