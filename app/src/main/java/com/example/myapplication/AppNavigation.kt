package com.example.myapplication

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun AppNavigation(
    bancoViewModel: BancoViewModel = viewModel(
        factory = BancoViewModel.factory(LocalContext.current.applicationContext)
    )
) {
    val navController = rememberNavController()
    val mensagem by bancoViewModel.mensagem.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(mensagem) {
        mensagem?.let {
            snackbar.showSnackbar(it)
            bancoViewModel.limparMensagem()
        }
    }
    var dificuldade by rememberSaveable { mutableStateOf("Fácil") }

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
        snackbarHost = { SnackbarHost(snackbar) },
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
            composable(Rotas.JOGO) { Jogo(bancoViewModel, dificuldade) }
            composable(Rotas.RANKING) { Ranking(bancoViewModel) }
            composable(Rotas.HISTORICO) { Historico(bancoViewModel) }
            composable(Rotas.CONFIGURACOES) {
                Configuracoes(
                    bancoViewModel = bancoViewModel,
                    dificuldade = dificuldade,
                    onDificuldadeChange = { dificuldade = it },
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
                Admin(bancoViewModel = bancoViewModel, onLogout = { navController.popBackStack(Rotas.HOME, inclusive = false) })
            }
        }
    }
}
