package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OrientGold,
    onPrimary = OrientGoldDark,
    secondary = OrientGoldLight,
    onSecondary = OrientGoldDark,
    background = OrientDarkBg,
    onBackground = OrientTextPrimary,
    surface = OrientSurface,
    onSurface = OrientTextPrimary,
    surfaceVariant = OrientSurfaceVariant,
    onSurfaceVariant = OrientTextSecondary
)

private val LightColorScheme = darkColorScheme(
    primary = OrientGold,
    onPrimary = OrientGoldDark,
    secondary = OrientGoldLight,
    onSecondary = OrientGoldDark,
    background = OrientDarkBg,
    onBackground = OrientTextPrimary,
    surface = OrientSurface,
    onSurface = OrientTextPrimary,
    surfaceVariant = OrientSurfaceVariant,
    onSurfaceVariant = OrientTextSecondary
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to maintain our highly branded, customized design
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
