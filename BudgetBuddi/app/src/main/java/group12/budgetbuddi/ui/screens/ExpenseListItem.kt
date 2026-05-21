//  Code Attributions


/* Stack Overflow (2010). Get Content URI from File Path in Android [Online].
* Available at: https://stackoverflow.com/questions/3004713/get-content-uri-from-file-path-in-android
* (Accessed: 29 April 2026)
*/



package group12.budgetbuddi.ui.screens

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.io.File
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseListItem(
    expense: ExpenseEntity,
    navController: NavController,
    expenseViewModel: ExpenseViewModel,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Format the date
    val displayDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = inputFormat.parse(expense.date)
        if (parsedDate != null) dateFormat.format(parsedDate) else expense.date
    } catch (e: Exception) {
        expense.date
    }

    // Check if photo exists
    val photoUri = expense.photoPath?.let { path ->
        if (path.startsWith("content://")) {
            try {
                Uri.parse(path)
            } catch (e: Exception) { null }
        } else {
            val file = File(path)
            if (file.exists()) Uri.fromFile(file) else null
        }
    }
    val hasPhoto = photoUri != null

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        if (hasPhoto && photoUri != null) {
                            try {
                                val encodedPath = URLEncoder.encode(expense.photoPath, "UTF-8")
                                navController.navigate("photo_viewer/$encodedPath")
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
                                "No photo attached",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (hasPhoto && photoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photoUri)
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
                modifier = Modifier
                    .weight(1f)
                    .clickable { onItemClick() },
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

            // Amount and Delete Button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "R${String.format("%.2f", expense.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Delete Button
                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete ${expense.category} - R${expense.amount}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseViewModel.deleteExpense(expense)
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}