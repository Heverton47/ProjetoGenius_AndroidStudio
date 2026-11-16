package com.example.myapplication.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "partidas", indices = [Index(value = ["sessaoId"], unique = true)])
data class Class_Partida(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val jogadorId: Int,
    val pontuacao: Int,
    val dificuldade: String,
    val data: Long,
    val sessaoId: String? = null
)
