package com.example.myapplication

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Botão colorido do jogo.
 *
 * Ele NÃO decide nada sozinho:
 *  - isLit: quem manda "acender" é a lógica do jogo (estado vem de fora)
 *  - enabled: bloqueia o toque enquanto a sequência está sendo mostrada
 *  - onClick: apenas avisa que foi tocado (o que fazer com isso é problema de quem chamou)
 */
@Composable
fun GeniusButton(
    normalColor: Color,
    litColor: Color,
    isLit: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Transição suave entre a cor normal e a cor "acesa".
    val color by animateColorAsState(
        targetValue = if (isLit) litColor else normalColor,
        animationSpec = tween(durationMillis = 120),
        label = "geniusButtonColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .clickable(enabled = enabled) { onClick() }
    )
}
