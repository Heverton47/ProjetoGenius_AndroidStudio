package com.example.myapplication

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query

@Dao
interface PartidaDAO {

    @Insert
    suspend fun inserir(partida: Class_Partida)

    @Query("SELECT * FROM partidas")
    suspend fun buscarPartidas(): List<Class_Partida>
}
