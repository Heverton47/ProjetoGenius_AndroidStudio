package com.example.myapplication

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.GeniusSurface
import com.example.myapplication.ui.theme.GeniusYellow

// Modelo temporário só para a tela. Na Etapa 4 isso vira a Entity do Room.
private data class RankingItem(val nome: String, val score: Int)

/** Ranking: lista ordenada da maior para a menor pontuação. */
@Composable
fun Ranking() {
    // Dados de exemplo (fora de ordem de propósito, para provar que a ordenação funciona)
    val ranking = remember {
        listOf(
            RankingItem("Ana", 12),
            RankingItem("Bruno", 25),
            RankingItem("Carla", 8),
            RankingItem("Diego", 19),
            RankingItem("Elisa", 31)
        ).sortedByDescending { it.score } // requisito do professor: sortedBy/lambda
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Ranking", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

        LazyColumn(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(ranking) { index, item ->
                Card(
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
                            text = "${index + 1}º  ${item.nome}",
                            fontSize = 18.sp,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == 0) GeniusYellow else Color.White
                        )
                        Text("${item.score} pts", fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
