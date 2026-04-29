package group12.budgetbuddi.ui.theme

import android.app.Activity
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
import androidx.compose.material3.Typography
// Your brand colors
val BrandGreen = Color(0xFF0DAB76)
val BrandGreenDark = Color(0xFF139A43)
val BrandGreenDeep = Color(0xFF0B5D1E)
val BrandBlack = Color(0xFF000000)



// Custom Light Color Scheme with your brand colors
private val LightColorScheme = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = BrandGreen.copy(alpha = 0.1f),
    onPrimaryContainer = BrandGreenDark,
    secondary = BrandGreenDark,
    onSecondary = Color.White,
    secondaryContainer = BrandGreen.copy(alpha = 0.05f),
    tertiary = BrandGreenDeep,
    onTertiary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF0F0F0),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

// Custom Dark Color Scheme with your brand colors
private val DarkColorScheme = darkColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = BrandGreen.copy(alpha = 0.2f),
    onPrimaryContainer = BrandGreen,
    secondary = BrandGreenDark,
    onSecondary = Color.White,
    secondaryContainer = BrandGreenDeep.copy(alpha = 0.3f),
    tertiary = BrandGreenDeep,
    onTertiary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2D2D2D),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun BudgetBuddiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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