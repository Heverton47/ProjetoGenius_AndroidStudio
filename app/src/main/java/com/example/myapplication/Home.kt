package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.GeniusBackground
import com.example.myapplication.ui.theme.GeniusBlue
import com.example.myapplication.ui.theme.GeniusGreen
import com.example.myapplication.ui.theme.GeniusRed
import com.example.myapplication.ui.theme.GeniusYellow

/** Tela inicial: o usuário escolhe entre Modo Jogador e Modo Administrador. */
@Composable
fun Home(
    onPlayerMode: () -> Unit,
    onAdminMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GeniusBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Quatro quadradinhos coloridos como "logo"
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(GeniusRed, GeniusBlue, GeniusGreen, GeniusYellow).forEach { cor ->
                Spacer(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(cor)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "GENIUS",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Cores e Sequências",
            style = MaterialTheme.typography.titleMedium,
            color = Color.LightGray
        )

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onPlayerMode,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GeniusBlue)
        ) {
            Text("Modo Jogador", fontSize = 18.sp)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onAdminMode,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Modo Administrador", fontSize = 18.sp, color = Color.White)
        }
    }
}
