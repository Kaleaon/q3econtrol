package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Kaleaon's Ktheme Design System Engine
 * Supports custom structural styling engines:
 * - Terminal & Neon (OLED dark, cyber chips)
 * - Sweep / LCARS (Iconic LCARS sweep arch, capsule pill buttons, split frame bars, serial blocks)
 * - Metro (Strictly flat 0.dp live tiles, stark bold typography, vibrant accent color blocks)
 * - Lumiya Classic Dark (Graphite slate)
 * - Frutiger Aero (Glossy sky blue)
 */
enum class KthemePack(val displayName: String, val description: String) {
    TERMINAL_NEON("Terminal & Neon", "OLED pitch black with electric cyan and cyber mint. Ultra battery saver."),
    SWEEP_LCARS("Sweep / LCARS", "True LCARS sweep arch, pill capsule buttons, split frame bars, and serial blocks."),
    METRO("Metro", "Sharp flat 0.dp live tiles, stark typography, vibrant accent color blocks, zero curves."),
    LUMIYA_DARK("Lumiya Classic Dark", "Deep graphite slate with warm amber accents. Classic high-contrast readability."),
    FRUTIGER_AERO("Frutiger Aero", "Glossy translucent sky blues and airy daylight surfaces.")
}

@Immutable
data class KthemeColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val border: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val isDark: Boolean = true
)

@Immutable
data class KthemeStyle(
    val cardShape: Shape,
    val buttonShape: Shape,
    val badgeShape: Shape,
    val borderWidth: Dp = 1.dp,
    val isLcars: Boolean = false,
    val isMetro: Boolean = false
)

// 1. Terminal & Neon - OLED Black with Cyan & Green
val TerminalNeonPalette = KthemeColors(
    background = Color(0xFF05080F),
    surface = Color(0xFF0D121F),
    surfaceVariant = Color(0xFF161F33),
    primary = Color(0xFF00F0FF),
    secondary = Color(0xFF00FF9D),
    tertiary = Color(0xFF9D4EDD),
    onBackground = Color(0xFFF0F6FC),
    onSurface = Color(0xFFF0F6FC),
    onSurfaceVariant = Color(0xFF8B949E),
    border = Color(0xFF1F293D),
    success = Color(0xFF00FF9D),
    warning = Color(0xFFFFB800),
    danger = Color(0xFFFF3366),
    isDark = true
)
val TerminalNeonStyle = KthemeStyle(
    cardShape = RoundedCornerShape(8.dp),
    buttonShape = RoundedCornerShape(6.dp),
    badgeShape = RoundedCornerShape(12.dp),
    borderWidth = 1.dp,
    isLcars = false,
    isMetro = false
)

// 2. Sweep / LCARS - Full LCARS Styling Engine
val SweepLcarsPalette = KthemeColors(
    background = Color(0xFF000000), // Pure Starfleet Pitch Black
    surface = Color(0xFF0A0A0A),
    surfaceVariant = Color(0xFF161218),
    primary = Color(0xFFFF9900), // LCARS Okuda Amber / Butterscotch
    secondary = Color(0xFFFF7700), // LCARS Peach Orange
    tertiary = Color(0xFFCC99CC), // LCARS Lilac
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFE4B5FF), // Pale Mauve Readout
    border = Color(0xFF442255), // LCARS Frame Border
    success = Color(0xFF99CCFF), // LCARS Pale Blue
    warning = Color(0xFFFFCC00), // LCARS Yellow
    danger = Color(0xFFDD2233), // LCARS Red Alert
    isDark = true
)
// Authentic LCARS asymmetrical sweep arch cards and capsule pill buttons
val SweepLcarsStyle = KthemeStyle(
    cardShape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = 4.dp, bottomEnd = 4.dp),
    buttonShape = RoundedCornerShape(percent = 50), // Iconic LCARS capsule button
    badgeShape = RoundedCornerShape(percent = 50),
    borderWidth = 1.5.dp,
    isLcars = true,
    isMetro = false
)

