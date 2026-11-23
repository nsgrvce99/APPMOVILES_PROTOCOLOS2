package cl.duocuc.miregistro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EsquemaClaro = lightColorScheme(
    primary = Primario,
    onPrimary = Blanco,
    primaryContainer = PrimarioClaro,
    onPrimaryContainer = PrimarioOscuro,
    secondary = Secundario,
    onSecondary = Blanco,
    tertiary = Acento,
    onTertiary = Blanco,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = SuperficieClara,
    onSurface = TextoOscuro,
    surfaceVariant = FondoClaro,
    onSurfaceVariant = TextoSecundario,
    outline = BordeSuave,
    error = ErrorEstado,
    onError = Blanco,
    errorContainer = ErrorFondo,
    onErrorContainer = ErrorEstado
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = PrimarioOscuro,
    primaryContainer = PrimarioOscuro,
    onPrimaryContainer = PrimarioClaro,
    secondary = Color(0xFF94A3B8),
    onSecondary = FondoOscuro,
    tertiary = Color(0xFFFB923C),
    onTertiary = FondoOscuro,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro,
    surfaceVariant = SuperficieOscura,
    onSurfaceVariant = GrisDeshabilitado,
    outline = Color(0xFF334155),
    error = Color(0xFFF87171),
    onError = FondoOscuro,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun MiRegistroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}