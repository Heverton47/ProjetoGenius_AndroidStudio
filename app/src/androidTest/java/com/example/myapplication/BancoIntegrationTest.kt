package com.example.myapplication

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.room3.Room
import androidx.room3.useWriterConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.remote.Desafio
import com.example.myapplication.data.remote.DesafioRemoteDataSource
import com.example.myapplication.data.repository.BancoRepository
import com.example.myapplication.data.repository.DadosBanco
import com.example.myapplication.data.repository.DesafioRepository
import com.example.myapplication.data.repository.Resultado
import com.example.myapplication.viewmodel.GameViewModel
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Fonte de desafios falsa (em memória), para testar sem depender da internet/Firestore. */
private class DesafiosFalsos(inicial: List<Desafio> = emptyList()) : DesafioRemoteDataSource {
    val lista = MutableStateFlow(inicial)
    override fun observar(): Flow<List<Desafio>> = lista
    override suspend fun adicionar(nome: String, sequencia: String) {
        lista.value = lista.value + Desafio(UUID.randomUUID().toString(), nome, sequencia)
    }
    override suspend fun atualizar(id: String, nome: String, sequencia: String) {
        lista.value = lista.value.map { if (it.id == id) Desafio(id, nome, sequencia) else it }
    }
    override suspend fun excluir(id: String) {
        lista.value = lista.value.filter { it.id != id }
    }
}

private fun <T> Resultado<T>.valor(): T = (this as Resultado.Sucesso).valor

