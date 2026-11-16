package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.remote.Desafio
import com.example.myapplication.data.remote.DesafioRemoteDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen

/** Repository do Firestore: isola falhas de rede/nuvem e devolve erros já tratados. */
class DesafioRepository(private val fonte: DesafioRemoteDataSource) {

    /** Lista de desafios em tempo real. Em caso de falha, emite um Erro e tenta de novo. */
    val desafios: Flow<Resultado<List<Desafio>>> = fonte.observar()
        .map<List<Desafio>, Resultado<List<Desafio>>> { Resultado.Sucesso(it) }
        .retryWhen { erro, _ ->
            if (erro is CancellationException) throw erro
            Log.e("DesafioRepository", "Erro ao observar desafios: ${erro.message}", erro)
            emit(Resultado.Erro("Não foi possível carregar os desafios. Tentando novamente."))
            delay(5000)
            true
        }
        .flowOn(Dispatchers.IO)

    suspend fun adicionar(nome: String, sequencia: String): Resultado<Unit> =
        executarSeguro("adicionar o desafio") { fonte.adicionar(nome, sequencia) }

    suspend fun atualizar(id: String, nome: String, sequencia: String): Resultado<Unit> =
        executarSeguro("atualizar o desafio") { fonte.atualizar(id, nome, sequencia) }

    suspend fun excluir(id: String): Resultado<Unit> =
        executarSeguro("excluir o desafio") { fonte.excluir(id) }
}
