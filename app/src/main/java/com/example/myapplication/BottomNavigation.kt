package com.example.myapplication

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.ui.theme.GeniusBlue
import com.example.myapplication.ui.theme.GeniusSurface

/** Descreve uma aba da barra inferior: para onde vai, o texto e o ícone. */
data class BottomNavItem(
    val rota: String,
    val label: String,
    val icon: ImageVector
)

/** As 4 abas do Modo Jogador. */
val bottomNavItems = listOf(
    BottomNavItem(Rotas.JOGO, "Jogar", Icons.Default.PlayArrow),
    BottomNavItem(Rotas.RANKING, "Ranking", Icons.Default.Star),
    BottomNavItem(Rotas.HISTORICO, "Histórico", Icons.AutoMirrored.Filled.List),
    BottomNavItem(Rotas.CONFIGURACOES, "Config.", Icons.Default.Settings)
)

@Composable
fun GeniusBottomBar(navController: NavHostController) {
    // Observa a tela atual para destacar a aba correta.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(containerColor = GeniusSurface) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.rota,
                onClick = {
                    navController.navigate(item.rota) {
                        // Evita empilhar a mesma aba várias vezes e guarda o estado de cada uma.
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = GeniusBlue,
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray
                )
            )
        }
    }
}
