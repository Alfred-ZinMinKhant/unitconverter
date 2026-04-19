package com.sideproject.unitconverter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Extended color scheme for the instrument theme
data class InstrumentColors(
    val bgPage: Color,
    val bgChrome: Color,
    val bgPanel: Color,
    val bgPanel2: Color,
    val bgInput: Color,
    val hair: Color,
    val hairSoft: Color,
    val ink100: Color,
    val ink90: Color,
    val ink70: Color,
    val ink50: Color,
    val ink30: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentHair: Color,
    val signal: Color,
    val signalSoft: Color,
    val warn: Color,
)

val DarkInstrumentColors = InstrumentColors(
    bgPage = DarkBgPage,
    bgChrome = DarkBgChrome,
    bgPanel = DarkBgPanel,
    bgPanel2 = DarkBgPanel2,
    bgInput = DarkBgInput,
    hair = DarkHair,
    hairSoft = DarkHairSoft,
    ink100 = DarkInk100,
    ink90 = DarkInk90,
    ink70 = DarkInk70,
    ink50 = DarkInk50,
    ink30 = DarkInk30,
    accent = Accent,
    accentSoft = AccentSoft,
    accentHair = AccentHair,
    signal = Signal,
    signalSoft = SignalSoft,
    warn = Warn,
)

val LightInstrumentColors = InstrumentColors(
    bgPage = LightBgPage,
    bgChrome = LightBgChrome,
    bgPanel = LightBgPanel,
    bgPanel2 = LightBgPanel2,
    bgInput = LightBgInput,
    hair = LightHair,
    hairSoft = LightHairSoft,
    ink100 = LightInk100,
    ink90 = LightInk90,
    ink70 = LightInk70,
    ink50 = LightInk50,
    ink30 = LightInk30,
    accent = Color(0xFFB08830),
    accentSoft = Color(0x26B08830),
    accentHair = Color(0x8CB08830),
    signal = Color(0xFF3AA060),
    signalSoft = Color(0x263AA060),
    warn = Color(0xFFD47A4A),
)

val LocalInstrumentColors = staticCompositionLocalOf { DarkInstrumentColors }

@Composable
fun UnitConverterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val instrumentColors = if (darkTheme) DarkInstrumentColors else LightInstrumentColors

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Accent,
            onPrimary = DarkBgDevice,
            primaryContainer = AccentSoft,
            secondary = Signal,
            background = DarkBgPage,
            surface = DarkBgPanel,
            surfaceVariant = DarkBgPanel2,
            onBackground = DarkInk100,
            onSurface = DarkInk100,
            onSurfaceVariant = DarkInk70,
            outline = DarkHair,
            outlineVariant = DarkHairSoft,
        )
    } else {
        lightColorScheme(
            primary = Color(0xFFB08830),
            onPrimary = Color.White,
            primaryContainer = Color(0x26B08830),
            secondary = Color(0xFF3AA060),
            background = LightBgPage,
            surface = LightBgPanel,
            surfaceVariant = LightBgPanel2,
            onBackground = LightInk100,
            onSurface = LightInk100,
            onSurfaceVariant = LightInk70,
            outline = LightHair,
            outlineVariant = LightHairSoft,
        )
    }

    CompositionLocalProvider(LocalInstrumentColors provides instrumentColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
