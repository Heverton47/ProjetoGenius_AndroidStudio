package com.example.myapplication.data.remote

/**
 * Desafio cadastrado pelo administrador no Firebase Firestore.
 * Coleção "desafios": cada documento tem os campos `nome` e `sequencia`
 * (ex.: "Vermelho, Azul, Verde"). O `id` é o id do documento no Firestore.
 */
data class Desafio(
    val id: String = "",
    val nome: String = "",
    val sequencia: String = ""
)
