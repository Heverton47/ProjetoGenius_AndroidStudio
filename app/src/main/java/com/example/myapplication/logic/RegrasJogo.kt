package com.example.myapplication.logic

/**
 * Regras do jogo que não dependem de tela, banco ou internet.
 * Ficam no pacote `logic` para o ViewModel só orquestrar.
 */
object RegrasJogo {
    /** A posição na lista é o índice do botão: 0 = vermelho, 1 = azul, 2 = verde, 3 = amarelo. */
    val NOMES_CORES = listOf("Vermelho", "Azul", "Verde", "Amarelo")

    val DIFICULDADES = listOf("Fácil", "Médio", "Difícil")

    /** Tempo (em segundos) que o jogador tem para tocar em cada cor. */
    fun tempoPorCor(dificuldade: String): Int = when (dificuldade) {
        "Difícil" -> 1
        "Médio" -> 3
        else -> 5
    }

    /**
     * Converte o texto digitado pelo administrador ("Vermelho, Azul, Verde")
     * em índices de botões ([0, 1, 2]). Devolve null se o texto estiver vazio
     * ou tiver alguma cor que não existe.
     */
    fun sequenciaDoTexto(texto: String): List<Int>? {
        val itens = texto.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (itens.isEmpty()) return null
        val indices = itens.map { item ->
            NOMES_CORES.indexOfFirst { it.equals(item, ignoreCase = true) }
        }
        return if (indices.any { it < 0 }) null else indices
    }

    /** Operação inversa: [0, 1, 2] vira "Vermelho, Azul, Verde". */
    fun textoDaSequencia(indices: List<Int>): String =
        indices.joinToString(", ") { NOMES_CORES[it] }
}
