package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.theme.Dimens

// =====================================================================
//  UsuariosScreen.kt  —  PANTALLA 3: LISTA DE USUARIOS  (NUEVO, Bloque 3)
//  ---------------------------------------------------------------------
//  ¿Column + forEach o LazyColumn?
//   - Column dibuja TODOS los elementos, aunque no se vean.
//   - LazyColumn ("perezosa") dibuja SOLO los visibles y recicla al
//     hacer scroll (es el RecyclerView de Compose). Trae su propio
//     scroll: NO se le agrega verticalScroll.
//
//  items(lista) { x -> ... }  = "un elemento por cada dato de la lista".
//
//  ListItem = fila de Material 3 ya armada con 3 zonas:
//     leadingContent (ícono) | headlineContent (título)
//                            | supportingContent (subtítulo)
//
//  No tiene ViewModel propio: recibe la lista desde el ViewModel compartido.
//
//  CAMBIO (Bloque 4): al tocar una tarjeta se abre el formulario de EDICIÓN.
//  itemsIndexed entrega también la POSICIÓN (0, 1, 2...) de cada usuario:
//  ese índice es el dato que viaja en la ruta "editar/{indice}".
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)   // TopAppBar aún es "experimental"
@Composable
fun UsuariosScreen(
    usuarios: List<Usuario>,
    onVolver: () -> Unit,
    onEditar: (Int) -> Unit = {}            // NUEVO (Bloque 4): entrega el índice tocado
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_usuarios)) },
                navigationIcon = {                    // flecha ← (igual que en Perfil)
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.cd_volver)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.espacioMedio),              // margen de la lista
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)  // espacio entre tarjetas
        ) {
            // Una Card por cada usuario
            // CAMBIO (Bloque 4): itemsIndexed -> (posición, usuario)
            itemsIndexed(usuarios) { indice, usuario ->
                Card(onClick = { onEditar(indice) }) {     // toda la tarjeta es presionable
                    ListItem(
                        leadingContent = {
                            Icon(painterResource(R.drawable.ic_persona), contentDescription = null)
                        },
                        headlineContent = { Text(usuario.nombre) },
                        supportingContent = { Text("${usuario.correo} · Rol: ${usuario.rolNombre ?: "Personal Induztek"}") }
                    )
                }
            }
        }
    }
}
