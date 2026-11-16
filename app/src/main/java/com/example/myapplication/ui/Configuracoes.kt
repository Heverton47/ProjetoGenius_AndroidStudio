package com.example.myapplication.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.remote.Desafio
import com.example.myapplication.logic.RegrasJogo
import com.example.myapplication.viewmodel.GameUiState

/**
 * Configurações do jogador: cadastro de jogador, dificuldade e escolha do desafio.
 * Os desafios exibidos aqui vêm do Firestore (cadastrados pelo administrador) e
 * o desafio escolhido é realmente usado na partida.
 */
@Composable
fun Configuracoes(
    ui: GameUiState,
    onAdicionarJogador: (String, () -> Unit) -> Unit,
    onDificuldade: (String) -> Unit,
    onDesafio: (Desafio?) -> Unit,
    onExit: () -> Unit
) {
    var nomePlayer by remember { mutableStateOf("") }
    var mostrarDialogDesafios by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Configurações", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nomePlayer,
            onValueChange = { nomePlayer = it },
            label = { Text("Nome do jogador") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { onAdicionarJogador(nomePlayer) { nomePlayer = "" } },
            enabled = !ui.ocupado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Adicionar jogador", color = Color.White)
        }

        Spacer(Modifier.height(24.dp))

        Text("Dificuldade", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)

        Column(Modifier.selectableGroup()) {
            RegrasJogo.DIFICULDADES.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = ui.dificuldade == option,
                            onClick = { onDificuldade(option) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = ui.dificuldade == option, onClick = null)
                    Text(option, color = Color.White, modifier = Modifier.padding(start = 12.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Desafio do Jogo", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Text(
            text = "Desafio ativo: ${ui.desafioSelecionado?.nome ?: "Modo livre (sequência aleatória)"}",
            fontSize = 14.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        ui.desafioSelecionado?.let {
            Text("Sequência: ${it.sequencia}", fontSize = 14.sp, color = Color.LightGray)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { mostrarDialogDesafios = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Alternar Desafio", color = Color.White)
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar ao início", color = Color.White)
        }
    }

    // Diálogo para escolha do desafio (lista vinda do Firestore + opção de modo livre)
    if (mostrarDialogDesafios) {
        AlertDialog(
            onDismissRequest = { mostrarDialogDesafios = false },
            title = { Text("Selecionar Desafio") },
            text = {
                Column(
                    Modifier
                        .selectableGroup()
                        .verticalScroll(rememberScrollState())
                ) {
                    OpcaoDesafio(
                        texto = "Modo livre (sequência aleatória)",
                        selecionado = ui.desafioSelecionado == null,
                        onClick = { onDesafio(null) }
                    )
                    ui.desafios.forEach { desafio ->
                        OpcaoDesafio(
                            texto = desafio.nome,
                            selecionado = ui.desafioSelecionado?.id == desafio.id,
                            onClick = { onDesafio(desafio) }
                        )
                    }
                    if (ui.desafios.isEmpty()) {
                        Text(
                            "Nenhum desafio cadastrado pelo administrador.",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogDesafios = false }) {
                    Text("Confirmar")
                }
            }
        )
    }
}

@Composable
private fun OpcaoDesafio(texto: String, selecionado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selecionado, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selecionado, onClick = null)
        Text(text = texto, modifier = Modifier.padding(start = 12.dp))
    }
}
