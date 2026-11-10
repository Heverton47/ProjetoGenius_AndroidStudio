package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema de cores do app, montado a partir das cores que já estão no Color.kt.
// O Genius usa sempre o tema escuro (combina com as cores vivas dos botões).
private val GeniusColorScheme = darkColorScheme(
    primary = GeniusBlue,
    secondary = GeniusGreen,
    tertiary = GeniusYellow,
    background = GeniusBackground,
    surface = GeniusSurface,
    error = GeniusRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GeniusColorScheme,
        typography = Typography, // vem do Type.kt gerado pelo Android Studio
        content = content
    )
}