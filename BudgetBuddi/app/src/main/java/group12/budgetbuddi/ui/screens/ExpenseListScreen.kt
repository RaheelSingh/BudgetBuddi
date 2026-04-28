package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

// Sample data class for testing
data class SampleExpense(
    val date: String,
    val description: String,
    val category: String,
    val amount: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(navController: NavController) {
    val startDate = remember { mutableStateOf(Date()) }
    val endDate = remember { mutableStateOf(Date()) }
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    // Sample expenses using your app's real categories
    val sampleExpenses = listOf(
        SampleExpense("01/15/2024", "Lunch at McDonald's", "restaurant", "$12.50"),
        SampleExpense("01/14/2024", "Movie night", "movie", "$25.00"),
        SampleExpense("01/13/2024", "Gas for car", "fuel", "$45.00"),
        SampleExpense("01/12/2024", "Grocery shopping", "shopping", "$85.30"),
        SampleExpense("01/11/2024", "Monthly rent", "home", "$1200.00"),
        SampleExpense("01/10/2024", "Doctor visit", "health", "$65.00"),
        SampleExpense("01/09/2024", "Bus ticket", "transport", "$3.50"),
        SampleExpense("01/08/2024", "School supplies", "school", "$35.00"),
        SampleExpense("01/07/2024", "Birthday gift", "gift", "$50.00"),
        SampleExpense("01/06/2024", "Coffee", "restaurant", "$4.50")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense List") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date Filter Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filter by Date Range",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            android.widget.Toast.makeText(
                                navController.context,
                                "Date picker coming soon",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start: ${dateFormat.format(startDate.value)}")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            android.widget.Toast.makeText(
                                navController.context,
                                "Date picker coming soon",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("End: ${dateFormat.format(endDate.value)}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            android.widget.Toast.makeText(
                                navController.context,
                                "Filter: ${dateFormat.format(startDate.value)} to ${dateFormat.format(endDate.value)}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Filter")
                    }
                }
            }

            // Title
            Text(
                text = "Recent Expenses",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // RecyclerView (LazyColumn) - Expense List with Photo Thumbnails
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleExpenses) { expense ->
                    // Each expense row with photo thumbnail
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                android.widget.Toast.makeText(
                                    navController.context,
                                    "Clicked: ${expense.description}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // PHOTO THUMBNAIL (Placeholder for now)
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable {
                                        android.widget.Toast.makeText(
                                            navController.context,
                                            "Photo clicked for: ${expense.description}",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("📷", style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }

                            // Expense Details
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = expense.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Text(
                                    text = expense.date,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                // Category name
                                Text(
                                    text = expense.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Amount
                            Text(
                                text = expense.amount,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}