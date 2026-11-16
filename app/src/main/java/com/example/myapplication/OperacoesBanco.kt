package com.example.myapplication

import android.util.Log
import kotlinx.coroutines.CancellationException

suspend fun inserirJogador(nome: String, jogadorDAO: JogadorDAO) {
    try {
        jogadorDAO.inserir(Class_Jogador(0, nome))
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Log.e("Erro ao inserir jogador", "Msg ${e.message}")
    }
}

suspend fun buscarJogadores(jogadorDAO: JogadorDAO): List<Class_Jogador> {
    return try {
        jogadorDAO.buscarJogadores()
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Log.e("Erro ao buscar jogadores", "Msg ${e.message}")
        emptyList()
    }
}

suspend fun inserirPartida(
    jogadorId: Int,
    pontuacao: Int,
    dificuldade: String,
    partidaDAO: PartidaDAO
) {
    try {
        partidaDAO.inserir(
            Class_Partida(0, jogadorId, pontuacao, dificuldade, System.currentTimeMillis())
        )
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Log.e("Erro ao inserir partida", "Msg ${e.message}")
    }
}

suspend fun buscarPartidas(partidaDAO: PartidaDAO): List<Class_Partida> {
    return try {
        partidaDAO.buscarPartidas()
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Log.e("Erro ao buscar partidas", "Msg ${e.message}")
        emptyList()
    }
}
