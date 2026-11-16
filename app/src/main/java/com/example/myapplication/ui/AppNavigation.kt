package com.example.myapplication.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.GeniusBackground
import com.example.myapplication.viewmodel.AdminViewModel
import com.example.myapplication.viewmodel.GameViewModel
import com.example.myapplication.viewmodel.ViewModelFactory

/**
 * Navegação centralizada com 1 NavController.
 * A barra inferior (Bottom Navigation) é exibida condicionalmente apenas nas telas do jogador.
 *
 * Existem dois ViewModels, cada um com o seu UiState:
 *  - GameViewModel  -> Modo Jogador (jogo, ranking, histórico, configurações)
 *  - AdminViewModel -> Modo Administrador (login, desafios, jogadores, partidas)
 */
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val factory = remember { ViewModelFactory.criar(context) }
    val gameViewModel: GameViewModel = viewModel(factory = factory)
    val adminViewModel: AdminViewModel = viewModel(factory = factory)

    val gameUi by gameViewModel.ui.collectAsStateWithLifecycle()
    val adminUi by adminViewModel.ui.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val snackbar = remember { SnackbarHostState() }

    // Mensagens de erro/aviso vindas dos UiStates são mostradas em um Snackbar.
    LaunchedEffect(gameUi.mensagem) {
        gameUi.mensagem?.let {
            snackbar.showSnackbar(it)
            gameViewModel.limparMensagem()
        }
    }
    LaunchedEffect(adminUi.mensagem) {
        adminUi.mensagem?.let {
            snackbar.showSnackbar(it)
            adminViewModel.limparMensagem()
        }
    }

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
            composable(Rotas.JOGO) {
                Jogo(
                    ui = gameUi,
                    onIniciar = gameViewModel::iniciarJogo,
                    onTocarCor = gameViewModel::tocarCor,
                    onSalvarNovamente = gameViewModel::salvarResultado
                )
            }
            composable(Rotas.RANKING) {
                Ranking(
                    ui = gameUi,
                    onSelecionar = gameViewModel::selecionarJogador
                )
            }
            composable(Rotas.HISTORICO) {
                Historico(
                    ui = gameUi,
                    onFiltrar = gameViewModel::filtrarHistorico
                )
            }
            composable(Rotas.CONFIGURACOES) {
                Configuracoes(
                    ui = gameUi,
                    onAdicionarJogador = gameViewModel::adicionarJogador,
                    onDificuldade = gameViewModel::definirDificuldade,
                    onDesafio = gameViewModel::selecionarDesafio,
                    onExit = { navController.popBackStack(Rotas.HOME, inclusive = false) }
                )
            }

            // Telas do Modo Administrador
            composable(Rotas.ADMIN_LOGIN) {
                AdminLogin(
                    ui = adminUi,
                    onEntrar = adminViewModel::entrar,
                    onLimparErro = adminViewModel::limparErroLogin,
                    onLoginSuccess = {
                        // popUpTo remove o login da pilha: "voltar" no painel não reabre o login.
                        navController.navigate(Rotas.ADMIN) {
                            popUpTo(Rotas.ADMIN_LOGIN) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Rotas.ADMIN) {
                // Controle de perfil: o painel só aparece se o AdminViewModel disser que há admin logado.
                if (adminUi.logado) {
                    Admin(
                        ui = adminUi,
                        onSalvarDesafio = adminViewModel::salvarDesafio,
                        onExcluirDesafio = adminViewModel::excluirDesafio,
                        onLimparErroDesafio = adminViewModel::limparErroDesafio,
                        onEditarJogador = adminViewModel::editarJogador,
                        onExcluirJogador = adminViewModel::excluirJogador,
                        onExcluirPartida = adminViewModel::excluirPartida,
                        onLogout = {
                            navController.popBackStack(Rotas.HOME, inclusive = false)
                            adminViewModel.sair()
                        }
                    )
                } else {
                    // Sem login (ex.: estado perdido): volta para a tela de login.
                    LaunchedEffect(Unit) {
                        if (navController.currentDestination?.route == Rotas.ADMIN) {
                            navController.navigate(Rotas.ADMIN_LOGIN) {
                                popUpTo(Rotas.ADMIN) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}
