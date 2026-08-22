package com.nexus.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NexusBackground = Color(0xFF0D0F16)
val NexusPanel = Color(0xFF151926)
val NexusPanel2 = Color(0xFF1B2032)
val NexusLine = Color(0xFF262C3F)
val NexusInk = Color(0xFFECEEF5)
val NexusInkDim = Color(0xFF8991A8)
val NexusViolet = Color(0xFF7C8CFF)
val NexusAmber = Color(0xFFE0973C)
val NexusGreen = Color(0xFF4FAE82)
val NexusDanger = Color(0xFFD1614A)

private val NexusColorScheme = darkColorScheme(
    primary = NexusViolet,
    secondary = NexusAmber,
    background = NexusBackground,
    surface = NexusPanel,
    onPrimary = NexusBackground,
    onBackground = NexusInk,
    onSurface = NexusInk,
    error = NexusDanger
)

@Composable
fun NexusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NexusColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
