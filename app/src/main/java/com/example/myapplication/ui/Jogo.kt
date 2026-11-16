package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.GeniusBlue
import com.example.myapplication.ui.theme.GeniusBlueLit
import com.example.myapplication.ui.theme.GeniusGreen
import com.example.myapplication.ui.theme.GeniusGreenLit
import com.example.myapplication.ui.theme.GeniusRed
import com.example.myapplication.ui.theme.GeniusRedLit
import com.example.myapplication.ui.theme.GeniusYellow
import com.example.myapplication.ui.theme.GeniusYellowLit
import com.example.myapplication.viewmodel.GameUiState

/** Tela do jogo. Toda a lógica está no GameViewModel; aqui só desenhamos o GameUiState. */
@Composable
fun Jogo(
    ui: GameUiState,
    onIniciar: () -> Unit,
    onTocarCor: (Int) -> Unit,
    onSalvarNovamente: () -> Unit
) {
    val jogador = ui.jogadorAtivo

    val buttonColors = listOf(
        GeniusRed to GeniusRedLit,
        GeniusBlue to GeniusBlueLit,
        GeniusGreen to GeniusGreenLit,
        GeniusYellow to GeniusYellowLit
    )

    // Durante a partida mostra o desafio usado nela; fora dela, o escolhido nas Configurações.
    val nomeDesafio = if (ui.partidaAberta) ui.nomeDesafioPartida else ui.desafioSelecionado?.nome
    val textoModo = if (nomeDesafio != null) "Desafio: $nomeDesafio" else "Livre (sequência aleatória)"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Rodada: ${ui.rodada}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Pontos: ${ui.score}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            for (linha in 0..1) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (coluna in 0..1) {
                        val index = linha * 2 + coluna
                        val (normal, lit) = buttonColors[index]
                        GeniusButton(
                            normalColor = normal,
                            litColor = lit,
                            isLit = ui.litIndex == index,
                            enabled = ui.aguardandoJogador,
                            onClick = { onTocarCor(index) },
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp)
                        )
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = jogador?.let { "Jogador: ${it.nome} • Recorde: ${it.recorde}" }
                    ?: "Adicione um jogador nas Configurações e selecione-o no Ranking.",
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text("Dificuldade: ${ui.dificuldadeExibida}", color = Color.LightGray)
            Text("Modo: $textoModo", color = Color.LightGray)
            Text("Limite: ${ui.tempoPorCor} segundo(s) por cor", color = Color.LightGray)
            if (ui.aguardandoJogador) {
                Text("Tempo: ${ui.tempoRestante}s", fontSize = 20.sp, color = Color.White)
            }
            Text(ui.status, fontSize = 22.sp, color = Color.White)
            Button(
                onClick = {
                    if (ui.erroAoSalvar) onSalvarNovamente() else onIniciar()
                },
                enabled = !ui.emAndamento && !ui.salvando &&
                    (jogador != null || ui.erroAoSalvar),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(52.dp)
            ) {
                Text(
                    when {
                        ui.salvando -> "Salvando..."
                        ui.erroAoSalvar -> "Tentar salvar novamente"
                        else -> "Iniciar"
                    },
                    fontSize = 18.sp
                )
            }
        }
    }
}
