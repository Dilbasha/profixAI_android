package com.simats.profixai.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Teal-Blue theme colors
private val TealPrimary = Color(0xFF0D9997)
private val BluePrimary = Color(0xFF3B82F6)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealPrimary,
    onPrimaryContainer = Color.White,
    secondary = Amber500,
    onSecondary = Color.Black,
    secondaryContainer = Amber600,
    onSecondaryContainer = Color.Black,
    tertiary = Green500,
    onTertiary = Color.White,
    background = Gray50,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Gray400,
    error = Red500,
    onError = Color.White
)

@Composable
fun ServiceConnectTheme(
    darkTheme: Boolean = false, // Force light theme
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Always use light theme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TealPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

