package com.example.lifedots.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5BA0E9),
    secondary = Color(0xFF4A90D9),
    tertiary = Color(0xFF6AB0F9),
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2A2A2A),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2C2C2C),
    secondary = Color(0xFF4A90D9),
    tertiary = Color(0xFF5BA0E9),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF2C2C2C),
    onSurface = Color(0xFF2C2C2C)
)

@Composable
fun LifeDotsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    liquidGlass: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        liquidGlass -> LiquidGlassColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(LocalLiquidGlass provides liquidGlass) { content() }
        }
    )
}

val LocalLiquidGlass = androidx.compose.runtime.staticCompositionLocalOf { false }

private val LiquidGlassColorScheme = darkColorScheme(
    primary = Color(0xFF83F4DC), onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF214B59), onPrimaryContainer = Color(0xFFD9FFF6),
    secondary = Color(0xFFB6C9FF), tertiary = Color(0xFFE4BEFF),
    background = Color(0xFF0B182C), onBackground = Color(0xFFF1F7FF),
    surface = Color(0xCC20344C), onSurface = Color(0xFFF1F7FF),
    surfaceVariant = Color(0xFF30465E), onSurfaceVariant = Color(0xFFCADAEA),
    outline = Color(0xFF91AABD)
)

@Composable
fun androidx.compose.ui.Modifier.lifeDotsBackground(): androidx.compose.ui.Modifier =
    if (LocalLiquidGlass.current) {
        this.then(androidx.compose.ui.Modifier.background(
            androidx.compose.ui.graphics.Brush.linearGradient(
                listOf(Color(0xFF102D41), Color(0xFF24243E), Color(0xFF0B182C))
            )
        ))
    } else this.then(androidx.compose.ui.Modifier.background(MaterialTheme.colorScheme.background))
