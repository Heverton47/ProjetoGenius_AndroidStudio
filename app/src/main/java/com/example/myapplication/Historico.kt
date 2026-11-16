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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Historico(bancoViewModel: BancoViewModel, isAdmin: Boolean = false) {
    val dados by bancoViewModel.dados.collectAsStateWithLifecycle()
    val ocupado by bancoViewModel.ocupado.collectAsStateWithLifecycle()
    var excluir by remember { mutableStateOf<Class_Partida?>(null) }
    val games = dados.partidas.sortedWith(
        compareByDescending<Class_Partida> { it.data }.thenByDescending { it.id }
    )
    val jogadores = dados.jogadores.associateBy { it.id }
    val formatoData = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
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

        if (filteredGames.isEmpty()) {
            Text("Nenhuma partida registrada.", color = Color.LightGray)
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
                            Text(jogadores[game.jogadorId]?.nome ?: "Jogador removido", color = Color.White)
                            Text("${game.pontuacao} pontos", fontSize = 18.sp, color = Color.White)
                            Text("${game.dificuldade} • ${formatoData.format(Date(game.data))}", fontSize = 14.sp, color = Color.LightGray)
                        }
                        if (isAdmin) {
                            IconButton(onClick = { excluir = game }, enabled = !ocupado) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir partida", tint = GeniusRed)
                            }
                        }
                    }
                }
            }
        }
    }

    excluir?.let { partida ->
        AlertDialog(
            onDismissRequest = { excluir = null },
            title = { Text("Excluir partida?") },
            text = { Text("Deseja excluir esta partida do histórico? O recorde do jogador será mantido.") },
            confirmButton = {
                TextButton(
                    enabled = !ocupado,
                    onClick = {
                        bancoViewModel.excluirPartida(partida)
                        excluir = null
                    }
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { excluir = null }) { Text("Cancelar") }
            }
        )
    }
}
