package com.poco.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IosColors.Blue,
    secondary = IosColors.Green,
    surface = IosColors.DarkSurface,
    background = Color.Black,
    onPrimary = Color.White,
    onSurface = IosColors.DarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = IosColors.Blue,
    secondary = IosColors.Green,
    surface = IosColors.LightSurface,
    background = Color(0xFFF2F2F7),
    onPrimary = Color.White,
    onSurface = IosColors.LightTextPrimary
)

@Composable
fun PocoLauncherTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
