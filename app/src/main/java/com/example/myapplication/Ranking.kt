package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.GeniusRed
import com.example.myapplication.ui.theme.GeniusSurface
import com.example.myapplication.ui.theme.GeniusYellow

// Modelo temporário só para a tela. Na Etapa 4 isso vira a Entity do Room.
private data class RankingItem(val id: Int, val nome: String, val score: Int)

/** Ranking: lista ordenada da maior para a menor pontuação. */
@Composable
fun Ranking(isAdmin: Boolean = false) {
    // Dados de exemplo (a lista é mutável para permitir exclusão se for Admin)
    val ranking = remember {
        mutableStateListOf(
            RankingItem(1, "Ana", 12),
            RankingItem(2, "Bruno", 25),
            RankingItem(3, "Carla", 8),
            RankingItem(4, "Diego", 19),
            RankingItem(5, "Elisa", 31)
        )
    }

    // Ordena a lista do maior para o menor score
    val sortedRanking = ranking.sortedByDescending { it.score }

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
            itemsIndexed(sortedRanking, key = { _, item -> item.id }) { index, item ->
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${item.score} pts", fontSize = 18.sp, color = Color.White)
                            if (isAdmin) {
                                IconButton(onClick = { ranking.remove(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir item do ranking", tint = GeniusRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
