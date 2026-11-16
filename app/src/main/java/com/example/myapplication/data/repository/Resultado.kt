package com.example.myapplication.data.repository

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext

/**
 * Resultado de uma operação do Repository.
 * Em vez de deixar a exceção "vazar" até a tela, o Repository captura a falha (try/catch)
 * e devolve um [Resultado.Erro] com uma mensagem já tratada, que o ViewModel coloca no UiState.
 */
sealed interface Resultado<out T> {
    data class Sucesso<out T>(val valor: T) : Resultado<T>
    data class Erro(val mensagem: String) : Resultado<Nothing>
}

/** Erro previsto pelas regras do app (ex.: nome em branco). A mensagem já é própria para o usuário. */
class RegraDeNegocioException(mensagem: String) : Exception(mensagem)

private const val TAG = "Repository"

/**
 * Executa [bloco] fora da thread principal e isola qualquer falha (Room, Firestore, rede).
 * [acao] descreve o que estava sendo feito, no infinitivo ("salvar a partida").
 */
internal suspend fun <T> executarSeguro(acao: String, bloco: suspend () -> T): Resultado<T> =
    withContext(Dispatchers.IO) {
        try {
            Resultado.Sucesso(bloco())
        } catch (e: TimeoutCancellationException) {
            // Vem antes de CancellationException porque é um tipo filho dela.
            Log.e(TAG, "Tempo esgotado ao $acao", e)
            Resultado.Erro("Tempo esgotado ao $acao. Verifique a conexão e tente novamente.")
        } catch (e: CancellationException) {
            throw e // cancelamento de coroutine nunca pode ser engolido
        } catch (e: RegraDeNegocioException) {
            Resultado.Erro(e.message ?: "Operação inválida.")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao $acao: ${e.message}", e)
            Resultado.Erro("Não foi possível $acao.")
        }
    }
