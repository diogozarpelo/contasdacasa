package com.diogo.contasdacasa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = HouseGreen,
    onPrimary = Color.White,
    primaryContainer = HouseGreenLight,
    onPrimaryContainer = HouseGreenDark,
    secondary = HouseGreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E9E1),
    onSecondaryContainer = HouseGreenDark,
    background = HouseCream,
    onBackground = HouseText,
    surface = HouseSurface,
    onSurface = HouseText,
    surfaceVariant = Color(0xFFE8EFEA),
    onSurfaceVariant = HouseTextSecondary,
    outline = HouseOutline,
    error = HouseError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = HouseGreenDarkTheme,
    onPrimary = Color(0xFF00382B),
    primaryContainer = HouseGreenContainerDark,
    onPrimaryContainer = Color(0xFFB9F1DB),
    secondary = Color(0xFFB7CCBF),
    onSecondary = Color(0xFF23352D),
    secondaryContainer = Color(0xFF394C43),
    onSecondaryContainer = Color(0xFFD3E8DA),
    background = HouseBackgroundDark,
    onBackground = HouseTextDark,
    surface = HouseSurfaceDark,
    onSurface = HouseTextDark,
    surfaceVariant = Color(0xFF3F4944),
    onSurfaceVariant = HouseTextSecondaryDark,
    outline = HouseOutlineDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun ContasDaCasaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}