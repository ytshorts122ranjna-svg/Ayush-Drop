package com.example.ui.theme

import android.os.Build
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
    primary = AyushBlueLight,
    onPrimary = Color(0xFF00297A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = AyushVioletLight,
    onSecondary = Color(0xFF2E1065),
    secondaryContainer = Color(0xFF4C1D95),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = AyushCyanLight,
    onTertiary = Color(0xFF00363D),
    tertiaryContainer = Color(0xFF0E7490),
    onTertiaryContainer = Color(0xFFCFFAFE),
    background = AyushDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = AyushDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = AyushDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = AyushDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AyushBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = AyushVioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary = AyushCyanTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFFAFE),
    onTertiaryContainer = Color(0xFF0E7490),
    background = AyushLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = AyushLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = AyushLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun AyushDropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default false to showcase branded Blue/Violet aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
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
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = AyushDropTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
