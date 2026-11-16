package com.example.myapplication

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "partidas")
data class Class_Partida(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val jogadorId: Int,
    val pontuacao: Int,
    val dificuldade: String,
    val data: Long
)
