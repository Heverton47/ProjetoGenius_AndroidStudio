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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

@Composable
fun Ranking(bancoViewModel: BancoViewModel, isAdmin: Boolean = false) {
    val dados by bancoViewModel.dados.collectAsStateWithLifecycle()
    val ativoId by bancoViewModel.jogadorAtivoId.collectAsStateWithLifecycle()
    val ocupado by bancoViewModel.ocupado.collectAsStateWithLifecycle()
    var excluir by remember { mutableStateOf<Class_Jogador?>(null) }
    val sortedRanking = dados.jogadores.sortedByDescending { it.recorde }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Ranking", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

        if (sortedRanking.isEmpty()) {
            Text("Nenhum jogador cadastrado. Adicione um nas Configurações.", color = Color.LightGray)
        }

        LazyColumn(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(sortedRanking, key = { _, item -> item.id }) { index, item ->
                Card(
                    onClick = { bancoViewModel.selecionarJogador(item) },
                    border = if (ativoId == item.id) BorderStroke(2.dp, GeniusYellow) else null,
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
                            text = "${index + 1}º  ${item.nome}" + if (ativoId == item.id) " • Selecionado" else "",
                            fontSize = 18.sp,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == 0) GeniusYellow else Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${item.recorde} pts", fontSize = 18.sp, color = Color.White)
                            if (isAdmin) {
                                IconButton(onClick = { excluir = item }, enabled = !ocupado) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir item do ranking", tint = GeniusRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    excluir?.let { jogador ->
        AlertDialog(
            onDismissRequest = { excluir = null },
            title = { Text("Excluir jogador?") },
            text = { Text("Excluir ${jogador.nome} e todas as partidas vinculadas?") },
            confirmButton = {
                TextButton(
                    enabled = !ocupado,
                    onClick = {
                        bancoViewModel.excluirJogador(jogador)
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
