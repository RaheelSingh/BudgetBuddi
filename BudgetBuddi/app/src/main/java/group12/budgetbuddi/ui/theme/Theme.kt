package group12.budgetbuddi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = BackgroundWhite,
    secondary = DarkGreen,
    onSecondary = BackgroundWhite,
    tertiary = DeepGreen,
    onTertiary = BackgroundWhite,
    background = BackgroundWhite,
    onBackground = TextBlack,
    surface = BackgroundWhite,
    onSurface = TextBlack,
    outline = LightBorder
)

@Composable
fun BudgetBuddiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}