package cl.duocuc.miregistro.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme

@Composable
fun AvatarCircular(
    @DrawableRes imagenPorDefecto: Int = R.drawable.avatar_usuario,
    descripcion: String,
    fotoUri: String? = null,
    modifier: Modifier = Modifier
) {
    val modifierFinal = modifier
        .size(Dimens.tamanoAvatar)
        .shadow(elevation = 6.dp, shape = CircleShape)
        .clip(CircleShape)
        .border(Dimens.bordeAvatar, MaterialTheme.colorScheme.surface, CircleShape)

    if (fotoUri != null) {

        AsyncImage(
            model = fotoUri,
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            modifier = modifierFinal
        )
    } else {

        Image(
            painter = painterResource(id = imagenPorDefecto),
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            modifier = modifierFinal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarCircularPreview() {
    MiRegistroTheme {
        AvatarCircular(
            descripcion = "",
            modifier = Modifier.padding(16.dp)
        )
    }
}