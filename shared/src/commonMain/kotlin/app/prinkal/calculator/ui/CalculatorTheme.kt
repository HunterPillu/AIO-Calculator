package app.prinkal.calculator.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4D46B8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E6FF),
    onPrimaryContainer = Color(0xFF15105F),
    secondary = Color(0xFF596175),
    secondaryContainer = Color(0xFFE0E3F2),
    surface = Color(0xFFFAF8FF),
    surfaceVariant = Color(0xFFE5E1EC),
    background = Color(0xFFFAF8FF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC9C3FF),
    onPrimary = Color(0xFF28216D),
    primaryContainer = Color(0xFF39328A),
    onPrimaryContainer = Color(0xFFE8E6FF),
    secondary = Color(0xFFC2C6D9),
    secondaryContainer = Color(0xFF414657),
    surface = Color(0xFF121318),
    surfaceVariant = Color(0xFF45464F),
    background = Color(0xFF121318)
)

@Composable
fun CalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
