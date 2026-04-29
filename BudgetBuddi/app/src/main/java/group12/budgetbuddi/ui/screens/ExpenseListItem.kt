package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import group12.budgetbuddi.data.entity.ExpenseEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseListItem(
    expense: ExpenseEntity,
    navController: NavController,
    onItemClick: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Format the date
    val displayDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = inputFormat.parse(expense.date)
        if (parsedDate != null) dateFormat.format(parsedDate) else expense.date
    } catch (e: Exception) {
        expense.date
    }

    // Debug: Show photo path in logcat
    println("DEBUG - Expense: ${expense.category}, Photo path: ${expense.photoPath}")

    // Check if photo exists
    val hasPhoto = !expense.photoPath.isNullOrBlank() && File(expense.photoPath).exists()
    println("DEBUG - Has photo: $hasPhoto")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Photo Thumbnail
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable {
                        // Show toast immediately to confirm click works
                        android.widget.Toast.makeText(
                            context,
                            "Photo clicked! Has photo: $hasPhoto",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        if (hasPhoto) {
                            try {
                                navController.navigate("photo_viewer/${expense.photoPath}")
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Error: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "No photo attached for ${expense.category}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (hasPhoto) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(expense.photoPath))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Expense photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "No photo",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Expense Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                if (expense.subCategory.isNotBlank()) {
                    Text(
                        text = expense.subCategory,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = expense.paymentMethod,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Amount
            Text(
                text = "R${String.format("%.2f", expense.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}