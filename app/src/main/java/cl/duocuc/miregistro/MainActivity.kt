package cl.duocuc.miregistro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import cl.duocuc.miregistro.navigation.AppNavegacion
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme

// =====================================================================
//  MainActivity.kt  —  PUNTO DE ENTRADA DE LA APP
//  ---------------------------------------------------------------------
//      setContent {
//          Tema {              <- colores / tipografía / formas
//              Surface {       <- lienzo base con color de fondo automático
//                  AppNavegacion() <- el NavHost decide qué pantalla mostrar
//              }
//          }
//      }
// =====================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Dibuja de borde a borde
        setContent {
            MiRegistroTheme {
                // Surface asegura que el fondo base de la app respete el modo claro/oscuro
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavegacion()
                }
            }
        }
    }
}