package group12.budgetbuddi.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

/*
Author: Android Developers
Description: Used as reference for custom pointer input and tap gesture detection in Compose.
Reference: Android Developers. 2026. Pointer input in Compose. [Online]. Available at: https://developer.android.com/develop/ui/compose/touch-input/pointer-input [Accessed 29 April 2026].
*/

/*
Author: Android Developers
Description: Used as reference for drawing custom UI with Canvas and Brush in Jetpack Compose.
Reference: Android Developers. n.d. Graphics in Compose | Jetpack Compose. [Online]. Available at: https://developer.android.com/develop/ui/compose/graphics/draw/overview [Accessed 29 April 2026].
*/

/*
Author: Android Developers
Description: Used as reference for using state-based colour and size animations in Compose.
Reference: Android Developers. 2025. Value-based animations. [Online]. Available at: https://developer.android.com/develop/ui/compose/animation/value-based [Accessed 29 April 2026].
*/

private val BudgetGreen = Color(0xFF139A43)

data class BudgetIconOption(
    val key: String,
    val icon: ImageVector
)

// These keys are saved in the database so the icon can be loaded again later.
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

// Converts stored hex strings back into Compose colours.
fun colorFromHex(hex: String): Color {
    return runCatching {
        Color(hex.toColorInt())
    }.getOrElse { Color.Gray }
}

// Converts selected Compose colours into hex strings for storing in Room.
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
        modifier = Modifier.height(170.dp)
    ) {
        items(budgetEmojis) { emoji ->
            val isSelected = selectedEmoji == emoji

            // Selected items are slightly larger and more visible.
            val animatedSize by animateDpAsState(
                targetValue = if (isSelected) 46.dp else 40.dp,
                label = "emojiSizeAnimation"
            )

            val borderWidth by animateDpAsState(
                targetValue = if (isSelected) 3.dp else 1.dp,
                label = "emojiBorderAnimation"
            )

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) {
                    BudgetGreen.copy(alpha = 0.16f)
                } else {
                    Color.Transparent
                },
                label = "emojiBackgroundAnimation"
            )

            val borderColor by animateColorAsState(
                targetValue = if (isSelected) BudgetGreen else Color.LightGray,
                label = "emojiBorderColorAnimation"
            )

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(animatedSize)
                    .background(
                        color = backgroundColor,
                        shape = CircleShape
                    )
                    .border(
                        width = borderWidth,
                        color = borderColor,
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
        modifier = Modifier.height(190.dp)
    ) {
        items(budgetIcons) { option ->
            val isSelected = selectedIconKey == option.key

            val animatedSize by animateDpAsState(
                targetValue = if (isSelected) 54.dp else 48.dp,
                label = "iconSizeAnimation"
            )

            val innerIconSize by animateDpAsState(
                targetValue = if (isSelected) 31.dp else 26.dp,
                label = "innerIconSizeAnimation"
            )

            val borderWidth by animateDpAsState(
                targetValue = if (isSelected) 3.dp else 1.dp,
                label = "iconBorderAnimation"
            )

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) {
                    BudgetGreen.copy(alpha = 0.16f)
                } else {
                    Color.Transparent
                },
                label = "iconBackgroundAnimation"
            )

            val borderColor by animateColorAsState(
                targetValue = if (isSelected) BudgetGreen else Color.LightGray,
                label = "iconBorderColorAnimation"
            )

            val iconTint by animateColorAsState(
                targetValue = if (isSelected) BudgetGreen else Color.DarkGray,
                label = "iconTintAnimation"
            )

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(animatedSize)
                    .background(
                        color = backgroundColor,
                        shape = CircleShape
                    )
                    .border(
                        width = borderWidth,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .clickable { onIconSelected(option.key) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = option.key,
                    modifier = Modifier.size(innerIconSize),
                    tint = iconTint
                )
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
                        var hue = Math.toDegrees(
                            atan2(dy.toDouble(), dx.toDouble())
                        ).toFloat()

                        if (hue < 0) hue += 360f

                        val saturation = (distance / radius).coerceIn(0f, 1f)

                        // Android HSV is used to turn the tap position into a real colour.
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
            .border(
                width = 3.dp,
                color = selectedColor,
                shape = CircleShape
            )
    )
}