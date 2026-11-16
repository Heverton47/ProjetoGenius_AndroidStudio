package com.example.myapplication.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PartidaDAO {
    @Insert
    suspend fun inserir(partida: Class_Partida)

    @Delete
    suspend fun deletar(partida: Class_Partida)

    @Query("DELETE FROM partidas WHERE id = :id")
    suspend fun deletarPorId(id: Int)

    // Read via Flow: o histórico se atualiza sozinho quando uma partida é salva ou excluída.
    @Query("SELECT * FROM partidas ORDER BY data DESC, id DESC")
    fun observarPartidas(): Flow<List<Class_Partida>>

    @Query("SELECT * FROM partidas WHERE sessaoId = :sessaoId")
    suspend fun buscarPorSessao(sessaoId: String): Class_Partida?

    @Query("DELETE FROM partidas WHERE jogadorId = :jogadorId")
    suspend fun deletarDoJogador(jogadorId: Int)
}
