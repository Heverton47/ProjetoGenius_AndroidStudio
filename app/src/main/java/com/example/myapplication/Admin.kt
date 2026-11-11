package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.myapplication.ui.theme.GeniusBackground
import com.example.myapplication.ui.theme.GeniusBlue
import com.example.myapplication.ui.theme.GeniusRed
import com.example.myapplication.ui.theme.GeniusSurface

// Modelo temporário só para a tela. Na Etapa 6 os desafios passam a vir do Firestore.
private data class Desafio(val id: Int, val nome: String, val sequencia: String)

/**
 * Painel do Administrador + gerenciamento de desafios (CRUD) numa tela só.
 *  C = botão "+" (abre o diálogo)   R = LazyColumn
 *  U = ícone de lápis               D = ícone de lixeira
 */
@Composable
fun Admin(onLogout: () -> Unit) {
    val Desafios = remember {
        mutableStateListOf(
            Desafio(1, "Aquecimento", "Vermelho, Azul, Verde"),
            Desafio(2, "Arco-íris", "Vermelho, Amarelo, Verde, Azul"),
            Desafio(3, "Maratona", "Azul, Azul, Verde, Amarelo, Vermelho")
        )
    }

    // Controle do diálogo: mostrarDialog liga/desliga; editandoId == null significa "novo desafio"
    var mostrarDialog by remember { mutableStateOf(false) }
    var editandoId by remember { mutableStateOf<Int?>(null) }
    var nameField by remember { mutableStateOf("") }
    var sequenceField by remember { mutableStateOf("") }

    Scaffold(
        containerColor = GeniusBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editandoId = null
                    nameField = ""
                    sequenceField = ""
                    mostrarDialog = true
                },
                containerColor = GeniusBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar desafio", tint = Color.White)
            }
        }
    ) { innerPadding ->
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

            Text(
                "Desafios cadastrados: ${Desafios.size}",
                color = Color.LightGray,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(Desafios, key = { _, item -> item.id }) { index, desafio ->
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
                                Text(desafio.nome, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text(desafio.sequencia, fontSize = 14.sp, color = Color.LightGray)
                            }
                            IconButton(onClick = {
                                editandoId = desafio.id
                                nameField = desafio.nome
                                sequenceField = desafio.sequencia
                                mostrarDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White)
                            }
                            IconButton(onClick = { Desafios.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = GeniusRed)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialog) {
        AlertDialog(
            onDismissRequest = { mostrarDialog = false },
            title = { Text(if (editandoId == null) "Novo desafio" else "Editar desafio") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameField,
                        onValueChange = { nameField = it },
                        label = { Text("Nome") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sequenceField,
                        onValueChange = { sequenceField = it },
                        label = { Text("Sequência (ex.: Vermelho, Azul)") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nameField.isNotBlank() && sequenceField.isNotBlank()) {
                            val id = editandoId
                            if (id == null) {
                                val newId = (Desafios.maxOfOrNull { it.id } ?: 0) + 1
                                Desafios.add(Desafio(newId, nameField.trim(), sequenceField.trim()))
                            } else {
                                val pos = Desafios.indexOfFirst { it.id == id }
                                if (pos >= 0) Desafios[pos] = Desafio(id, nameField.trim(), sequenceField.trim())
                            }
                            mostrarDialog = false
                        }
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
