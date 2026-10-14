package cl.duocuc.miregistro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duocuc.miregistro.navigation.AppNavegacion
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme

// =====================================================================
//  MainActivity.kt  —  PUNTO DE ENTRADA DE LA APP
//  ---------------------------------------------------------------------
//      setContent {
//          Tema {              <- colores / tipografía / formas
//              AppNavegacion() <- el NavHost decide qué pantalla mostrar
//          }
//      }
//
//  CAMBIO (Bloque 1): se eliminaron el enum Pantalla y PANTALLA_INICIAL.
//  Ya no cambiamos la pantalla a mano: ahora la app NAVEGA.
// =====================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // dibuja de borde a borde; el Scaffold respeta las barras del sistema
        setContent {
            MiRegistroTheme {
                AppNavegacion()
            }
        }
    }
}
