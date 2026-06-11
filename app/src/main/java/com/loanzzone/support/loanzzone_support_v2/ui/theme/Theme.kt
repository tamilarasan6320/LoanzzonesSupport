package com.loanzzone.support.loanzzone_support_v2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

private val LightColorScheme = lightColorScheme(
    primary = LzBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = LzGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8F3DB),
    onSecondaryContainer = Color(0xFF00210A),
    tertiary = LzOrange,
    onTertiary = Color.White,
    background = LzScreenBg,
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE6EEF9),
    onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFF72787F),
    outlineVariant = Color(0xFFC2C7CF),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

@Composable
fun LoanzzonesSupportTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
