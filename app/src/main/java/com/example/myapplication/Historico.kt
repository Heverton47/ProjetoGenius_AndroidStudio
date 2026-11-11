package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.GeniusRed
import com.example.myapplication.ui.theme.GeniusSurface

// Modelo temporário só para a tela. Na Etapa 4 isso vira a Entity do Room.
private data class GameRecord(
    val id: Int,
    val dificuldade: String,
    val score: Int,
    val data: String
)

/** Histórico de partidas: filtro por dificuldade e exclusão de itens. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Historico(isAdmin: Boolean = false) {
    // Dados de exemplo (a lista é "observável": mudou, a tela se redesenha)
    val games = remember {
        mutableStateListOf(
            GameRecord(1, "Fácil", 12, "05/11/2026"),
            GameRecord(2, "Médio", 20, "06/11/2026"),
            GameRecord(3, "Difícil", 9, "07/11/2026"),
            GameRecord(4, "Fácil", 15, "08/11/2026"),
            GameRecord(5, "Médio", 27, "09/11/2026")
        )
    }
    // null = sem filtro (mostra todas)
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    // Requisito do professor: filter com lambda
    val filteredGames = games.filter { selectedFilter == null || it.dificuldade == selectedFilter }

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
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("Todas") }
            )
            listOf("Fácil", "Médio", "Difícil").forEach { level ->
                FilterChip(
                    selected = selectedFilter == level,
                    onClick = { selectedFilter = level },
                    label = { Text(level) }
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredGames, key = { it.id }) { game ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GeniusSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("${game.score} pontos", fontSize = 18.sp, color = Color.White)
                            Text("${game.dificuldade} • ${game.data}", fontSize = 14.sp, color = Color.LightGray)
                        }
                        if (isAdmin) {
                            IconButton(onClick = { games.remove(game) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir partida", tint = GeniusRed)
                            }
                        }
                    }
                }
            }
        }
    }
}
