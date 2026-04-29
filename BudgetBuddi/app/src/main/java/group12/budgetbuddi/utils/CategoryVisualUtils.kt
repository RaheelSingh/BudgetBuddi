package group12.budgetbuddi.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt
import androidx.core.graphics.toColorInt

data class BudgetIconOption(
    val key: String,
    val icon: ImageVector
)

val budgetIcons = listOf(
    BudgetIconOption("restaurant", Icons.Default.Restaurant),
    BudgetIconOption("shopping", Icons.Default.ShoppingCart),
    BudgetIconOption("transport", Icons.Default.DirectionsBus),
    BudgetIconOption("home", Icons.Default.Home),
    BudgetIconOption("movie", Icons.Default.Movie),
    BudgetIconOption("school", Icons.Default.School),
    BudgetIconOption("health", Icons.Default.LocalHospital),
    BudgetIconOption("car", Icons.Default.DirectionsCar),
    BudgetIconOption("fuel", Icons.Default.LocalGasStation),
    BudgetIconOption("money", Icons.Default.AttachMoney),
    BudgetIconOption("card", Icons.Default.CreditCard),
    BudgetIconOption("receipt", Icons.Default.Receipt),
    BudgetIconOption("savings", Icons.Default.Savings),
    BudgetIconOption("gift", Icons.Default.CardGiftcard),
    BudgetIconOption("work", Icons.Default.Work)
)

val budgetEmojis = listOf(
    "🍳", "🥪", "🍽️", "🍪", "🍴", "🥤",
    "🚌", "🚇", "🚕", "🚆",
    "🛒", "🏠", "💧", "⚡",
    "🎬", "🎮", "🎵", "🎉",
    "🏫", "📚", "🏥", "💊",
    "⛽", "🅿️", "💰", "💳", "🧾"
)

fun getBudgetIcon(key: String): ImageVector {
    return budgetIcons.find { it.key == key }?.icon ?: Icons.Default.Category
}

fun colorFromHex(hex: String): Color {
    return runCatching {
        Color(hex.toColorInt())
    }.getOrElse { Color.Gray }
}

fun colorToHex(color: Color): String {
    return "#%06X".format(0xFFFFFF and color.toArgb())
}

@Composable
fun CategoryVisualPreview(
    visualType: String,
    visualValue: String,
    outlineColor: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .border(
                width = 3.dp,
                color = colorFromHex(outlineColor),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (visualType == "icon") {
            Icon(
                imageVector = getBudgetIcon(visualValue),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        } else {
            Text(text = visualValue.ifBlank { "?" })
        }
    }
}

@Composable
fun EmojiPicker(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier.height(160.dp)
    ) {
        items(budgetEmojis) { emoji ->
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(40.dp)
                    .border(
                        width = if (selectedEmoji == emoji) 2.dp else 1.dp,
                        color = if (selectedEmoji == emoji) Color.Black else Color.LightGray,
                        shape = CircleShape
                    )
                    .clickable { onEmojiSelected(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(emoji)
            }
        }
    }
}

@Composable
fun IconPicker(
    selectedIconKey: String,
    onIconSelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier.height(180.dp)
    ) {
        items(budgetIcons) { option ->
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(48.dp)
                    .border(
                        width = if (selectedIconKey == option.key) 2.dp else 1.dp,
                        color = if (selectedIconKey == option.key) Color.Black else Color.LightGray,
                        shape = CircleShape
                    )
                    .clickable { onIconSelected(option.key) },
                contentAlignment = Alignment.Center
            ) {
                Icon(option.icon, contentDescription = option.key)
            }
        }
    }
}

@Composable
fun SimpleColorWheel(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val wheelColors = listOf(
        Color.Red,
        Color.Yellow,
        Color.Green,
        Color.Cyan,
        Color.Blue,
        Color.Magenta,
        Color.Red
    )

    Canvas(
        modifier = Modifier
            .size(180.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    val radius = min(size.width, size.height) / 2f
                    val distance = sqrt(dx * dx + dy * dy)

                    if (distance <= radius) {
                        var hue = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        if (hue < 0) hue += 360f

                        val saturation = (distance / radius).coerceIn(0f, 1f)
                        val androidColor = AndroidColor.HSVToColor(
                            floatArrayOf(hue, saturation, 1f)
                        )

                        onColorSelected(Color(androidColor))
                    }
                }
            }
    ) {
        drawCircle(
            brush = Brush.sweepGradient(wheelColors)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Box(
        modifier = Modifier
            .size(48.dp)
            .border(3.dp, selectedColor, CircleShape)
    )
}