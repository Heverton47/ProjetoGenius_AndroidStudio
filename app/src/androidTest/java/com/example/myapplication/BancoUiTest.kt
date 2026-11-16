package com.example.myapplication

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BancoUiTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var context: Context
    private lateinit var banco: AppDatabase
    private lateinit var model: BancoViewModel
    private lateinit var nomeBanco: String
    private val viewModels = ViewModelStore()

    @Before
    fun preparar() {
        context = ApplicationProvider.getApplicationContext()
        nomeBanco = "teste-ui-${UUID.randomUUID()}"
        banco = Room.databaseBuilder<AppDatabase>(context, nomeBanco)
            .setDriver(AndroidSQLiteDriver()).build()
        compose.runOnUiThread {
            model = BancoViewModel(BancoRepository(banco), SavedStateHandle())
            viewModels.put("banco", model)
        }
        compose.setContent {
            MyApplicationTheme {
                Surface(Modifier.fillMaxSize()) {
                    AppNavigation(model)
                }
            }
        }
    }

    @After
    fun limpar() {
        compose.runOnUiThread { viewModels.clear() }
        banco.close()
        context.deleteDatabase(nomeBanco)
    }

    @Test
    fun cadastroSelecaoJogoHistoricoEExclusoesAdmin() {
        compose.onNodeWithText("Modo Jogador").performClick()
        compose.onNodeWithText("Iniciar").assertIsNotEnabled()
        compose.onNodeWithText("Config.").performClick()
        compose.onNodeWithText("Adicionar jogador").performClick()
        compose.runOnIdle { assertTrue(model.dados.value.jogadores.isEmpty()) }
        compose.onNodeWithText("Nome do jogador").performTextInput("Jogador UI")
        compose.onNodeWithText("Adicionar jogador").performClick()
        compose.waitUntil(5000) { model.dados.value.jogadores.size == 1 }
        compose.waitUntil(10000) { model.mensagem.value == null }
        compose.onNodeWithText("Difícil").performClick()
        compose.onNodeWithText("Ranking").performClick()
        compose.onAllNodesWithContentDescription("Excluir item do ranking").assertCountEquals(0)
        compose.onNodeWithText("1º  Jogador UI").performClick()
        compose.onNodeWithText("1º  Jogador UI • Selecionado").assertExists()
        compose.onNodeWithText("Jogar").performClick()
        compose.onNodeWithText("Jogador: Jogador UI • Recorde: 0").assertExists()
        compose.onNodeWithText("Iniciar").performClick()
        compose.runOnIdle { assertTrue("Jogo não iniciou: ${model.mensagem.value}", model.jogo.value.emAndamento) }
        compose.mainClock.advanceTimeBy(7000)
        try {
            compose.waitUntil(10000) { model.dados.value.partidas.size == 1 }
        } catch (erro: ComposeTimeoutException) {
            throw AssertionError("Estado: ${model.jogo.value}; mensagem: ${model.mensagem.value}", erro)
        }
        compose.onNodeWithText("Histórico").performClick()
        compose.onNodeWithText("Jogador UI").assertExists()
        compose.onNodeWithText("0 pontos").assertExists()
        compose.onAllNodesWithContentDescription("Excluir partida").assertCountEquals(0)
        compose.onNodeWithText("Config.").performClick()
        compose.onNodeWithText("Voltar ao início").performClick()
        compose.onNodeWithText("Modo Administrador").performClick()
        compose.onNodeWithText("Usuário").performTextInput("admin")
        compose.onNodeWithText("Senha").performTextInput("1234")
        compose.onNodeWithText("Entrar").performClick()
        compose.onNodeWithText("Mostrar Histórico").performClick()
        compose.onNodeWithContentDescription("Excluir partida").performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.runOnIdle { assertEquals(1, model.dados.value.partidas.size) }
        compose.onNodeWithContentDescription("Excluir partida").performClick()
        compose.onNodeWithText("Excluir", useUnmergedTree = true).performClick()
        compose.waitUntil(5000) { model.dados.value.partidas.isEmpty() }
        compose.onNodeWithText("Mostrar Ranking").performClick()
        compose.onNodeWithContentDescription("Excluir item do ranking").performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.runOnIdle { assertEquals(1, model.dados.value.jogadores.size) }
        compose.onNodeWithContentDescription("Excluir item do ranking").performClick()
        compose.onNodeWithText("Excluir", useUnmergedTree = true).performClick()
        compose.waitUntil(5000) { model.dados.value.jogadores.isEmpty() }
        compose.runOnIdle { assertNull(model.jogadorAtivoId.value) }
        runBlocking {
            assertTrue(buscarJogadores(banco.jogadorDAO()).isEmpty())
            assertTrue(buscarPartidas(banco.partidaDAO()).isEmpty())
        }
    }
}
