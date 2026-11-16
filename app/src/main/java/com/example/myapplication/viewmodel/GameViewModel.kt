package com.example.myapplication.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.Class_Jogador
import com.example.myapplication.data.local.Class_Partida
import com.example.myapplication.data.remote.Desafio
import com.example.myapplication.data.repository.BancoRepository
import com.example.myapplication.data.repository.DesafioRepository
import com.example.myapplication.data.repository.Resultado
import com.example.myapplication.logic.RegrasJogo
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Estado completo das telas do Modo Jogador (Jogo, Ranking, Histórico e Configurações).
 * As telas só leem este objeto; quem altera é o [GameViewModel].
 */
data class GameUiState(
    // ----- Listas já tratadas pelo ViewModel (sortedByDescending / filter / map) -----
    val ranking: List<Class_Jogador> = emptyList(),
    val historico: List<ItemHistorico> = emptyList(),
    val filtroDificuldade: String? = null,          // null = histórico sem filtro
    val desafios: List<Desafio> = emptyList(),       // vêm do Firestore

    // ----- Escolhas do jogador -----
    val jogadorAtivoId: Int? = null,
    val dificuldade: String = "Fácil",
    val desafioSelecionado: Desafio? = null,         // null = modo livre (sequência aleatória)

    // ----- Partida em andamento -----
    val jogadorPartidaId: Int? = null,
    val dificuldadePartida: String = "Fácil",
    val nomeDesafioPartida: String? = null,
    val alvo: List<Int> = emptyList(),               // sequência completa do desafio (vazia no modo livre)
    val sequencia: List<Int> = emptyList(),          // sequência da rodada atual
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
    val erroAoSalvar: Boolean = false,

    // ----- Feedback para a UI -----
    val ocupado: Boolean = false,
    val mensagem: String? = null
) {
    val jogadorAtivo: Class_Jogador?
        get() = ranking.firstOrNull { it.id == jogadorAtivoId }

    val partidaAberta: Boolean
        get() = emAndamento || salvando || erroAoSalvar

    /** Durante a partida vale a dificuldade escolhida no início; fora dela, a da tela de Configurações. */
    val dificuldadeExibida: String
        get() = if (partidaAberta) dificuldadePartida else dificuldade

    val tempoPorCor: Int
        get() = RegrasJogo.tempoPorCor(dificuldadeExibida)
}

