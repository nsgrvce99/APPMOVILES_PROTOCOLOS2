package cl.duocuc.miregistro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.ui.theme.Dimens

@Composable
fun AcordeonSeccion(
    titulo: String,
    icono: ImageVector,
    expandido: Boolean,
    onToggle: () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val rotacion by animateFloatAsState(
        targetValue = if (expandido) 180f else 0f,
        label = "rotacion_flecha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(Dimens.espacioMedio),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expandido) "Cerrar" else "Abrir",
                    modifier = Modifier.rotate(rotacion)
                )
            }

            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Dimens.espacioMedio, end = Dimens.espacioMedio, bottom = Dimens.espacioMedio)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                    contenido()
                }
            }
        }
    }
}