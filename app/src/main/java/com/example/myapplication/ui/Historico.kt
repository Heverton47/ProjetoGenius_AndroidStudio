package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.logic.RegrasJogo
import com.example.myapplication.ui.theme.GeniusSurface
import com.example.myapplication.viewmodel.GameUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Histórico de partidas. O filtro por dificuldade, a ordenação e a troca do id pelo nome do jogador
 * acontecem no GameViewModel; a tela só mostra a lista pronta. O jogador não pode apagar partidas
 * (isso só é possível no painel Admin).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Historico(
    ui: GameUiState,
    onFiltrar: (String?) -> Unit
) {
    val formatoData = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Histórico", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = ui.filtroDificuldade == null,
                onClick = { onFiltrar(null) },
                label = { Text("Todas") }
            )
            RegrasJogo.DIFICULDADES.forEach { nivel ->
                FilterChip(
                    selected = ui.filtroDificuldade == nivel,
                    onClick = { onFiltrar(nivel) },
                    label = { Text(nivel) }
                )
            }
        }

        if (ui.historico.isEmpty()) {
            Text("Nenhuma partida registrada.", color = Color.LightGray)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(ui.historico, key = { it.id }) { partida ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GeniusSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(partida.nomeJogador, color = Color.White)
                        Text("${partida.pontuacao} pontos", fontSize = 18.sp, color = Color.White)
                        Text(
                            "${partida.dificuldade} • ${formatoData.format(Date(partida.data))}",
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}
