package cl.duoc.tiotecpreventivo.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.tiotecpreventivo.ui.screens.AccionesScreen
import cl.duoc.tiotecpreventivo.ui.screens.AprendeScreen
import cl.duoc.tiotecpreventivo.ui.screens.DispositivosScreen
import cl.duoc.tiotecpreventivo.ui.screens.InicioScreen
import cl.duoc.tiotecpreventivo.ui.screens.PerfilScreen
import androidx.navigation.navigation

private const val RUTA_DISPOSITIVOS = "dispositivos"
private const val GRAFO_DISPOSITIVOS = "gestion_dispositivos"

private enum class DestinoPrincipal(
    val ruta: String, val titulo: String, val simbolo: String
) {
    INICIO("inicio", "Inicio", "⌂"), ACCIONES("acciones", "Mis acciones", "✓"), APRENDE(
        "aprende", "Aprende", "?"
    ),
    PERFIL("perfil", "Perfil", "○")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TioTecApp() {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val enDispositivos = rutaActual == RUTA_DISPOSITIVOS

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = {
                Text(
                    if (enDispositivos) {
                        "Mis dispositivos"
                    } else {
                        "TíoTec Preventivo"
                    }
                )
            }, navigationIcon = {
                if (enDispositivos) {
                    TextButton(
                        onClick = {
                            navController.popBackStack()
                        }) {
                        Text("← Volver")
                    }
                }
            })
        },
        bottomBar = {
            NavigationBar {
                DestinoPrincipal.entries.forEach { destino ->
                    NavigationBarItem(selected = rutaActual == destino.ruta, onClick = {
                        navController.navigate(destino.ruta) {
                            popUpTo(
                                navController.graph.findStartDestination().id
                            ) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }, icon = {
                        Text(
                            text = destino.simbolo, style = MaterialTheme.typography.titleLarge
                        )
                    }, label = {
                        Text(destino.titulo)
                    })
                }
            }
        }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DestinoPrincipal.INICIO.ruta,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable(DestinoPrincipal.INICIO.ruta) {
                InicioScreen(
                    onVerDispositivos = {
                        navController.navigate(GRAFO_DISPOSITIVOS) {
                            launchSingleTop = true
                        }
                    })
            }

            composable(DestinoPrincipal.ACCIONES.ruta) {
                AccionesScreen()
            }

            composable(DestinoPrincipal.APRENDE.ruta) {
                AprendeScreen()
            }

            composable(DestinoPrincipal.PERFIL.ruta) {
                PerfilScreen()
            }

            navigation(
                startDestination = RUTA_DISPOSITIVOS,
                route = GRAFO_DISPOSITIVOS
            ) {
                composable(RUTA_DISPOSITIVOS) {
                    DispositivosScreen()
                }
            }
        }
    }
}