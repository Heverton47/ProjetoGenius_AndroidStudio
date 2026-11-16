package com.example.myapplication

import androidx.room3.withReadTransaction
import androidx.room3.withWriteTransaction
import com.example.myapplication.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class DadosBanco(
    val jogadores: List<Class_Jogador> = emptyList(),
    val partidas: List<Class_Partida> = emptyList()
)

class BancoRepository(private val banco: AppDatabase) {
    private val jogadorDAO = banco.jogadorDAO()
    private val partidaDAO = banco.partidaDAO()

    val dados = banco.invalidationTracker.createFlow("jogadores", "partidas").map {
        banco.withReadTransaction {
            DadosBanco(buscarJogadores(jogadorDAO), buscarPartidas(partidaDAO))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun adicionarJogador(nome: String) = withContext(Dispatchers.IO) {
        inserirJogador(nome, jogadorDAO)
    }

    suspend fun salvarPartida(
        jogadorId: Int,
        pontuacao: Int,
        dificuldade: String,
        sessaoId: String,
        data: Long
    ) = withContext(Dispatchers.IO) {
        banco.withWriteTransaction {
            if (partidaDAO.buscarPorSessao(sessaoId) == null) {
                val jogador = checkNotNull(jogadorDAO.buscarPorId(jogadorId)) {
                    "O jogador desta partida foi excluído."
                }
                inserirPartida(jogadorId, pontuacao, dificuldade, partidaDAO, sessaoId, data)
                if (pontuacao > jogador.recorde) {
                    atualizarJogador(jogador.copy(recorde = pontuacao), jogadorDAO)
                }
            }
        }
    }

    suspend fun excluirJogador(jogador: Class_Jogador) = withContext(Dispatchers.IO) {
        banco.withWriteTransaction {
            partidaDAO.deletarDoJogador(jogador.id)
            deletarJogador(jogador, jogadorDAO)
        }
    }

    suspend fun excluirPartida(partida: Class_Partida) = withContext(Dispatchers.IO) {
        deletarPartida(partida, partidaDAO)
    }
}
