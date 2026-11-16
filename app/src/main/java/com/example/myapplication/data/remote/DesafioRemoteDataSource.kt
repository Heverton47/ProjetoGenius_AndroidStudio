package com.example.myapplication.data.remote

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

/**
 * Contrato da fonte remota de desafios.
 * Ser uma interface permite trocar o Firestore por uma versão falsa nos testes.
 */
interface DesafioRemoteDataSource {
    fun observar(): Flow<List<Desafio>>
    suspend fun adicionar(nome: String, sequencia: String)
    suspend fun atualizar(id: String, nome: String, sequencia: String)
    suspend fun excluir(id: String)
}

/** Implementação real, usando o Firebase Firestore. */
class FirestoreDesafioDataSource : DesafioRemoteDataSource {

    // lazy: só acessa o Firebase quando for realmente usado.
    private val colecao by lazy { FirebaseFirestore.getInstance().collection("desafios") }

    /** Flow em tempo real: emite a lista inteira sempre que algo muda no Firestore. */
    override fun observar(): Flow<List<Desafio>> = callbackFlow {
        val registro = colecao.addSnapshotListener { snapshot, erro ->
            if (erro != null) {
                close(erro) // o Repository trata o erro (try/catch / retry)
                return@addSnapshotListener
            }
            val lista = snapshot?.documents
                ?.map { doc ->
                    Desafio(
                        id = doc.id,
                        nome = doc.getString("nome").orEmpty(),
                        sequencia = doc.getString("sequencia").orEmpty()
                    )
                }
                ?.sortedBy { it.nome.lowercase() }
                .orEmpty()
            trySend(lista)
        }
        awaitClose { registro.remove() }
    }

    override suspend fun adicionar(nome: String, sequencia: String) {
        withTimeout(TEMPO_LIMITE_MS) {
            colecao.add(mapOf("nome" to nome, "sequencia" to sequencia)).aguardar()
        }
    }

    override suspend fun atualizar(id: String, nome: String, sequencia: String) {
        withTimeout(TEMPO_LIMITE_MS) {
            colecao.document(id).update(mapOf("nome" to nome, "sequencia" to sequencia)).aguardar()
        }
    }

    override suspend fun excluir(id: String) {
        withTimeout(TEMPO_LIMITE_MS) {
            colecao.document(id).delete().aguardar()
        }
    }

    private companion object {
        // Sem internet o Firestore não confirma a escrita; o limite evita travar a tela para sempre.
        const val TEMPO_LIMITE_MS = 10_000L
    }
}

/** Transforma o Task do Firebase em uma função suspend (sem precisar de biblioteca extra). */
private suspend fun <T> Task<T>.aguardar(): T = suspendCancellableCoroutine { continuacao ->
    addOnSuccessListener { resultado -> continuacao.resume(resultado) }
    addOnFailureListener { erro -> continuacao.resumeWithException(erro) }
}
