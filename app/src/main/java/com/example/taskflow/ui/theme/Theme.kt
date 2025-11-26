package com.example.taskflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PinkPrimary,
    secondary = DeepBlue,
    tertiary = LavenderSecondary
)

private val DarkColors = darkColorScheme(
    primary = PinkPrimary,
    secondary = DeepBlue,
    tertiary = LavenderSecondary
)

@Composable
fun TaskFlowTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
