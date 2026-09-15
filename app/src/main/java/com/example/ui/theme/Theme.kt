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

private val DarkColorScheme =
  darkColorScheme(
    primary = BlueInk,
    secondary = RedInk,
    tertiary = AmberStamp,
    background = Color(0xFF1E1E24),
    surface = Color(0xFF282830),
    surfaceVariant = Color(0xFF33333E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF0EBE1),
    onSurface = Color(0xFFF0EBE1),
    outline = Color(0xFF4A4A58),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BlueInk,
    secondary = RedInk,
    tertiary = AmberStamp,
    background = PaperBackground,
    surface = PaperSurface,
    surfaceVariant = PaperCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = InkDark,
    onSurface = InkDark,
    outline = PaperBorder,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Default to classic authentic pen & paper daytime aesthetic
  dynamicColor: Boolean = false, // Keep consistent paper aesthetic
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
