package com.example.myapplication

import android.util.Log
import kotlinx.coroutines.CancellationException

private suspend fun <T> executarOperacao(nome: String, operacao: suspend () -> T): T {
    return try {
        operacao()
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Log.e("OperacoesBanco", "Erro ao $nome: ${e.message}", e)
        throw e
    }
}

suspend fun inserirJogador(nome: String, jogadorDAO: JogadorDAO): Long =
    executarOperacao("inserir jogador") {
        require(nome.isNotBlank()) { "Digite o nome do jogador." }
        jogadorDAO.inserir(Class_Jogador(0, nome.trim()))
    }

suspend fun atualizarJogador(jogador: Class_Jogador, jogadorDAO: JogadorDAO) =
    executarOperacao("atualizar jogador") { jogadorDAO.atualizar(jogador) }

suspend fun deletarJogador(jogador: Class_Jogador, jogadorDAO: JogadorDAO) =
    executarOperacao("deletar jogador") { jogadorDAO.deletar(jogador) }

suspend fun buscarJogadores(jogadorDAO: JogadorDAO): List<Class_Jogador> =
    executarOperacao("buscar jogadores") { jogadorDAO.buscarJogadores() }

suspend fun inserirPartida(
    jogadorId: Int,
    pontuacao: Int,
    dificuldade: String,
    partidaDAO: PartidaDAO,
    sessaoId: String? = null,
    data: Long = System.currentTimeMillis()
) = executarOperacao("inserir partida") {
    partidaDAO.inserir(Class_Partida(0, jogadorId, pontuacao, dificuldade, data, sessaoId))
}

suspend fun deletarPartida(partida: Class_Partida, partidaDAO: PartidaDAO) =
    executarOperacao("deletar partida") { partidaDAO.deletar(partida) }

suspend fun buscarPartidas(partidaDAO: PartidaDAO): List<Class_Partida> =
    executarOperacao("buscar partidas") { partidaDAO.buscarPartidas() }
