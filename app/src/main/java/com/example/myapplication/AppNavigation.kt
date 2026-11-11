package com.example.myapplication

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.GeniusBackground

/**
 * Navegação centralizada com 1 NavController.
 * A barra inferior (Bottom Navigation) é exibida condicionalmente apenas nas telas do jogador.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Observa a rota atual para decidir se a barra de navegação deve aparecer
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Lista de rotas onde a barra inferior DEVE aparecer
    val playerRoutes = listOf(
        Rotas.JOGO,
        Rotas.RANKING,
        Rotas.HISTORICO,
        Rotas.CONFIGURACOES
    )

    Scaffold(
        containerColor = GeniusBackground,
        bottomBar = {
            if (currentRoute in playerRoutes) {
                GeniusBottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rotas.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Tela Inicial
            composable(Rotas.HOME) {
                Home(
                    onPlayerMode = { navController.navigate(Rotas.JOGO) },
                    onAdminMode = { navController.navigate(Rotas.ADMIN_LOGIN) }
                )
            }

            // Telas do Modo Jogador
            composable(Rotas.JOGO) { Jogo() }
            composable(Rotas.RANKING) { Ranking() }
            composable(Rotas.HISTORICO) { Historico() }
            composable(Rotas.CONFIGURACOES) {
                Configuracoes(
                    onExit = { navController.popBackStack(Rotas.HOME, inclusive = false) }
                )
            }

            // Telas do Modo Administrador
            composable(Rotas.ADMIN_LOGIN) {
                AdminLogin(
                    onLoginSuccess = { navController.navigate(Rotas.ADMIN) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Rotas.ADMIN) {
                Admin(onLogout = { navController.popBackStack(Rotas.HOME, inclusive = false) })
            }
        }
    }
}
