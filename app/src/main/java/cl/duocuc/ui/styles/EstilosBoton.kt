package cl.duocuc.ui.styles

import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.ui.theme.Blanco
import cl.duocuc.ui.theme.Dimens
import cl.duocuc.ui.theme.GrisDeshabilitado
import cl.duocuc.ui.theme.Primario

object EstilosBoton{
    val forma = RoundedCornerShape(Dimens.radioBoton)
    val relleno = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    //botón principal
    @Composable
    fun coloresPrincipal(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Primario, //background-color
        contentColor = Blanco, //color
        disabledContainerColor = GrisDeshabilitado,
        disabledContentColor = Blanco
    )
    //botones secundarios
    @Composable
    fun bordeSecundario(): BorderStroke = BorderStroke(Dimens.bordeBoton, MaterialTheme.colorScheme.secondary)
    //sombra del boton principal
    @Composable
    fun coloresSecundario(): ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    )
    @Composable
    fun elevacion(): ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = Dimens.elevacionBoton,
        pressedElevation = 1.dp
    )

}

fun Modifier.estiloAltoBoton(): Modifier = this.height(Dimens.alturaBoton)
