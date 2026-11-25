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
import cl.duocuc.miregistro.network.*
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
    val api = LocalApiTerreno.current
    val dbHelper = remember(api) { if (api == null) UsuarioDbHelper(context) else null }
    val operacion = recordarOperacion()
    var errorTipo by remember { mutableStateOf<String?>(null) }

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
        FondoFooter(
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.espacioPantalla)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                Text("Complete los datos del equipo.", style = MaterialTheme.typography.titleMedium)
                Text("Instalación: $nombreInstalacion", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)

                CampoValidado(
                    valor = tag,
                    onValorCambia = { tag = Validaciones.limitarTexto(it).uppercase(); errorTag = null },
                    etiqueta = "TAG del equipo.",
                    icono = R.drawable.ic_candado,
                    error = errorTag
                )

                SelectorDesplegablePaginado(
                    etiqueta = "Instalación / Planta",
                    itemSeleccionado = plantaSeleccionada,
                    textoDe = { it.second },
                    onBuscar = { query, limit, offset ->

                        if (api == null) checkNotNull(dbHelper).listarInstalacionesPaginado(query, limit, offset) else api.instalaciones(query, offset / limit, limit).content.map { it.id to it.nombre }
                    },
                    onItemSeleccionado = { planta ->
                        plantaSeleccionada = planta
                    }
                )

                CampoValidado(
                    valor = marca,
                    onValorCambia = { marca = Validaciones.limitarTexto(it); errorMarca = null },
                    etiqueta = "Marca",
                    icono = R.drawable.ic_candado,
                    error = errorMarca
                )

                CampoValidado(
                    valor = serie,
                    onValorCambia = { serie = Validaciones.limitarTexto(it); errorSerie = null },
                    etiqueta = "Número de serie",
                    icono = R.drawable.ic_candado,
                    error = errorSerie
                )

                SelectorDesplegable(
                    etiqueta = "Tipo de equipo",
                    opciones = tiposEquipo,
                    seleccionada = tipoEquipo.ifBlank { null },
                    onSeleccionar = { tipoEquipo = it; errorTipo = null },
                    textoDe = { it }
                )
                MensajeError(texto = errorTipo ?: operacion.error)

                if (exito) {
                    MensajeExito(
                        titulo = "Equipo registrado exitosamente",
                        detalles = listOf("TAG: $tag", "Tipo: $tipoEquipo", "Instalación: $nombreInstalacion")
                    )
                }

                BotonPrincipal(
                    habilitado = !operacion.ejecutando,
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
                                dbHelper?.existeTagEnInstalacion(tag, plantaSeleccionada?.second.orEmpty()) == true -> errorTag = "El TAG '$tag' ya existe en esta instalación."
                                marca.isBlank() -> errorMarca = "La marca es obligatoria"
                                !Validaciones.esTextoValido(marca) -> errorMarca = "Caracteres inválidos"
                                serie.isBlank() -> errorSerie = "El número de serie es obligatorio"
                                !Validaciones.esTextoValido(serie) -> errorSerie = "Caracteres inválidos"
                                tipoEquipo.isBlank() -> errorTipo = "Selecciona el tipo de equipo"
                                plantaSeleccionada == null -> errorTipo = "Selecciona la instalación"
                                else -> {
                                    val (plantaId, nombrePlanta) = checkNotNull(plantaSeleccionada)
                                    if (api != null) {
                                        operacion.ejecutar {
                                            api.crearEquipo(EquipoSolicitud(tag.trim(), tipoEquipo, marca.trim(), serie.trim(), plantaId))
                                            exito = true
                                        }
                                    } else {
                                    val local = checkNotNull(dbHelper)
                                    val idClienteReal = local.obtenerIdClienteDeInstalacion(nombrePlanta)
                                    val nuevoEquipo = Equipo(
                                        tag = tag.trim(),
                                        tipoEquipo = tipoEquipo,
                                        clasificacion = "Primario",
                                        instalacion = nombrePlanta,
                                        marca = marca.trim(),
                                        serie = serie.trim(),
                                        potenciaTension = "",
                                        idCliente = idClienteReal
                                    )
                                    val idInsertado = local.insertarEquipo(nuevoEquipo)
                                    if (idInsertado > 0) {
                                        exito = true
                                    } else {
                                        errorSerie = "Error al guardar. Intente nuevamente."
                                    }
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
}