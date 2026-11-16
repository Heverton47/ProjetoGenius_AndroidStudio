package com.example.myapplication.viewmodel

/** Partida já "montada" para exibição: o ViewModel troca o jogadorId pelo nome do jogador. */
data class ItemHistorico(
    val id: Int,
    val nomeJogador: String,
    val pontuacao: Int,
    val dificuldade: String,
    val data: Long
)
