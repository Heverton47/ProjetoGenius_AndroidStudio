package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.Class_Jogador
import com.example.myapplication.data.remote.Desafio
import com.example.myapplication.ui.theme.GeniusBackground
import com.example.myapplication.ui.theme.GeniusBlue
import com.example.myapplication.ui.theme.GeniusRed
import com.example.myapplication.ui.theme.GeniusSurface
import com.example.myapplication.viewmodel.AdminUiState
import com.example.myapplication.viewmodel.ItemHistorico
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ABAS = listOf("Desafios", "Jogadores", "Partidas")

/**
 * Painel do Administrador, com 3 áreas:
 *  - Desafios (Firestore): CRUD completo. Os desafios criados aqui aparecem para o jogador nas Configurações.
 *  - Jogadores (Room): editar nome e excluir.
 *  - Partidas (Room): excluir do histórico (o jogador comum não consegue apagar histórico).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Admin(
    ui: AdminUiState,
    onSalvarDesafio: (String?, String, String, () -> Unit) -> Unit,
    onExcluirDesafio: (Desafio) -> Unit,
    onLimparErroDesafio: () -> Unit,
    onEditarJogador: (Class_Jogador, String) -> Unit,
    onExcluirJogador: (Class_Jogador) -> Unit,
    onExcluirPartida: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var aba by rememberSaveable { mutableIntStateOf(0) }

    // Diálogo de desafio (criar/editar)
    var mostrarDialogDesafio by remember { mutableStateOf(false) }
    var desafioEditando by remember { mutableStateOf<Desafio?>(null) }
    var nomeDesafio by remember { mutableStateOf("") }
    var sequenciaDesafio by remember { mutableStateOf("") }

    // Diálogos de jogador e partida
    var jogadorEditando by remember { mutableStateOf<Class_Jogador?>(null) }
    var nomeJogador by remember { mutableStateOf("") }
    var jogadorExcluir by remember { mutableStateOf<Class_Jogador?>(null) }
    var partidaExcluir by remember { mutableStateOf<ItemHistorico?>(null) }

    val formatoData = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Scaffold(containerColor = GeniusBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Painel Admin", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                OutlinedButton(onClick = onLogout) { Text("Sair", color = Color.White) }
            }

            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ABAS.forEachIndexed { indice, titulo ->
                    FilterChip(
                        selected = aba == indice,
                        onClick = { aba = indice },
                        label = { Text(titulo) }
                    )
                }
            }

            when (aba) {
                0 -> {
                    Button(
                        onClick = {
                            desafioEditando = null
                            nomeDesafio = ""
                            sequenciaDesafio = ""
                            onLimparErroDesafio()
                            mostrarDialogDesafio = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GeniusBlue)
                    ) {
                        Text("Novo Desafio", color = Color.White)
                    }
                    Text(
                        "Desafios cadastrados: ${ui.desafios.size}",
                        color = Color.LightGray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ui.desafios, key = { it.id }) { desafio ->
                            LinhaCard(
                                titulo = desafio.nome,
                                subtitulo = desafio.sequencia,
                                onEditar = {
                                    desafioEditando = desafio
                                    nomeDesafio = desafio.nome
                                    sequenciaDesafio = desafio.sequencia
                                    onLimparErroDesafio()
                                    mostrarDialogDesafio = true
                                },
                                onExcluir = { onExcluirDesafio(desafio) },
                                habilitado = !ui.ocupado
                            )
                        }
                    }
                }

                1 -> {
                    Text(
                        "Jogadores cadastrados: ${ui.jogadores.size}",
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ui.jogadores, key = { it.id }) { jogador ->
                            LinhaCard(
                                titulo = jogador.nome,
                                subtitulo = "Recorde: ${jogador.recorde}",
                                onEditar = {
                                    jogadorEditando = jogador
                                    nomeJogador = jogador.nome
                                },
                                onExcluir = { jogadorExcluir = jogador },
                                habilitado = !ui.ocupado
                            )
                        }
                    }
                }

                else -> {
                    Text(
                        "Partidas registradas: ${ui.partidas.size}",
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ui.partidas, key = { it.id }) { partida ->
                            LinhaCard(
                                titulo = "${partida.nomeJogador} • ${partida.pontuacao} pontos",
                                subtitulo = "${partida.dificuldade} • ${formatoData.format(Date(partida.data))}",
                                onEditar = null,
                                onExcluir = { partidaExcluir = partida },
                                habilitado = !ui.ocupado
                            )
                        }
                    }
                }
            }
        }
    }

    // ---------- Diálogo: criar/editar desafio ----------
    if (mostrarDialogDesafio) {
        AlertDialog(
            onDismissRequest = { mostrarDialogDesafio = false },
            title = { Text(if (desafioEditando == null) "Novo desafio" else "Editar desafio") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nomeDesafio,
                        onValueChange = { nomeDesafio = it },
                        label = { Text("Nome") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sequenciaDesafio,
                        onValueChange = { sequenciaDesafio = it },
                        label = { Text("Sequência (ex.: Vermelho, Azul)") },
                        supportingText = { Text("Cores: Vermelho, Azul, Verde e Amarelo") },
                        isError = ui.erroDesafio != null
                    )
                    ui.erroDesafio?.let {
                        Text(it, color = GeniusRed, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !ui.ocupado,
                    onClick = {
                        onSalvarDesafio(desafioEditando?.id, nomeDesafio, sequenciaDesafio) {
                            mostrarDialogDesafio = false
                        }
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogDesafio = false }) { Text("Cancelar") }
            }
        )
    }

    // ---------- Diálogo: editar nome do jogador (Update do CRUD) ----------
    jogadorEditando?.let { jogador ->
        AlertDialog(
            onDismissRequest = { jogadorEditando = null },
            title = { Text("Editar jogador") },
            text = {
                OutlinedTextField(
                    value = nomeJogador,
                    onValueChange = { nomeJogador = it },
                    label = { Text("Nome do jogador") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !ui.ocupado,
                    onClick = {
                        onEditarJogador(jogador, nomeJogador)
                        jogadorEditando = null
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { jogadorEditando = null }) { Text("Cancelar") }
            }
        )
    }

    // ---------- Confirmação: excluir jogador ----------
    jogadorExcluir?.let { jogador ->
        AlertDialog(
            onDismissRequest = { jogadorExcluir = null },
            title = { Text("Excluir jogador?") },
            text = { Text("Excluir ${jogador.nome} e todas as partidas vinculadas?") },
            confirmButton = {
                TextButton(
                    enabled = !ui.ocupado,
                    onClick = {
                        onExcluirJogador(jogador)
                        jogadorExcluir = null
                    }
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { jogadorExcluir = null }) { Text("Cancelar") }
            }
        )
    }

    // ---------- Confirmação: excluir partida ----------
    partidaExcluir?.let { partida ->
        AlertDialog(
            onDismissRequest = { partidaExcluir = null },
            title = { Text("Excluir partida?") },
            text = { Text("Deseja excluir esta partida do histórico? O recorde do jogador será mantido.") },
            confirmButton = {
                TextButton(
                    enabled = !ui.ocupado,
                    onClick = {
                        onExcluirPartida(partida.id)
                        partidaExcluir = null
                    }
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { partidaExcluir = null }) { Text("Cancelar") }
            }
        )
    }
}

/** Card padrão das listas do painel: texto à esquerda e ícones de editar/excluir à direita. */
@Composable
private fun LinhaCard(
    titulo: String,
    subtitulo: String,
    onEditar: (() -> Unit)?,
    onExcluir: () -> Unit,
    habilitado: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GeniusSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(subtitulo, fontSize = 14.sp, color = Color.LightGray)
            }
            if (onEditar != null) {
                IconButton(onClick = onEditar, enabled = habilitado) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White)
                }
            }
            IconButton(onClick = onExcluir, enabled = habilitado) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = GeniusRed)
            }
        }
    }
}
