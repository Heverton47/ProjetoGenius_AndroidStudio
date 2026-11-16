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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun Jogo(dificuldade: String = "Fácil") {
    val scope = rememberCoroutineScope()
    var litIndex by remember { mutableIntStateOf(-1) }
    var status by remember { mutableStateOf("Toque em Iniciar") }
    var jogoEmAndamento by remember { mutableStateOf(false) }
    var aguardandoJogador by remember { mutableStateOf(false) }
    var sequencia by remember { mutableStateOf(emptyList<Int>()) }
    var indiceJogador by remember { mutableIntStateOf(0) }
    var rodada by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var tempoRestante by remember { mutableIntStateOf(0) }

    val tempoPorCor = when (dificuldade) {
        "Difícil" -> 1
        "Médio" -> 3
        else -> 5
    }

    suspend fun mostrarSequencia() {
        aguardandoJogador = false
        status = "Observe..."
        litIndex = -1
        delay(600)
        for (cor in sequencia) {
            litIndex = cor
            delay(500)
            litIndex = -1
            delay(300)
        }
        indiceJogador = 0
        tempoRestante = tempoPorCor
        status = "Sua vez!"
        aguardandoJogador = true
    }

    LaunchedEffect(aguardandoJogador, indiceJogador) {
        if (aguardandoJogador) {
            tempoRestante = tempoPorCor
            while (tempoRestante > 0) {
                delay(1000)
                tempoRestante--
            }
            aguardandoJogador = false
            jogoEmAndamento = false
            litIndex = -1
            status = "Tempo esgotado!"
        }
    }

    LaunchedEffect(litIndex, aguardandoJogador) {
        if (aguardandoJogador && litIndex != -1) {
            delay(150)
            litIndex = -1
        }
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
            Text("Rodada: $rodada", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Pontos: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                            isLit = litIndex == index,
                            enabled = aguardandoJogador,
                            onClick = {
                                if (index != sequencia[indiceJogador]) {
                                    aguardandoJogador = false
                                    jogoEmAndamento = false
                                    litIndex = -1
                                    status = "Sequência errada!"
                                } else {
                                    litIndex = index
                                    indiceJogador++
                                    if (indiceJogador == sequencia.size) {
                                        aguardandoJogador = false
                                        score++
                                        rodada++
                                        sequencia = sequencia + Random.nextInt(4)
                                        scope.launch { mostrarSequencia() }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp)
                        )
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Dificuldade: $dificuldade", color = Color.LightGray)
            Text("Limite: $tempoPorCor segundo(s) por cor", color = Color.LightGray)
            if (aguardandoJogador) {
                Text("Tempo: ${tempoRestante}s", fontSize = 20.sp, color = Color.White)
            }
            Text(status, fontSize = 22.sp, color = Color.White)
            Button(
                onClick = {
                    jogoEmAndamento = true
                    score = 0
                    rodada = 1
                    sequencia = listOf(Random.nextInt(4))
                    scope.launch { mostrarSequencia() }
                },
                enabled = !jogoEmAndamento,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(52.dp)
            ) {
                Text("Iniciar", fontSize = 18.sp)
            }
        }
    }
}
