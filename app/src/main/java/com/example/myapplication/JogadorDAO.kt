package com.example.myapplication

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface JogadorDAO {
    @Insert
    suspend fun inserir(jogador: Class_Jogador): Long

    @Update
    suspend fun atualizar(jogador: Class_Jogador)

    @Delete
    suspend fun deletar(jogador: Class_Jogador)

    @Query("SELECT * FROM jogadores ORDER BY recorde DESC, nome, id")
    suspend fun buscarJogadores(): List<Class_Jogador>

    @Query("SELECT * FROM jogadores WHERE id = :id")
    suspend fun buscarPorId(id: Int): Class_Jogador?
}
