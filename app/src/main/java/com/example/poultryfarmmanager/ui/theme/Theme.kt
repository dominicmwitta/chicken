package com.example.poultryfarmmanager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark palette
val NavyDeep     = Color(0xFF0D1117)
val NavySurface  = Color(0xFF161B22)
val NavyCard     = Color(0xFF21262D)
val NavyBorder   = Color(0xFF30363D)
val Emerald      = Color(0xFF3FB950)
val EmeraldDark  = Color(0xFF0D2B14)
val AmberGold    = Color(0xFFF0B429)
val AmberDark    = Color(0xFF2D1F00)
val CoralRed     = Color(0xFFF85149)
val CoralDark    = Color(0xFF2D0A09)
val TextPrimary  = Color(0xFFE6EDF3)
val TextMuted    = Color(0xFF8B949E)

// Light palette
val SlateDeep    = Color(0xFF0F172A)
val SlateMid     = Color(0xFF475569)
val GrayBg       = Color(0xFFF1F5F9)
val GreenPrimary = Color(0xFF16A34A)
val GreenCont    = Color(0xFFDCFCE7)
val GreenOnCont  = Color(0xFF14532D)
val AmberLight   = Color(0xFFD97706)
val AmberLightC  = Color(0xFFFEF3C7)
val RedErr       = Color(0xFFDC2626)
val RedErrC      = Color(0xFFFEE2E2)

private val DarkColorScheme = darkColorScheme(
    primary              = Emerald,
    onPrimary            = NavyDeep,
    primaryContainer     = EmeraldDark,
    onPrimaryContainer   = Emerald,
    secondary            = AmberGold,
    onSecondary          = NavyDeep,
    secondaryContainer   = AmberDark,
    onSecondaryContainer = AmberGold,
    background           = NavyDeep,
    onBackground         = TextPrimary,
    surface              = NavySurface,
    onSurface            = TextPrimary,
    surfaceVariant       = NavyCard,
    onSurfaceVariant     = TextMuted,
    outline              = NavyBorder,
    error                = CoralRed,
    onError              = NavyDeep,
    errorContainer       = CoralDark,
    onErrorContainer     = CoralRed
)

private val LightColorScheme = lightColorScheme(
    primary              = GreenPrimary,
    onPrimary            = Color.White,
    primaryContainer     = GreenCont,
    onPrimaryContainer   = GreenOnCont,
    secondary            = AmberLight,
    onSecondary          = Color.White,
    secondaryContainer   = AmberLightC,
    onSecondaryContainer = SlateDeep,
    background           = GrayBg,
    onBackground         = SlateDeep,
    surface              = Color.White,
    onSurface            = SlateDeep,
    surfaceVariant       = Color(0xFFF8FAFC),
    onSurfaceVariant     = SlateMid,
    outline              = Color(0xFFE2E8F0),
    error                = RedErr,
    onError              = Color.White,
    errorContainer       = RedErrC,
    onErrorContainer     = RedErr
)

@Composable
fun PoultryFarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
