package com.example.myapplication

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query

@Dao
interface JogadorDAO {

    @Insert
    suspend fun inserir(jogador: Class_Jogador)

    @Query("SELECT * FROM jogadores")
    suspend fun buscarJogadores(): List<Class_Jogador>
}
