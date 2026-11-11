package com.example.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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

/** Configurações: nome do jogador, dificuldade e seleção de desafios (estado local por enquanto). */
@Composable
fun Configuracoes(onExit: () -> Unit) {
    var nomePlayer by remember { mutableStateOf("") }
    var dificuldade by remember { mutableStateOf("Fácil") }
    val opcoes = listOf("Fácil", "Médio", "Difícil")

    // Estado para seleção de desafios criados pelo Admin
    var desafioSelecionado by remember { mutableStateOf("Aquecimento") }
    var mostrarDialogDesafios by remember { mutableStateOf(false) }
    //Temporário: lista de desafios disponíveis para escolha
    val desafiosDisponiveis = listOf("Aquecimento", "Arco-íris", "Maratona")

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            onClick = { /*TODO*/ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Adicionar jogador", color = Color.White)
        }

        Spacer(Modifier.height(24.dp))

        Text("Dificuldade", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)

        Column(Modifier.selectableGroup()) {
            opcoes.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = dificuldade == option,
                            onClick = { dificuldade = option },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = dificuldade == option, onClick = null)
                    Text(option, color = Color.White, modifier = Modifier.padding(start = 12.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Desafio do Jogo", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Text(
            text = "Desafio ativo: $desafioSelecionado",
            fontSize = 14.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { mostrarDialogDesafios = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Alternar Desafio", color = Color.White)
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar ao início", color = Color.White)
        }
    }

    // Diálogo para escolha do desafio
    if (mostrarDialogDesafios) {
        AlertDialog(
            onDismissRequest = { mostrarDialogDesafios = false },
            title = { Text("Selecionar Desafio") },
            text = {
                Column(Modifier.selectableGroup()) {
                    desafiosDisponiveis.forEach { desafio ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = desafioSelecionado == desafio,
                                    onClick = { desafioSelecionado = desafio },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = desafioSelecionado == desafio,
                                onClick = null
                            )
                            Text(
                                text = desafio,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogDesafios = false }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogDesafios = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
