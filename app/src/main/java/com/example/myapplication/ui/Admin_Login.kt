package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.GeniusBackground
import com.example.myapplication.ui.theme.GeniusRed
import com.example.myapplication.viewmodel.AdminUiState

/**
 * Login simulado do administrador.
 * A tela só coleta usuário e senha; quem valida é o AdminViewModel (credenciais de teste: admin / 1234).
 * Quando o UiState passa a ter logado = true, a tela avisa a navegação para abrir o painel.
 */
@Composable
fun AdminLogin(
    ui: AdminUiState,
    onEntrar: (String, String) -> Unit,
    onLimparErro: () -> Unit,
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var user by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    LaunchedEffect(ui.logado) {
        if (ui.logado) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GeniusBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Área do Administrador", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = user,
            onValueChange = { user = it; onLimparErro() },
            label = { Text("Usuário") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it; onLimparErro() },
            label = { Text("Senha") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        ui.erroLogin?.let { erro ->
            Text(
                text = erro,
                color = GeniusRed,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onEntrar(user, senha) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Entrar", fontSize = 18.sp)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar", color = Color.White)
        }
    }
}
