package com.example.myapplication

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "jogadores")
data class Class_Jogador(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val nome: String
)
