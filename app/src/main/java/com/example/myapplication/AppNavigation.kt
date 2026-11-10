package com.example.myapplication

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.GeniusBackground

/**
 * Navegação de nível 1: decide em qual "modo" o usuário está.
 * Home -> Modo Jogador (com Bottom Navigation)
 * Home -> Login Admin -> Painel Admin
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rotas.HOME) {

        composable(Rotas.HOME) {
            Home(
                onPlayerMode = { navController.navigate(Rotas.PLAYER_ROOT) },
                onAdminMode = { navController.navigate(Rotas.ADMIN_LOGIN) }
            )
        }

        composable(Rotas.PLAYER_ROOT) {
            PlayerRoot(onExit = { navController.popBackStack(Rotas.HOME, inclusive = false) })
        }

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

/**
 * Navegação de nível 2: o "container" do Modo Jogador.
 * Tem seu próprio NavController para que a Bottom Navigation apareça só aqui.
 */
@Composable
fun PlayerRoot(onExit: () -> Unit) {
    val innerNavController = rememberNavController()

    Scaffold(
        containerColor = GeniusBackground,
        bottomBar = { GeniusBottomBar(innerNavController) }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = Rotas.JOGO,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Rotas.JOGO) { Jogo() }
            composable(Rotas.RANKING) { Ranking() }
            composable(Rotas.HISTORICO) { Historico() }
            composable(Rotas.CONFIGURACOES) { Configuracoes(onExit = onExit) }
        }
    }
}
