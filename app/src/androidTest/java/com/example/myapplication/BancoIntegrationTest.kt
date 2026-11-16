package com.example.myapplication

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.room3.Room
import androidx.room3.useWriterConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.AppDatabase
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BancoIntegrationTest {
    private lateinit var context: Context
    private lateinit var banco: AppDatabase
    private lateinit var repository: BancoRepository
    private lateinit var nomeBanco: String
    private val viewModels = ViewModelStore()

    @Before
    fun preparar() {
        context = ApplicationProvider.getApplicationContext()
        nomeBanco = "teste-genius-${UUID.randomUUID()}"
        abrirBanco()
    }

    private fun abrirBanco() {
        banco = Room.databaseBuilder<AppDatabase>(context, nomeBanco)
            .setDriver(AndroidSQLiteDriver())
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
        repository = BancoRepository(banco)
    }

    @After
    fun limpar() = runBlocking {
        withContext(Dispatchers.Main) { viewModels.clear() }
        banco.close()
        context.deleteDatabase(nomeBanco)
        Unit
    }

    @Test
    fun crudRecordesDuplicacaoEReabertura() = runBlocking {
        val jogadorId = repository.adicionarJogador("  Ana  ").toInt()
        repository.adicionarJogador("Bruno")
        assertEquals("Ana", buscarJogadores(banco.jogadorDAO()).first().nome)

        coroutineScope {
            val primeira = async { repository.salvarPartida(jogadorId, 8, "Fácil", "sessao-1", 100) }
            val repetida = async { repository.salvarPartida(jogadorId, 8, "Fácil", "sessao-1", 100) }
            primeira.await()
            repetida.await()
        }
        repository.salvarPartida(jogadorId, 3, "Médio", "sessao-2", 200)
        var dados = repository.dados.first()
        assertEquals(2, dados.partidas.size)
        assertEquals(listOf(200L, 100L), dados.partidas.map { it.data })
        assertEquals(8, dados.jogadores.first().recorde)
        assertEquals(jogadorId, dados.jogadores.first().id)

        val jogador = dados.jogadores.first()
        atualizarJogador(jogador.copy(nome = "Ana Maria"), banco.jogadorDAO())
        repository.excluirPartida(dados.partidas.last())
        assertEquals(8, banco.jogadorDAO().buscarPorId(jogadorId)?.recorde)

        banco.close()
        abrirBanco()
        dados = repository.dados.first()
        assertEquals("Ana Maria", dados.jogadores.first().nome)
        assertEquals(1, dados.partidas.size)
        assertEquals(jogadorId, dados.partidas.single().jogadorId)
        assertEquals(8, dados.jogadores.first().recorde)

        repository.excluirJogador(dados.jogadores.first())
        dados = repository.dados.first()
        assertTrue(dados.partidas.isEmpty())
        assertEquals("Bruno", dados.jogadores.single().nome)
    }

    @Test
    fun falhaAoAtualizarRecordeDesfazInsercaoDaPartida() = runBlocking {
        val jogadorId = repository.adicionarJogador("Ana").toInt()
        banco.useWriterConnection { conexao ->
            conexao.usePrepared(
                "CREATE TRIGGER impedir_recorde BEFORE UPDATE ON jogadores " +
                    "BEGIN SELECT RAISE(ABORT, 'falha de teste'); END"
            ) { it.step() }
        }
        try {
            repository.salvarPartida(jogadorId, 9, "Fácil", "falha", 100)
            fail("A transação deveria falhar")
        } catch (esperado: android.database.SQLException) {
            assertNotNull(esperado.message)
        }
        assertTrue(buscarPartidas(banco.partidaDAO()).isEmpty())
        assertEquals(0, banco.jogadorDAO().buscarPorId(jogadorId)?.recorde)
    }

    @Test
    fun fluxoReageAInsercaoEExclusao() = runBlocking {
        val jogadorId = repository.adicionarJogador("Ana").toInt()
        coroutineScope {
            val atualizacao = async {
                withTimeout(5000) { repository.dados.first { it.partidas.size == 1 } }
            }
            repository.salvarPartida(jogadorId, 4, "Difícil", "reativa", 100)
            assertEquals(4, atualizacao.await().jogadores.single().recorde)
        }
        val jogador = banco.jogadorDAO().buscarPorId(jogadorId)!!
        coroutineScope {
            val exclusao = async {
                withTimeout(5000) { repository.dados.first { it.jogadores.isEmpty() } }
            }
            repository.excluirJogador(jogador)
            assertTrue(exclusao.await().partidas.isEmpty())
        }
    }

    @Test
    fun migrationPreservaDadosERecuperaRecorde() = runBlocking {
        banco.close()
        context.deleteDatabase(nomeBanco)
        context.openOrCreateDatabase(nomeBanco, Context.MODE_PRIVATE, null).use { antigo ->
            antigo.execSQL("CREATE TABLE jogadores (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nome TEXT NOT NULL)")
            antigo.execSQL(
                "CREATE TABLE partidas (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "jogadorId INTEGER NOT NULL, pontuacao INTEGER NOT NULL, " +
                    "dificuldade TEXT NOT NULL, data INTEGER NOT NULL)"
            )
            antigo.execSQL("INSERT INTO jogadores VALUES (7, 'Existente')")
            antigo.execSQL("INSERT INTO partidas VALUES (12, 7, 15, 'Fácil', 123456)")
            antigo.version = 1
        }
        abrirBanco()
        val dados = repository.dados.first()
        assertEquals(7, dados.jogadores.single().id)
        assertEquals("Existente", dados.jogadores.single().nome)
        assertEquals(15, dados.jogadores.single().recorde)
        assertEquals(12, dados.partidas.single().id)
        assertEquals(123456L, dados.partidas.single().data)
        assertNull(dados.partidas.single().sessaoId)
        repository.salvarPartida(7, 20, "Difícil", "depois-migration", 200000)
        assertEquals(20, banco.jogadorDAO().buscarPorId(7)?.recorde)
        assertEquals(2, buscarPartidas(banco.partidaDAO()).size)
    }

    @Test
    fun jogoExigeJogadorSalvaErroETimeoutUmaVez() = runBlocking {
        val estado = SavedStateHandle()
        val model = withContext(Dispatchers.Main) {
            BancoViewModel(repository, estado).also { viewModels.put("banco", it) }
        }
        withContext(Dispatchers.Main) { model.iniciarJogo("Difícil") }
        assertFalse(model.jogo.value.emAndamento)
        assertTrue(buscarPartidas(banco.partidaDAO()).isEmpty())

        withContext(Dispatchers.Main) {
            model.adicionarJogador("Ana")
            model.adicionarJogador("Ana")
        }
        val dados = withTimeout(5000) { model.dados.first { it.jogadores.isNotEmpty() } }
        assertEquals(1, dados.jogadores.size)
        val jogador = dados.jogadores.single()
        withContext(Dispatchers.Main) {
            model.selecionarJogador(jogador)
            model.iniciarJogo("Difícil")
        }
        assertEquals(jogador.id, estado.get<Int>("jogadorAtivoId"))
        val primeiraRodada = withTimeout(5000) { model.jogo.first { it.aguardandoJogador } }
        withContext(Dispatchers.Main) { model.tocarCor(primeiraRodada.sequencia.first()) }
        assertEquals(1, model.jogo.value.score)
        withTimeout(5000) { model.jogo.first { it.aguardandoJogador } }
        withContext(Dispatchers.Main) {
            val errada = (model.jogo.value.sequencia.first() + 1) % 4
            model.tocarCor(errada)
            model.tocarCor(errada)
        }
        val salvo = withTimeout(5000) { model.dados.first { it.partidas.size == 1 } }
        assertEquals(1, salvo.jogadores.single().recorde)
        assertEquals(1, salvo.partidas.single().pontuacao)
        assertEquals(jogador.id, salvo.partidas.single().jogadorId)
        withTimeout(5000) { model.jogo.first { !it.salvando } }
        withContext(Dispatchers.Main) {
            model.salvarResultado()
            model.salvarResultado()
        }
        withTimeout(5000) { model.jogo.first { !it.salvando } }
        assertEquals(1, buscarPartidas(banco.partidaDAO()).size)

        withContext(Dispatchers.Main) { model.iniciarJogo("Difícil") }
        withTimeout(5000) { model.jogo.first { it.aguardandoJogador } }
        val timeout = withTimeout(7000) { model.dados.first { it.partidas.size == 2 } }
        assertEquals("Tempo esgotado!", model.jogo.value.status)
        assertEquals(1, timeout.jogadores.single().recorde)
        assertEquals(0, timeout.partidas.first().pontuacao)
    }
}