class GameViewModel(
    private val bancoRepository: BancoRepository,
    private val desafioRepository: DesafioRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _ui = MutableStateFlow(
        GameUiState(
            jogadorAtivoId = savedStateHandle.get<Int>(CHAVE_JOGADOR),
            dificuldade = savedStateHandle.get<String>(CHAVE_DIFICULDADE) ?: "Fácil"
        )
    )
    val ui: StateFlow<GameUiState> = _ui.asStateFlow()

    // Dados "crus" vindos do Room. O ViewModel transforma estas listas em ranking e histórico.
    private var jogadores: List<Class_Jogador> = emptyList()
    private var partidas: List<Class_Partida> = emptyList()

    private var animacao: Job? = null
    private var cronometro: Job? = null
    private var feedback: Job? = null

    init {
        // Coleta o Flow do Room (via Repository) e atualiza o UiState de forma reativa.
        viewModelScope.launch {
            bancoRepository.dados.collect { resultado ->
                when (resultado) {
                    is Resultado.Erro -> avisar(resultado.mensagem)
                    is Resultado.Sucesso -> {
                        jogadores = resultado.valor.jogadores
                        partidas = resultado.valor.partidas
                        aplicarDados()
                    }
                }
            }
        }
        // Coleta os desafios que o administrador cadastrou no Firestore.
        viewModelScope.launch {
            desafioRepository.desafios.collect { resultado ->
                when (resultado) {
                    is Resultado.Erro -> avisar(resultado.mensagem)
                    is Resultado.Sucesso -> {
                        val idSalvo = savedStateHandle.get<String>(CHAVE_DESAFIO)
                        val escolhido = resultado.valor.firstOrNull { it.id == idSalvo }
                        if (idSalvo != null && escolhido == null) {
                            savedStateHandle[CHAVE_DESAFIO] = null // o admin excluiu o desafio escolhido
                        }
                        _ui.update { it.copy(desafios = resultado.valor, desafioSelecionado = escolhido) }
                    }
                }
            }
        }
    }

    /**
     * Regras de apresentação das listas (requisito: map, filter e sortedBy com lambdas):
     *  - Ranking: jogadores ordenados do maior para o menor recorde.
     *  - Histórico: partidas filtradas por dificuldade, ordenadas da mais recente e convertidas
     *    em [ItemHistorico] (com o nome do jogador no lugar do id).
     */
    private fun aplicarDados() {
        val nomes = jogadores.associate { it.id to it.nome }
        val rankingOrdenado = jogadores.sortedByDescending { it.recorde }
        _ui.update { atual ->
            atual.copy(
                ranking = rankingOrdenado,
                historico = partidas
                    .filter { atual.filtroDificuldade == null || it.dificuldade == atual.filtroDificuldade }
                    .sortedWith(compareByDescending<Class_Partida> { it.data }.thenByDescending { it.id })
                    .map {
                        ItemHistorico(
                            id = it.id,
                            nomeJogador = nomes[it.jogadorId] ?: "Jogador removido",
                            pontuacao = it.pontuacao,
                            dificuldade = it.dificuldade,
                            data = it.data
                        )
                    }
            )
        }

        val atual = _ui.value
        // Se o administrador excluiu o jogador selecionado, limpa a seleção.
        if (atual.jogadorAtivoId != null && jogadores.none { it.id == atual.jogadorAtivoId }) {
            savedStateHandle[CHAVE_JOGADOR] = null
            _ui.update { it.copy(jogadorAtivoId = null) }
        }
        // Se o jogador da partida em andamento foi excluído, encerra a partida.
        if (atual.emAndamento && jogadores.none { it.id == atual.jogadorPartidaId }) {
            animacao?.cancel()
            cronometro?.cancel()
            feedback?.cancel()
            _ui.update {
                it.copy(
                    emAndamento = false, aguardandoJogador = false, litIndex = -1,
                    status = "O jogador desta partida foi excluído."
                )
            }
        }
        // Se o jogador foi excluído enquanto o resultado ainda não tinha sido salvo, não há mais
        // o que salvar: libera a tela para uma nova partida em vez de ficar presa em "tentar de novo".
        if (atual.erroAoSalvar && jogadores.none { it.id == atual.jogadorPartidaId }) {
            _ui.update {
                it.copy(
                    erroAoSalvar = false,
                    status = "O jogador desta partida foi excluído."
                )
            }
        }
    }

    // ---------------------------------------------------------------- Ações da UI

    fun limparMensagem() {
        _ui.update { it.copy(mensagem = null) }
    }

    private fun avisar(texto: String) {
        _ui.update { it.copy(mensagem = texto) }
    }

    fun filtrarHistorico(dificuldade: String?) {
        _ui.update { it.copy(filtroDificuldade = dificuldade) }
        aplicarDados()
    }

    fun definirDificuldade(dificuldade: String) {
        savedStateHandle[CHAVE_DIFICULDADE] = dificuldade
        _ui.update { it.copy(dificuldade = dificuldade) }
    }

    /** null = modo livre (sequência aleatória). */
    fun selecionarDesafio(desafio: Desafio?) {
        savedStateHandle[CHAVE_DESAFIO] = desafio?.id
        _ui.update { it.copy(desafioSelecionado = desafio) }
    }

    fun selecionarJogador(jogador: Class_Jogador) {
        if (_ui.value.partidaAberta) {
            avisar("Finalize e salve a partida antes de trocar de jogador.")
            return
        }
        if (jogadores.any { it.id == jogador.id }) {
            savedStateHandle[CHAVE_JOGADOR] = jogador.id
            _ui.update { it.copy(jogadorAtivoId = jogador.id) }
        }
    }

    fun adicionarJogador(nome: String, aoSalvar: () -> Unit = {}) {
        if (_ui.value.ocupado) return
        if (nome.isBlank()) {
            avisar("Digite o nome do jogador.")
            return
        }
        _ui.update { it.copy(ocupado = true) }
        viewModelScope.launch {
            when (val resultado = bancoRepository.adicionarJogador(nome.trim())) {
                is Resultado.Sucesso -> {
                    aoSalvar()
                    _ui.update {
                        it.copy(ocupado = false, mensagem = "Jogador adicionado. Selecione-o no Ranking.")
                    }
                }
                is Resultado.Erro -> _ui.update { it.copy(ocupado = false, mensagem = resultado.mensagem) }
            }
        }
    }

    // ---------------------------------------------------------------- Lógica do jogo

    fun iniciarJogo() {
        val atual = _ui.value
        if (atual.partidaAberta) return
        val jogador = atual.jogadorAtivo
        if (jogador == null) {
            avisar("Adicione um jogador nas Configurações e selecione-o no Ranking.")
            return
        }
        cronometro?.cancel()
        feedback?.cancel()

        // Desafio do Firestore: a sequência é fixa e cresce uma cor por rodada.
        // Modo livre: a sequência é aleatória e cresce uma cor por rodada.
        val alvo = atual.desafioSelecionado
            ?.let { RegrasJogo.sequenciaDoTexto(it.sequencia) }
            .orEmpty()
        val primeiraSequencia = if (alvo.isNotEmpty()) alvo.take(1) else listOf(Random.nextInt(4))

        _ui.update {
            it.copy(
                jogadorPartidaId = jogador.id,
                dificuldadePartida = it.dificuldade,
                nomeDesafioPartida = if (alvo.isNotEmpty()) it.desafioSelecionado?.nome else null,
                alvo = alvo,
                sequencia = primeiraSequencia,
                indiceJogador = 0,
                litIndex = -1,
                rodada = 1,
                score = 0,
                tempoRestante = 0,
                emAndamento = true,
                aguardandoJogador = false,
                salvando = false,
                erroAoSalvar = false,
                data = 0,
                sessaoId = UUID.randomUUID().toString()
            )
        }
        mostrarSequencia()
    }

    private fun mostrarSequencia() {
        animacao?.cancel()
        feedback?.cancel()
        _ui.update { it.copy(aguardandoJogador = false, litIndex = -1, status = "Observe...") }
        animacao = viewModelScope.launch {
            delay(600)
            for (cor in _ui.value.sequencia) {
                _ui.update { it.copy(litIndex = cor) }
                delay(500)
                _ui.update { it.copy(litIndex = -1) }
                delay(300)
            }
            _ui.update { it.copy(indiceJogador = 0, status = "Sua vez!", aguardandoJogador = true) }
            iniciarCronometro()
        }
    }

    private fun iniciarCronometro() {
        cronometro?.cancel()
        _ui.update { it.copy(tempoRestante = RegrasJogo.tempoPorCor(it.dificuldadePartida)) }
        cronometro = viewModelScope.launch {
            while (_ui.value.tempoRestante > 0) {
                delay(1000)
                _ui.update { it.copy(tempoRestante = it.tempoRestante - 1) }
            }
            terminarJogo("Tempo esgotado!")
        }
    }

    fun tocarCor(cor: Int) {
        val atual = _ui.value
        if (!atual.aguardandoJogador) return
        if (cor != atual.sequencia[atual.indiceJogador]) {
            terminarJogo("Sequência errada!")
            return
        }
        cronometro?.cancel()
        feedback?.cancel()
        val novoIndice = atual.indiceJogador + 1

        if (novoIndice == atual.sequencia.size) {
            // Acertou a rodada inteira.
            _ui.update { it.copy(litIndex = cor, indiceJogador = novoIndice, score = atual.score + 1) }
            val noDesafio = atual.alvo.isNotEmpty()
            if (noDesafio && atual.sequencia.size == atual.alvo.size) {
                terminarJogo("Desafio concluído!") // completou a sequência inteira do desafio
            } else {
                val proxima = if (noDesafio) {
                    atual.alvo.take(atual.sequencia.size + 1)
                } else {
                    atual.sequencia + Random.nextInt(4)
                }
                _ui.update { it.copy(rodada = atual.rodada + 1, sequencia = proxima) }
                mostrarSequencia()
            }
        } else {
            // Acertou esta cor; faltam outras da mesma rodada.
            _ui.update { it.copy(litIndex = cor, indiceJogador = novoIndice) }
            feedback = viewModelScope.launch {
                delay(150)
                _ui.update { it.copy(litIndex = -1) }
            }
            iniciarCronometro()
        }
    }

    private fun terminarJogo(status: String) {
        if (!_ui.value.emAndamento) return
        cronometro?.cancel()
        animacao?.cancel()
        feedback?.cancel()
        _ui.update {
            it.copy(
                emAndamento = false, aguardandoJogador = false, litIndex = -1,
                status = status, data = System.currentTimeMillis()
            )
        }
        salvarResultado()
    }

    fun salvarResultado() {
        val resultado = _ui.value
        val jogadorId = resultado.jogadorPartidaId ?: return
        if (resultado.emAndamento || resultado.salvando || resultado.data == 0L) return
        _ui.update { it.copy(salvando = true, erroAoSalvar = false) }
        viewModelScope.launch {
            when (
                val retorno = bancoRepository.salvarPartida(
                    jogadorId, resultado.score, resultado.dificuldadePartida,
                    resultado.sessaoId, resultado.data
                )
            ) {
                is Resultado.Sucesso ->
                    _ui.update { it.copy(salvando = false, erroAoSalvar = false) }
                is Resultado.Erro ->
                    _ui.update {
                        it.copy(
                            salvando = false,
                            erroAoSalvar = true,
                            mensagem = "${retorno.mensagem} Toque em Tentar salvar novamente."
                        )
                    }
            }
        }
    }

    private companion object {
        const val CHAVE_JOGADOR = "jogadorAtivoId"
        const val CHAVE_DIFICULDADE = "dificuldade"
        const val CHAVE_DESAFIO = "desafioId"
    }
}
