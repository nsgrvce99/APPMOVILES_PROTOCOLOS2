package cl.duocuc.miregistro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duocuc.miregistro.model.Equipo
import cl.duocuc.miregistro.ui.theme.Dimens

@Composable
fun TarjetaEquipo(
    equipo: Equipo,
    onVerHistorial: () -> Unit,
    onRegistrarPrueba: (() -> Unit)? = null
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Dimens.espacioMedio)) {
            Text("${equipo.tipoEquipo} ${equipo.tag}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Dimens.espacioMini))
            FilaInfo("Marca", equipo.marca)
            FilaInfo("Serie", equipo.serie)
            FilaInfo("Instalación", equipo.instalacion)

            Spacer(Modifier.height(Dimens.espacioMedio))

            if (onRegistrarPrueba != null) {
                Button(
                    onClick = onRegistrarPrueba,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar Prueba")
                }
                Spacer(Modifier.height(Dimens.espacioMini))
            }

            OutlinedButton(
                onClick = onVerHistorial,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver Historial (RF-10)")
            }
        }
    }
}