private suspend fun BancoRepository.dadosAtuais(): DadosBanco = dados.first().valor()

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
        val jogadorId = repository.adicionarJogador("  Ana  ").valor().toInt()
        repository.adicionarJogador("Bruno")
        assertEquals("Ana", banco.jogadorDAO().observarJogadores().first().first().nome)

        coroutineScope {
            val primeira = async { repository.salvarPartida(jogadorId, 8, "Fácil", "sessao-1", 100) }
            val repetida = async { repository.salvarPartida(jogadorId, 8, "Fácil", "sessao-1", 100) }
            primeira.await().valor()
            repetida.await().valor()
        }
        repository.salvarPartida(jogadorId, 3, "Médio", "sessao-2", 200)
        var dados = repository.dadosAtuais()
        assertEquals(2, dados.partidas.size)
        assertEquals(listOf(200L, 100L), dados.partidas.map { it.data })
        assertEquals(8, dados.jogadores.first().recorde)
        assertEquals(jogadorId, dados.jogadores.first().id)

        val jogador = dados.jogadores.first()
        repository.atualizarNomeJogador(jogador, "Ana Maria") // Update
        repository.excluirPartida(dados.partidas.last().id)   // Delete
        assertEquals(8, banco.jogadorDAO().buscarPorId(jogadorId)?.recorde)

        banco.close()
        abrirBanco()
        dados = repository.dadosAtuais()
        assertEquals("Ana Maria", dados.jogadores.first().nome)
        assertEquals(1, dados.partidas.size)
        assertEquals(jogadorId, dados.partidas.single().jogadorId)
        assertEquals(8, dados.jogadores.first().recorde)

        repository.excluirJogador(dados.jogadores.first())
        dados = repository.dadosAtuais()
        assertTrue(dados.partidas.isEmpty())
        assertEquals("Bruno", dados.jogadores.single().nome)
    }

    @Test
    fun nomeEmBrancoRetornaErroTratado() = runBlocking {
        val resultado = repository.adicionarJogador("   ")
        assertTrue(resultado is Resultado.Erro)
        assertEquals("Digite o nome do jogador.", (resultado as Resultado.Erro).mensagem)
        assertTrue(repository.dadosAtuais().jogadores.isEmpty())
    }

    @Test
    fun falhaAoAtualizarRecordeDesfazInsercaoDaPartida() = runBlocking {
        val jogadorId = repository.adicionarJogador("Ana").valor().toInt()
        banco.useWriterConnection { conexao ->
            conexao.usePrepared(
                "CREATE TRIGGER impedir_recorde BEFORE UPDATE ON jogadores " +
                    "BEGIN SELECT RAISE(ABORT, 'falha de teste'); END"
            ) { it.step() }
        }
        // O Repository não lança exceção: devolve um Erro já tratado.
        val resultado = repository.salvarPartida(jogadorId, 9, "Fácil", "falha", 100)
        assertTrue(resultado is Resultado.Erro)
        assertTrue(repository.dadosAtuais().partidas.isEmpty())
        assertEquals(0, banco.jogadorDAO().buscarPorId(jogadorId)?.recorde)
    }

    @Test
    fun fluxoReageAInsercaoEExclusao() = runBlocking {
        val jogadorId = repository.adicionarJogador("Ana").valor().toInt()
        coroutineScope {
            val atualizacao = async {
                withTimeout(5000) {
                    repository.dados.first { it is Resultado.Sucesso && it.valor.partidas.size == 1 }.valor()
                }
            }
            repository.salvarPartida(jogadorId, 4, "Difícil", "reativa", 100)
            assertEquals(4, atualizacao.await().jogadores.single().recorde)
        }
        val jogador = banco.jogadorDAO().buscarPorId(jogadorId)!!
        coroutineScope {
            val exclusao = async {
                withTimeout(5000) {
                    repository.dados.first { it is Resultado.Sucesso && it.valor.jogadores.isEmpty() }.valor()
                }
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
        val dados = repository.dadosAtuais()
        assertEquals(7, dados.jogadores.single().id)
        assertEquals("Existente", dados.jogadores.single().nome)
        assertEquals(15, dados.jogadores.single().recorde)
        assertEquals(12, dados.partidas.single().id)
        assertEquals(123456L, dados.partidas.single().data)
        assertNull(dados.partidas.single().sessaoId)
        repository.salvarPartida(7, 20, "Difícil", "depois-migration", 200000)
        assertEquals(20, banco.jogadorDAO().buscarPorId(7)?.recorde)
        assertEquals(2, repository.dadosAtuais().partidas.size)
    }

    @Test
    fun desafiosRemotosCrudViaRepository() = runBlocking {
        val repositorio = DesafioRepository(DesafiosFalsos())
        repositorio.adicionar("Aquecimento", "Vermelho, Azul")
        var lista = repositorio.desafios.first().valor()
        assertEquals(1, lista.size)
        val id = lista.single().id

        repositorio.atualizar(id, "Aquecimento 2", "Verde")
        lista = repositorio.desafios.first().valor()
        assertEquals("Aquecimento 2", lista.single().nome)

        repositorio.excluir(id)
        assertTrue(repositorio.desafios.first().valor().isEmpty())
    }

    @Test
    fun jogoExigeJogadorSalvaErroETimeoutUmaVez() = runBlocking {
        val estado = SavedStateHandle()
        val model = withContext(Dispatchers.Main) {
            GameViewModel(repository, DesafioRepository(DesafiosFalsos()), estado)
                .also { viewModels.put("game", it) }
        }
        withContext(Dispatchers.Main) {
            model.definirDificuldade("Difícil")
            model.iniciarJogo()
        }
        assertFalse(model.ui.value.emAndamento)
        assertTrue(repository.dadosAtuais().partidas.isEmpty())

        withContext(Dispatchers.Main) {
            model.adicionarJogador("Ana")
            model.adicionarJogador("Ana")
        }
        val comJogador = withTimeout(5000) { model.ui.first { it.ranking.isNotEmpty() } }
        assertEquals(1, comJogador.ranking.size)
        val jogador = comJogador.ranking.single()
        withContext(Dispatchers.Main) {
            model.selecionarJogador(jogador)
            model.iniciarJogo()
        }
        assertEquals(jogador.id, estado.get<Int>("jogadorAtivoId"))
        val primeiraRodada = withTimeout(5000) { model.ui.first { it.aguardandoJogador } }
        withContext(Dispatchers.Main) { model.tocarCor(primeiraRodada.sequencia.first()) }
        assertEquals(1, model.ui.value.score)
        withTimeout(5000) { model.ui.first { it.aguardandoJogador } }
        withContext(Dispatchers.Main) {
            val errada = (model.ui.value.sequencia.first() + 1) % 4
            model.tocarCor(errada)
            model.tocarCor(errada)
        }
        val salvo = withTimeout(5000) {
            repository.dados.first { it is Resultado.Sucesso && it.valor.partidas.size == 1 }.valor()
        }
        assertEquals(1, salvo.jogadores.single().recorde)
        assertEquals(1, salvo.partidas.single().pontuacao)
        assertEquals(jogador.id, salvo.partidas.single().jogadorId)
        withTimeout(5000) { model.ui.first { !it.salvando } }
        withContext(Dispatchers.Main) {
            model.salvarResultado()
            model.salvarResultado()
        }
        withTimeout(5000) { model.ui.first { !it.salvando } }
        assertEquals(1, repository.dadosAtuais().partidas.size)

        withContext(Dispatchers.Main) { model.iniciarJogo() }
        withTimeout(5000) { model.ui.first { it.aguardandoJogador } }
        val timeout = withTimeout(7000) {
            repository.dados.first { it is Resultado.Sucesso && it.valor.partidas.size == 2 }.valor()
        }
        assertEquals("Tempo esgotado!", model.ui.value.status)
        assertEquals(1, timeout.jogadores.single().recorde)
        assertEquals(0, timeout.partidas.first().pontuacao)
    }
}
