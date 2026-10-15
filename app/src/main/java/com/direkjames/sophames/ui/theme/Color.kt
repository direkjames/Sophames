package com.direkjames.sophames.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Sophames brand palette — light-first, rosé pink with a champagne accent. */
object Brand {
    val Rose = Color(0xFFD6567F)
    val RoseLight = Color(0xFFE8799F)
    val RoseDeep = Color(0xFFB93D68)
    val Blush = Color(0xFFF7D6E0)
    val Champagne = Color(0xFFC9A074)
    val ChampagneDeep = Color(0xFF9A7447)
    val Porcelain = Color(0xFFFFF9F6)
    val Ink = Color(0xFF2B1D24)
}

/** Extra tokens Material's ColorScheme has no slot for. */
@Immutable
data class SophamesExtras(
    val hairline: Color,
    val heroBrush: Brush,
    /** Fill for buttons and highlights; deep enough for white text on top. */
    val roseBrush: Brush,
    /** Gradient used on the big serif numerals. */
    val numberBrush: Brush,
    val labelColor: Color,
    val shadowTint: Color,
    val softFill: Color,
    val isDark: Boolean,
)

val LightExtras = SophamesExtras(
    hairline = Color(0xFFF0E2E7),
    heroBrush = Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFCE9EF), Color(0xFFF9DCE6))),
    roseBrush = Brush.linearGradient(listOf(Color(0xFFE0668F), Brand.RoseDeep)),
    numberBrush = Brush.linearGradient(listOf(Brand.RoseDeep, Brand.RoseLight)),
    labelColor = Brand.ChampagneDeep,
    shadowTint = Brand.Rose,
    softFill = Color(0xFFFCEEF2),
    isDark = false,
)

val DarkExtras = SophamesExtras(
    hairline = Color(0xFF3A2C34),
    heroBrush = Brush.linearGradient(listOf(Color(0xFF281D24), Color(0xFF2F1F28), Color(0xFF3A2230))),
    roseBrush = Brush.linearGradient(listOf(Color(0xFFE0668F), Brand.RoseDeep)),
    numberBrush = Brush.linearGradient(listOf(Color(0xFFF7B3C9), Color(0xFFF08FB0))),
    labelColor = Brand.Champagne,
    shadowTint = Color.Black,
    softFill = Color(0xFF281D24),
    isDark = true,
)

val LocalExtras = staticCompositionLocalOf { LightExtras }

val LightScheme = lightColorScheme(
    primary = Brand.Rose,
    onPrimary = Color.White,
    primaryContainer = Brand.Blush,
    onPrimaryContainer = Color(0xFF5A1730),
    secondary = Brand.ChampagneDeep,
    onSecondary = Color.White,
    background = Brand.Porcelain,
    onBackground = Brand.Ink,
    surface = Color.White,
    onSurface = Brand.Ink,
    surfaceVariant = Color(0xFFFBEFF2),
    onSurfaceVariant = Color(0xFF7C6A72),
    outline = Color(0xFFE9D9DE),
    outlineVariant = Color(0xFFF0E2E7),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFFBFA),
    surfaceContainer = Color(0xFFFEF5F6),
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color(0xFFFBEFF2),
)

val DarkScheme = darkColorScheme(
    primary = Color(0xFFF08FB0),
    onPrimary = Color(0xFF3A0F20),
    primaryContainer = Color(0xFF4A2231),
    onPrimaryContainer = Brand.Blush,
    secondary = Brand.Champagne,
    onSecondary = Color(0xFF2B1D10),
    background = Color(0xFF130E12),
    onBackground = Color(0xFFF8EEF1),
    surface = Color(0xFF1D151A),
    onSurface = Color(0xFFF8EEF1),
    surfaceVariant = Color(0xFF281D24),
    onSurfaceVariant = Color(0xFFB7A3AC),
    outline = Color(0xFF4A3A43),
    outlineVariant = Color(0xFF3A2C34),
    surfaceContainerLowest = Color(0xFF130E12),
    surfaceContainerLow = Color(0xFF1A1318),
    surfaceContainer = Color(0xFF1D151A),
    surfaceContainerHigh = Color(0xFF281D24),
    surfaceContainerHighest = Color(0xFF32252D),
)
