package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.Class_Jogador
import com.example.myapplication.data.remote.Desafio
import com.example.myapplication.data.repository.BancoRepository
import com.example.myapplication.data.repository.DesafioRepository
import com.example.myapplication.data.repository.Resultado
import com.example.myapplication.logic.RegrasJogo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado da área do Administrador.
 * [logado] é o controle de perfil: enquanto for false, a tela do painel não é exibida.
 */
data class AdminUiState(
    val logado: Boolean = false,
    val erroLogin: String? = null,
    val desafios: List<Desafio> = emptyList(),     // Firestore
    val jogadores: List<Class_Jogador> = emptyList(), // Room
    val partidas: List<ItemHistorico> = emptyList(),  // Room
    val erroDesafio: String? = null,               // erro mostrado dentro do diálogo de desafio
    val ocupado: Boolean = false,
    val mensagem: String? = null
)

class AdminViewModel(
    private val bancoRepository: BancoRepository,
    private val desafioRepository: DesafioRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AdminUiState())
    val ui: StateFlow<AdminUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            bancoRepository.dados.collect { resultado ->
                when (resultado) {
                    is Resultado.Erro -> _ui.update { it.copy(mensagem = resultado.mensagem) }
                    is Resultado.Sucesso -> {
                        val nomes = resultado.valor.jogadores.associate { it.id to it.nome }
                        _ui.update {
                            it.copy(
                                jogadores = resultado.valor.jogadores.sortedBy { j -> j.nome.lowercase() },
                                partidas = resultado.valor.partidas
                                    .sortedByDescending { p -> p.data }
                                    .map { p ->
                                        ItemHistorico(
                                            id = p.id,
                                            nomeJogador = nomes[p.jogadorId] ?: "Jogador removido",
                                            pontuacao = p.pontuacao,
                                            dificuldade = p.dificuldade,
                                            data = p.data
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            desafioRepository.desafios.collect { resultado ->
                when (resultado) {
                    is Resultado.Erro -> _ui.update { it.copy(mensagem = resultado.mensagem) }
                    is Resultado.Sucesso -> _ui.update { it.copy(desafios = resultado.valor) }
                }
            }
        }
    }

    // ---------------------------------------------------------------- Login simulado

    /** Validação do login simulado. A tela só envia o que foi digitado e mostra o resultado. */
    fun entrar(usuario: String, senha: String) {
        if (usuario.trim() == USUARIO_ADMIN && senha == SENHA_ADMIN) {
            _ui.update { it.copy(logado = true, erroLogin = null) }
        } else {
            _ui.update { it.copy(logado = false, erroLogin = "Usuário ou senha inválidos") }
        }
    }

    fun limparErroLogin() {
        _ui.update { it.copy(erroLogin = null) }
    }

    fun sair() {
        _ui.update { it.copy(logado = false, erroLogin = null, erroDesafio = null) }
    }

    fun limparMensagem() {
        _ui.update { it.copy(mensagem = null) }
    }

    fun limparErroDesafio() {
        _ui.update { it.copy(erroDesafio = null) }
    }

    // ---------------------------------------------------------------- Desafios (Firestore)

    /**
     * Cria (id == null) ou atualiza (id != null) um desafio no Firestore.
     * [aoSalvar] só é chamado se deu certo (a tela usa para fechar o diálogo).
     */
    fun salvarDesafio(id: String?, nome: String, sequenciaDigitada: String, aoSalvar: () -> Unit) {
        if (nome.isBlank()) {
            _ui.update { it.copy(erroDesafio = "Digite o nome do desafio.") }
            return
        }
        val indices = RegrasJogo.sequenciaDoTexto(sequenciaDigitada)
        if (indices == null) {
            _ui.update {
                it.copy(
                    erroDesafio = "Sequência inválida. Use apenas " +
                        RegrasJogo.NOMES_CORES.joinToString(", ") + ", separadas por vírgula."
                )
            }
            return
        }
        val nomeLimpo = nome.trim()
        val sequencia = RegrasJogo.textoDaSequencia(indices) // padroniza maiúsculas/espaços
        executar(
            aoConcluir = { resultado ->
                when (resultado) {
                    is Resultado.Sucesso -> aoSalvar()
                    is Resultado.Erro -> _ui.update { it.copy(erroDesafio = resultado.mensagem) }
                }
            }
        ) {
            if (id == null) desafioRepository.adicionar(nomeLimpo, sequencia)
            else desafioRepository.atualizar(id, nomeLimpo, sequencia)
        }
    }

    fun excluirDesafio(desafio: Desafio) {
        executar { desafioRepository.excluir(desafio.id) }
    }

    // ---------------------------------------------------------------- Jogadores e partidas (Room)

    fun editarJogador(jogador: Class_Jogador, novoNome: String) {
        executar(mensagemSucesso = "Jogador atualizado.") {
            bancoRepository.atualizarNomeJogador(jogador, novoNome)
        }
    }

    fun excluirJogador(jogador: Class_Jogador) {
        executar { bancoRepository.excluirJogador(jogador) }
    }

    fun excluirPartida(partidaId: Int) {
        executar { bancoRepository.excluirPartida(partidaId) }
    }

    /** Todas as ações do painel passam por aqui: só funcionam com o perfil Admin logado. */
    private fun executar(
        mensagemSucesso: String? = null,
        aoConcluir: (Resultado<*>) -> Unit = {},
        bloco: suspend () -> Resultado<*>
    ) {
        if (!_ui.value.logado || _ui.value.ocupado) return
        _ui.update { it.copy(ocupado = true) }
        viewModelScope.launch {
            val resultado = bloco()
            _ui.update { estado ->
                estado.copy(
                    ocupado = false,
                    mensagem = when (resultado) {
                        is Resultado.Erro -> resultado.mensagem
                        is Resultado.Sucesso -> mensagemSucesso ?: estado.mensagem
                    }
                )
            }
            aoConcluir(resultado)
        }
    }

    private companion object {
        const val USUARIO_ADMIN = "admin"
        const val SENHA_ADMIN = "1234"
    }
}
