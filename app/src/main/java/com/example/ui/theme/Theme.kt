package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimary,
    secondary = VioletGlowing,
    background = BgDark,
    surface = BgCardDark,
    onPrimary = Color.White,
    onBackground = OnBgDark,
    onSurface = OnBgDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (ThemeManager.isLightMode) {
        lightColorScheme(
            primary = VioletPrimary,
            secondary = VioletGlowing,
            background = BgDark,
            surface = BgCardDark,
            onPrimary = Color.White,
            onBackground = OnBgDark,
            onSurface = OnBgDark
        )
    } else {
        darkColorScheme(
            primary = VioletPrimary,
            secondary = VioletGlowing,
            background = BgDark,
            surface = BgCardDark,
            onPrimary = Color.White,
            onBackground = OnBgDark,
            onSurface = OnBgDark
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
