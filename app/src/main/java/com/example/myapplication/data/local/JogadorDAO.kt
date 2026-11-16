package com.example.myapplication.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JogadorDAO {
    @Insert
    suspend fun inserir(jogador: Class_Jogador): Long

    @Update
    suspend fun atualizar(jogador: Class_Jogador)

    @Delete
    suspend fun deletar(jogador: Class_Jogador)

    // Read via Flow: sempre que a tabela muda, uma nova lista é emitida automaticamente.
    @Query("SELECT * FROM jogadores ORDER BY recorde DESC, nome, id")
    fun observarJogadores(): Flow<List<Class_Jogador>>

    @Query("SELECT * FROM jogadores WHERE id = :id")
    suspend fun buscarPorId(id: Int): Class_Jogador?
}
