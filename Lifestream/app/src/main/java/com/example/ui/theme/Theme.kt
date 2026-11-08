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
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    secondary = SlateLight,
    tertiary = CrimsonSecondary,
    background = MedicalLightBg,
    surface = MedicalWhite,
    onPrimary = MedicalWhite,
    onSecondary = MedicalLightBg,
    onTertiary = MedicalWhite,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonPrimary,
    secondary = SlateGrey,
    tertiary = SlateLight,
    background = MedicalLightBg,
    surface = MedicalWhite,
    onPrimary = MedicalWhite,
    onSecondary = MedicalWhite,
    onTertiary = DarkSurface,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun LifeStreamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep standard dynamic coloring disable option to pin our professional Medical color style strictly as requested!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