// 3. Metro - Full Microsoft Metro / Zune Styling Engine (Strictly 0.dp Sharp Tiles)
val MetroPalette = KthemeColors(
    background = Color(0xFF000000), // Pure Metro Stark Black
    surface = Color(0xFF111111),
    surfaceVariant = Color(0xFF1E1E1E),
    primary = Color(0xFF00A4EF), // Metro Bright Cyan
    secondary = Color(0xFF0050EF), // Metro Cobalt
    tertiary = Color(0xFF008A00), // Metro Emerald
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFAAAAAA),
    border = Color(0xFF2A2A2A),
    success = Color(0xFF008A00),
    warning = Color(0xFFF0A30A), // Metro Amber
    danger = Color(0xFFE51400), // Metro Crimson
    isDark = true
)
val MetroStyle = KthemeStyle(
    cardShape = RectangleShape, // Strictly 0.dp flat sharp tiles
    buttonShape = RectangleShape, // Strictly 0.dp sharp tile buttons
    badgeShape = RectangleShape,
    borderWidth = 1.5.dp,
    isLcars = false,
    isMetro = true
)

// 4. Lumiya Classic Dark - Deep graphite slate
val LumiyaDarkPalette = KthemeColors(
    background = Color(0xFF0D1117),
    surface = Color(0xFF161B22),
    surfaceVariant = Color(0xFF21262D),
    primary = Color(0xFFF59E0B), // Lumiya Amber Gold
    secondary = Color(0xFF10B981), // Emerald
    tertiary = Color(0xFF6366F1), // Indigo
    onBackground = Color(0xFFF0F6FC),
    onSurface = Color(0xFFF0F6FC),
    onSurfaceVariant = Color(0xFF9CA3AF),
    border = Color(0xFF30363D),
    success = Color(0xFF10B981),
    warning = Color(0xFFF59E0B),
    danger = Color(0xFFEF4444),
    isDark = true
)
val LumiyaDarkStyle = KthemeStyle(
    cardShape = RoundedCornerShape(10.dp),
    buttonShape = RoundedCornerShape(8.dp),
    badgeShape = RoundedCornerShape(12.dp),
    borderWidth = 1.dp,
    isLcars = false,
    isMetro = false
)

// 5. Frutiger Aero / Daylight - Clean daylight with sky blue
val FrutigerAeroPalette = KthemeColors(
    background = Color(0xFFF0F4F8),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE2E8F0),
    primary = Color(0xFF0284C7),
    secondary = Color(0xFF0D9488),
    tertiary = Color(0xFF2563EB),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    border = Color(0xFFCBD5E1),
    success = Color(0xFF16A34A),
    warning = Color(0xFFD97706),
    danger = Color(0xFFDC2626),
    isDark = false
)
val FrutigerAeroStyle = KthemeStyle(
    cardShape = RoundedCornerShape(14.dp),
    buttonShape = RoundedCornerShape(10.dp),
    badgeShape = RoundedCornerShape(16.dp),
    borderWidth = 1.dp,
    isLcars = false,
    isMetro = false
)

val LocalKtheme = staticCompositionLocalOf { TerminalNeonPalette }
val LocalKthemeStyle = staticCompositionLocalOf { TerminalNeonStyle }

fun KthemeColors.toMaterialColorScheme(): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = Color.Black,
            secondary = secondary,
            onSecondary = Color.Black,
            tertiary = tertiary,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = border
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            secondary = secondary,
            onSecondary = Color.White,
            tertiary = tertiary,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = border
        )
    }
}

@Composable
fun Q3eKthemeProvider(
    activePack: KthemePack,
    content: @Composable () -> Unit
) {
    val (colors, style) = when (activePack) {
        KthemePack.TERMINAL_NEON -> Pair(TerminalNeonPalette, TerminalNeonStyle)
        KthemePack.SWEEP_LCARS -> Pair(SweepLcarsPalette, SweepLcarsStyle)
        KthemePack.METRO -> Pair(MetroPalette, MetroStyle)
        KthemePack.LUMIYA_DARK -> Pair(LumiyaDarkPalette, LumiyaDarkStyle)
        KthemePack.FRUTIGER_AERO -> Pair(FrutigerAeroPalette, FrutigerAeroStyle)
    }

    CompositionLocalProvider(
        LocalKtheme provides colors,
        LocalKthemeStyle provides style
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography = Typography,
            content = content
        )
    }
}
