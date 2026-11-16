package com.example.myapplication

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun Jogo(bancoViewModel: BancoViewModel, dificuldadeSelecionada: String = "Fácil") {
    val jogo by bancoViewModel.jogo.collectAsStateWithLifecycle()
    val dados by bancoViewModel.dados.collectAsStateWithLifecycle()
    val ativoId by bancoViewModel.jogadorAtivoId.collectAsStateWithLifecycle()
    val jogador = dados.jogadores.firstOrNull { it.id == ativoId }
    val dificuldade = if (jogo.emAndamento || jogo.salvando || jogo.erroAoSalvar) {
        jogo.dificuldade
    } else {
        dificuldadeSelecionada
    }

    val buttonColors = listOf(
        GeniusRed to GeniusRedLit,
        GeniusBlue to GeniusBlueLit,
        GeniusGreen to GeniusGreenLit,
        GeniusYellow to GeniusYellowLit
    )

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
            Text("Rodada: ${jogo.rodada}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Pontos: ${jogo.score}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                            isLit = jogo.litIndex == index,
                            enabled = jogo.aguardandoJogador,
                            onClick = { bancoViewModel.tocarCor(index) },
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
                jogador?.let { "Jogador: ${it.nome} • Recorde: ${it.recorde}" }
                    ?: "Adicione um jogador nas Configurações e selecione-o no Ranking.",
                color = Color.White
            )
            Text("Dificuldade: $dificuldade", color = Color.LightGray)
            val tempoPorCor = EstadoJogo(dificuldade = dificuldade).tempoPorCor
            Text("Limite: $tempoPorCor segundo(s) por cor", color = Color.LightGray)
            if (jogo.aguardandoJogador) {
                Text("Tempo: ${jogo.tempoRestante}s", fontSize = 20.sp, color = Color.White)
            }
            Text(jogo.status, fontSize = 22.sp, color = Color.White)
            Button(
                onClick = {
                    if (jogo.erroAoSalvar) bancoViewModel.salvarResultado()
                    else bancoViewModel.iniciarJogo(dificuldadeSelecionada)
                },
                enabled = !jogo.emAndamento && !jogo.salvando &&
                    (jogador != null || jogo.erroAoSalvar),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(52.dp)
            ) {
                Text(
                    when {
                        jogo.salvando -> "Salvando..."
                        jogo.erroAoSalvar -> "Tentar salvar novamente"
                        else -> "Iniciar"
                    },
                    fontSize = 18.sp
                )
            }
        }
    }
}
