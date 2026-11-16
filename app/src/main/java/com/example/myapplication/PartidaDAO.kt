package com.example.myapplication

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query

@Dao
interface PartidaDAO {
    @Insert
    suspend fun inserir(partida: Class_Partida)

    @Delete
    suspend fun deletar(partida: Class_Partida)

    @Query("SELECT * FROM partidas ORDER BY data DESC, id DESC")
    suspend fun buscarPartidas(): List<Class_Partida>

    @Query("SELECT * FROM partidas WHERE sessaoId = :sessaoId")
    suspend fun buscarPorSessao(sessaoId: String): Class_Partida?

    @Query("DELETE FROM partidas WHERE jogadorId = :jogadorId")
    suspend fun deletarDoJogador(jogadorId: Int)
}
