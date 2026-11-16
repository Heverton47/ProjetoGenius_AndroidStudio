package com.example.myapplication.data.repository

import android.util.Log
import androidx.room3.withWriteTransaction
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.Class_Jogador
import com.example.myapplication.data.local.Class_Partida
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen

data class DadosBanco(
    val jogadores: List<Class_Jogador> = emptyList(),
    val partidas: List<Class_Partida> = emptyList()
)

/** Repository do Room: único ponto de acesso ao banco local para os ViewModels. */
class BancoRepository(private val banco: AppDatabase) {
    private val jogadorDAO = banco.jogadorDAO()
    private val partidaDAO = banco.partidaDAO()

    /**
     * Junta os dois Flow<List<...>> dos DAOs. Se o banco falhar, o erro é tratado aqui:
     * emitimos um [Resultado.Erro] para o ViewModel mostrar na tela e tentamos de novo.
     */
    val dados: Flow<Resultado<DadosBanco>> =
        combine<List<Class_Jogador>, List<Class_Partida>, Resultado<DadosBanco>>(
            jogadorDAO.observarJogadores(),
            partidaDAO.observarPartidas()
        ) { jogadores, partidas ->
            Resultado.Sucesso(DadosBanco(jogadores, partidas))
        }.retryWhen { erro, _ ->
            if (erro is CancellationException) throw erro
            Log.e("BancoRepository", "Erro ao observar o banco: ${erro.message}", erro)
            emit(Resultado.Erro("Não foi possível carregar o banco. Tentando novamente."))
            delay(2000)
            true
        }.flowOn(Dispatchers.IO)

    // ---------- Jogador (CRUD completo) ----------

    /** Create */
    suspend fun adicionarJogador(nome: String): Resultado<Long> =
        executarSeguro("adicionar o jogador") {
            if (nome.isBlank()) throw RegraDeNegocioException("Digite o nome do jogador.")
            jogadorDAO.inserir(Class_Jogador(0, nome.trim()))
        }

    /** Update (o recorde é preservado; só o nome muda) */
    suspend fun atualizarNomeJogador(jogador: Class_Jogador, novoNome: String): Resultado<Unit> =
        executarSeguro("atualizar o jogador") {
            if (novoNome.isBlank()) throw RegraDeNegocioException("Digite o nome do jogador.")
            jogadorDAO.atualizar(jogador.copy(nome = novoNome.trim()))
        }

    /** Delete (apaga também as partidas do jogador, na mesma transação) */
    suspend fun excluirJogador(jogador: Class_Jogador): Resultado<Unit> =
        executarSeguro("excluir o jogador") {
            banco.withWriteTransaction {
                partidaDAO.deletarDoJogador(jogador.id)
                jogadorDAO.deletar(jogador)
            }
        }

    // ---------- Partida ----------

    /** Salva a partida e atualiza o recorde do jogador na mesma transação (tudo ou nada). */
    suspend fun salvarPartida(
        jogadorId: Int,
        pontuacao: Int,
        dificuldade: String,
        sessaoId: String,
        data: Long
    ): Resultado<Unit> = executarSeguro("salvar a partida") {
        banco.withWriteTransaction {
            // sessaoId evita salvar duas vezes a mesma partida (ex.: toque duplo em "tentar novamente")
            if (partidaDAO.buscarPorSessao(sessaoId) == null) {
                val jogador = jogadorDAO.buscarPorId(jogadorId)
                    ?: throw RegraDeNegocioException("O jogador desta partida foi excluído.")
                partidaDAO.inserir(
                    Class_Partida(0, jogadorId, pontuacao, dificuldade, data, sessaoId)
                )
                if (pontuacao > jogador.recorde) {
                    jogadorDAO.atualizar(jogador.copy(recorde = pontuacao))
                }
            }
        }
    }

    suspend fun excluirPartida(partidaId: Int): Resultado<Unit> =
        executarSeguro("excluir a partida") { partidaDAO.deletarPorId(partidaId) }
}
