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
    primary = OceanBlueLight,
    onPrimary = OceanBlueDark,
    primaryContainer = OceanBluePrimary,
    onPrimaryContainer = Color.White,
    secondary = ToskaLight,
    onSecondary = ToskaDark,
    secondaryContainer = ToskaSecondary,
    onSecondaryContainer = Color.White,
    tertiary = CalmingNavyContainer,
    background = Color(0xFF111827),
    surface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFF374151),
    onBackground = Color(0xFFF9FAFB),
    onSurface = Color(0xFFF9FAFB),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = OceanBluePrimary,
    onPrimary = Color.White,
    primaryContainer = OceanBlueContainer,
    onPrimaryContainer = OnOceanBlueContainer,
    secondary = ToskaSecondary,
    onSecondary = Color.White,
    secondaryContainer = ToskaContainer,
    onSecondaryContainer = OnToskaContainer,
    tertiary = CalmingNavy,
    onTertiary = Color.White,
    tertiaryContainer = CalmingNavyContainer,
    background = MedicalCanvasBg,
    onBackground = TextPrimary,
    surface = MedicalCardSurface,
    onSurface = TextPrimary,
    surfaceVariant = MedicalSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = MedicalOutline,
    outlineVariant = MedicalOutlineVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep true for modern adaptive feel while using custom theme as default
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

