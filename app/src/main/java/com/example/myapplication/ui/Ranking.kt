package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.Class_Jogador
import com.example.myapplication.ui.theme.GeniusSurface
import com.example.myapplication.ui.theme.GeniusYellow
import com.example.myapplication.viewmodel.GameUiState

/**
 * Ranking: a lista já chega ordenada (maior recorde primeiro) do GameViewModel.
 * Tocar em um card seleciona o jogador que vai jogar. Excluir jogadores é função do painel Admin.
 */
@Composable
fun Ranking(
    ui: GameUiState,
    onSelecionar: (Class_Jogador) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Ranking", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

        if (ui.ranking.isEmpty()) {
            Text("Nenhum jogador cadastrado. Adicione um nas Configurações.", color = Color.LightGray)
        }

        LazyColumn(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(ui.ranking, key = { _, item -> item.id }) { index, item ->
                val selecionado = ui.jogadorAtivoId == item.id
                Card(
                    onClick = { onSelecionar(item) },
                    border = if (selecionado) BorderStroke(2.dp, GeniusYellow) else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GeniusSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index + 1}º  ${item.nome}" + if (selecionado) " • Selecionado" else "",
                            fontSize = 18.sp,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == 0) GeniusYellow else Color.White
                        )
                        Text("${item.recorde} pts", fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
