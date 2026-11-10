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

/**
 * Tela do jogo (SÓ VISUAL por enquanto).
 *
 * O "Iniciar" aqui apenas pisca uma sequência fixa para provar que a UI funciona.
 * Na Etapa 3 (lógica) e na Etapa 5 (ViewModel) esse estado e essas regras
 * saem daqui e vão para o GameViewModel / GameUiState.
 */
@Composable
fun Jogo() {
    val scope = rememberCoroutineScope()

    // Estado local temporário (será substituído pelo UiState do ViewModel)
    var litIndex by remember { mutableIntStateOf(-1) }      // -1 = nenhum botão aceso
    var status by remember { mutableStateOf("Toque em Iniciar") }
    var mostrandoSequencia by remember { mutableStateOf(false) }
    var rodada by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }

    // Par (cor normal, cor acesa) de cada botão, na ordem: vermelho, azul, verde, amarelo
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
        // Topo: rodada e pontuação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Rodada: $rodada", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Pontos: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Centro: grade 2x2 de botões
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
                            enabled = !mostrandoSequencia,
                            onClick = {
                                // Feedback visual ao tocar (a verificação de acerto vem na Etapa 3)
                                scope.launch {
                                    litIndex = index
                                    delay(200)
                                    litIndex = -1
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

        // Base: mensagem de status e botão Iniciar
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(status, fontSize = 22.sp, color = Color.White)
            Button(
                onClick = {
                    scope.launch {
                        mostrandoSequencia = true
                        status = "Observe..."
                        rodada = 1
                        for (i in listOf(0, 2, 1, 3)) { // sequência de exemplo
                            delay(300)
                            litIndex = i
                            delay(500)
                            litIndex = -1
                        }
                        status = "Sua vez!"
                        mostrandoSequencia = false
                    }
                },
                enabled = !mostrandoSequencia,
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
