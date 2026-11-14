package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.AppTopBar
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenVisitaScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }
    val resumen = remember { dbHelper.obtenerResumenVisita(usuarioActual?.id ?: 0L) }

    Scaffold(
        topBar = { AppTopBar(titulo = "Resumen de Visita", onVolver = onVolver) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hola, ${usuarioActual?.nombre}", style = MaterialTheme.typography.titleLarge)
            Text("Este es tu resumen de terreno:", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(Dimens.espacioMedio))


            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(Dimens.espacioMedio),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total de Pruebas Registradas", style = MaterialTheme.typography.titleMedium)
                    Text("${resumen.totalPruebas}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(Dimens.espacioMedio))


            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                ContadorCard("Aprobadas", resumen.aprobadas, Color(0xFF388E3C), Modifier.weight(1f))
                ContadorCard("Precaución", resumen.precaucion, Color(0xFFF57C00), Modifier.weight(1f))
                ContadorCard("Rechazadas", resumen.rechazadas, Color(0xFFD32F2F), Modifier.weight(1f))
            }

            Spacer(Modifier.height(Dimens.espacioMedio))


            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                ContadorCard("Borradores", resumen.borradores, Color(0xFF757575), Modifier.weight(1f))
                ContadorCard("Pendientes", resumen.pendientesFormalizar, Color(0xFF1976D2), Modifier.weight(1f))
                ContadorCard("Formalizadas", resumen.formalizadas, Color(0xFF7B1FA2), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ContadorCard(titulo: String, cantidad: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(cantidad.toString(), style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = color, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}