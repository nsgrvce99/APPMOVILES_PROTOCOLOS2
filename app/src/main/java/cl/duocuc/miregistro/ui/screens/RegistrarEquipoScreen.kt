package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Equipo
import cl.duocuc.miregistro.ui.components.*
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.utils.Validaciones
import cl.duocuc.miregistro.ui.components.SelectorDesplegablePaginado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarEquipoScreen(
    instalacionId: Long,
    nombreInstalacion: String,
    onVolver: () -> Unit,
    onEquipoRegistrado: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { UsuarioDbHelper(context) }

    var tag by remember { mutableStateOf("") }
    var tipoEquipo by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var serie by remember { mutableStateOf("") }

    var errorTag by remember { mutableStateOf<String?>(null) }
    var errorMarca by remember { mutableStateOf<String?>(null) }
    var errorSerie by remember { mutableStateOf<String?>(null) }

    var exito by remember { mutableStateOf(false) }
    var plantaSeleccionada by remember {

        mutableStateOf<Pair<Long, String>?>(instalacionId.takeIf { it > 0 }?.let { Pair(it, nombreInstalacion) })
    }
    val tiposEquipo = listOf("Transformador", "Interruptor", "Celda MT", "Cable de poder", "Relé de protección")

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Registrar Nuevo Equipo",
                onVolver = onVolver
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioPantalla)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Text("Complete los datos del equipo (RF-02 / HU-01)", style = MaterialTheme.typography.titleMedium)
            Text("Instalación: $nombreInstalacion", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)

            CampoValidado(
                valor = tag,
                onValorCambia = { tag = it.uppercase(); errorTag = null },
                etiqueta = "TAG del equipo (ej. TR-03)",
                icono = R.drawable.ic_candado,
                error = errorTag
            )

            SelectorDesplegablePaginado(
                etiqueta = "Instalación / Planta",
                itemSeleccionado = plantaSeleccionada,
                textoDe = { it.second },
                onBuscar = { query, limit, offset ->

                    dbHelper.listarInstalacionesPaginado(query, limit, offset)
                },
                onItemSeleccionado = { planta ->
                    plantaSeleccionada = planta
                }
            )

            CampoValidado(
                valor = marca,
                onValorCambia = { marca = it; errorMarca = null },
                etiqueta = "Marca",
                icono = R.drawable.ic_candado,
                error = errorMarca
            )

            CampoValidado(
                valor = serie,
                onValorCambia = { serie = it; errorSerie = null },
                etiqueta = "Número de serie",
                icono = R.drawable.ic_candado,
                error = errorSerie
            )

            if (exito) {
                MensajeExito(
                    titulo = "Equipo registrado exitosamente",
                    detalles = listOf("TAG: $tag", "Tipo: $tipoEquipo", "Instalación: $nombreInstalacion")
                )
            }

            BotonPrincipal(
                texto = if (exito) "Volver a la lista" else "Registrar Equipo",
                onClick = {
                    if (exito) {
                        onEquipoRegistrado()
                    } else {

                        errorTag = null
                        errorMarca = null
                        errorSerie = null

                        when {
                            tag.isBlank() -> errorTag = "El TAG es obligatorio"
                            !Validaciones.esTagValido(tag) -> errorTag = "Solo letras mayúsculas, números y guiones"
                            dbHelper.existeTagEnInstalacion(tag, nombreInstalacion) -> errorTag = "El TAG '$tag' ya existe en esta instalación (RN-01)"
                            marca.isBlank() -> errorMarca = "La marca es obligatoria"
                            !Validaciones.esTextoValido(marca) -> errorMarca = "Caracteres inválidos"
                            serie.isBlank() -> errorSerie = "El número de serie es obligatorio"
                            !Validaciones.esTextoValido(serie) -> errorSerie = "Caracteres inválidos"
                            else -> {
                                val idClienteReal = dbHelper.obtenerIdClienteDeInstalacion(nombreInstalacion)
                                val nuevoEquipo = Equipo(
                                    tag = tag.trim(),
                                    tipoEquipo = tipoEquipo,
                                    clasificacion = "Primario",
                                    instalacion = nombreInstalacion,
                                    marca = marca.trim(),
                                    serie = serie.trim(),
                                    potenciaTension = "",
                                    idCliente = idClienteReal
                                )
                                val idInsertado = dbHelper.insertarEquipo(nuevoEquipo)
                                if (idInsertado > 0) {
                                    exito = true
                                } else {
                                    errorSerie = "Error al guardar. Intente nuevamente."
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}