package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

object ThemeManager {
    var isLightMode by mutableStateOf(false)
}

// Dynamic colors adapting automatically to active Theme Mode
val BgDark: Color get() = if (ThemeManager.isLightMode) Color(0xFFF5F7FA) else Color(0xFF0D0F12)
val BgCardDark: Color get() = if (ThemeManager.isLightMode) Color(0xFFFFFFFF) else Color(0xFF15191E)
val BorderDark: Color get() = if (ThemeManager.isLightMode) Color(0xFFE2E8F0) else Color(0xFF262E38)
val OnBgDark: Color get() = if (ThemeManager.isLightMode) Color(0xFF1E293B) else Color(0xFFF1F2F4)
val SubtitleDark: Color get() = if (ThemeManager.isLightMode) Color(0xFF64748B) else Color(0xFF9CA3AF)

// Custom Accent Primary Colors
val VioletPrimary = Color(0xFF6C5DD3)
val VioletGlowing = Color(0xFF8B7DFF)

val CoralPrimary = Color(0xFFFF6B6B)
val CoralGlowing = Color(0xFFFF8E8E)

val EmeraldPrimary = Color(0xFF00B894)
val EmeraldGlowing = Color(0xFF55EFC4)

val AzurePrimary = Color(0xFF0984E3)
val AzureGlowing = Color(0xFF74B9FF)
