package com.example.myapplication

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.AppDatabase
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlin.random.Random

data class EstadoJogo(
    val jogadorId: Int? = null,
    val dificuldade: String = "Fácil",
    val sequencia: List<Int> = emptyList(),
    val indiceJogador: Int = 0,
    val litIndex: Int = -1,
    val status: String = "Toque em Iniciar",
    val emAndamento: Boolean = false,
    val aguardandoJogador: Boolean = false,
    val rodada: Int = 0,
    val score: Int = 0,
    val tempoRestante: Int = 0,
    val sessaoId: String = "",
    val data: Long = 0,
    val salvando: Boolean = false,
    val erroAoSalvar: Boolean = false
) {
    val tempoPorCor: Int
        get() = when (dificuldade) {
            "Difícil" -> 1
            "Médio" -> 3
            else -> 5
        }
}

class BancoViewModel(
    private val repository: BancoRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _dados = MutableStateFlow(DadosBanco())
    val dados = _dados.asStateFlow()
    val jogadorAtivoId = savedStateHandle.getStateFlow<Int?>("jogadorAtivoId", null)
    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem = _mensagem.asStateFlow()
    private val _ocupado = MutableStateFlow(false)
    val ocupado = _ocupado.asStateFlow()
    private val _jogo = MutableStateFlow(EstadoJogo())
    val jogo = _jogo.asStateFlow()
    private var animacao: Job? = null
    private var cronometro: Job? = null
    private var feedback: Job? = null

    init {
        viewModelScope.launch {
            repository.dados.retryWhen { erro, _ ->
                if (erro is CancellationException) throw erro
                _mensagem.value = "Não foi possível carregar o banco. Tentando novamente."
                delay(2000)
                true
            }.collect { novosDados ->
                _dados.value = novosDados
                if (novosDados.jogadores.none { it.id == jogadorAtivoId.value }) {
                    savedStateHandle["jogadorAtivoId"] = null
                }
                if (_jogo.value.emAndamento &&
                    novosDados.jogadores.none { it.id == _jogo.value.jogadorId }) {
                    animacao?.cancel()
                    cronometro?.cancel()
                    feedback?.cancel()
                    _jogo.value = _jogo.value.copy(
                        emAndamento = false, aguardandoJogador = false,
                        litIndex = -1, status = "O jogador desta partida foi excluído."
                    )
                }
            }
        }
    }

    fun limparMensagem() {
        _mensagem.value = null
    }

    fun selecionarJogador(jogador: Class_Jogador) {
        if (_jogo.value.emAndamento || _jogo.value.salvando || _jogo.value.erroAoSalvar) {
            _mensagem.value = "Finalize e salve a partida antes de trocar de jogador."
            return
        }
        if (_dados.value.jogadores.any { it.id == jogador.id }) {
            savedStateHandle["jogadorAtivoId"] = jogador.id
        }
    }

    fun adicionarJogador(nome: String, aoSalvar: () -> Unit = {}) {
        if (_ocupado.value) return
        if (nome.isBlank()) {
            _mensagem.value = "Digite o nome do jogador."
            return
        }
        executarAlteracao {
            repository.adicionarJogador(nome.trim())
            aoSalvar()
            _mensagem.value = "Jogador adicionado. Selecione-o no Ranking."
        }
    }

    fun excluirJogador(jogador: Class_Jogador) {
        if (_jogo.value.jogadorId == jogador.id &&
            (_jogo.value.emAndamento || _jogo.value.salvando || _jogo.value.erroAoSalvar)) {
            _mensagem.value = "Finalize e salve a partida deste jogador antes de excluí-lo."
            return
        }
        executarAlteracao {
            repository.excluirJogador(jogador)
            if (jogadorAtivoId.value == jogador.id) savedStateHandle["jogadorAtivoId"] = null
        }
    }

    fun excluirPartida(partida: Class_Partida) {
        executarAlteracao { repository.excluirPartida(partida) }
    }

    private fun executarAlteracao(operacao: suspend () -> Unit) {
        if (_ocupado.value) return
        _ocupado.value = true
        viewModelScope.launch {
            try {
                operacao()
            } catch (erro: Exception) {
                if (erro is CancellationException) throw erro
                _mensagem.value = erro.message ?: "Não foi possível salvar a alteração."
            } finally {
                _ocupado.value = false
            }
        }
    }

    fun iniciarJogo(dificuldade: String) {
        if (_jogo.value.emAndamento || _jogo.value.salvando || _jogo.value.erroAoSalvar) return
        val jogador = _dados.value.jogadores.firstOrNull { it.id == jogadorAtivoId.value }
        if (jogador == null) {
            _mensagem.value = "Adicione um jogador nas Configurações e selecione-o no Ranking."
            return
        }
        cronometro?.cancel()
        feedback?.cancel()
        _jogo.value = EstadoJogo(
            jogadorId = jogador.id, dificuldade = dificuldade,
            sequencia = listOf(Random.nextInt(4)), rodada = 1,
            emAndamento = true, sessaoId = UUID.randomUUID().toString()
        )
        mostrarSequencia()
    }

    private fun mostrarSequencia() {
        animacao?.cancel()
        feedback?.cancel()
        _jogo.value = _jogo.value.copy(
            aguardandoJogador = false, litIndex = -1, status = "Observe..."
        )
        animacao = viewModelScope.launch {
            delay(600)
            for (cor in _jogo.value.sequencia) {
                _jogo.value = _jogo.value.copy(litIndex = cor)
                delay(500)
                _jogo.value = _jogo.value.copy(litIndex = -1)
                delay(300)
            }
            _jogo.value = _jogo.value.copy(
                indiceJogador = 0, status = "Sua vez!", aguardandoJogador = true
            )
            iniciarCronometro()
        }
    }

    private fun iniciarCronometro() {
        cronometro?.cancel()
        _jogo.value = _jogo.value.copy(tempoRestante = _jogo.value.tempoPorCor)
        cronometro = viewModelScope.launch {
            while (_jogo.value.tempoRestante > 0) {
                delay(1000)
                _jogo.value = _jogo.value.copy(tempoRestante = _jogo.value.tempoRestante - 1)
            }
            terminarJogo("Tempo esgotado!")
        }
    }

    fun tocarCor(cor: Int) {
        val atual = _jogo.value
        if (!atual.aguardandoJogador) return
        if (cor != atual.sequencia[atual.indiceJogador]) {
            terminarJogo("Sequência errada!")
            return
        }
        cronometro?.cancel()
        feedback?.cancel()
        _jogo.value = atual.copy(litIndex = cor, indiceJogador = atual.indiceJogador + 1)
        if (_jogo.value.indiceJogador == atual.sequencia.size) {
            _jogo.value = _jogo.value.copy(
                score = atual.score + 1, rodada = atual.rodada + 1,
                sequencia = atual.sequencia + Random.nextInt(4)
            )
            mostrarSequencia()
        } else {
            feedback = viewModelScope.launch {
                delay(150)
                _jogo.value = _jogo.value.copy(litIndex = -1)
            }
            iniciarCronometro()
        }
    }

    private fun terminarJogo(status: String) {
        if (!_jogo.value.emAndamento) return
        cronometro?.cancel()
        animacao?.cancel()
        feedback?.cancel()
        _jogo.value = _jogo.value.copy(
            emAndamento = false, aguardandoJogador = false,
            litIndex = -1, status = status, data = System.currentTimeMillis()
        )
        salvarResultado()
    }

    fun salvarResultado() {
        val resultado = _jogo.value
        val jogadorId = resultado.jogadorId ?: return
        if (resultado.emAndamento || resultado.salvando || resultado.data == 0L) return
        _jogo.value = resultado.copy(salvando = true, erroAoSalvar = false)
        viewModelScope.launch {
            try {
                repository.salvarPartida(
                    jogadorId, resultado.score, resultado.dificuldade,
                    resultado.sessaoId, resultado.data
                )
                _jogo.value = _jogo.value.copy(salvando = false, erroAoSalvar = false)
            } catch (erro: Exception) {
                if (erro is CancellationException) throw erro
                _jogo.value = _jogo.value.copy(salvando = false, erroAoSalvar = true)
                _mensagem.value = "Não foi possível salvar a partida. Toque em Tentar salvar novamente."
            }
        }
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                BancoViewModel(
                    BancoRepository(AppDatabase.getDatabase(context.applicationContext)),
                    createSavedStateHandle()
                )
            }
        }
    }
}
