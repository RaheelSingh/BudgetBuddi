package group12.budgetbuddi.ui.screens

import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(navController: NavController) {
    // Temporary hardcoded dates for UI testing
    val startDate = remember { mutableStateOf(Date()) }
    val endDate = remember { mutableStateOf(Date()) }
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

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
            // Card with date filters
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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

                    // Start Date Button
                    OutlinedButton(
                        onClick = { /* Date picker will go here later */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start: ${dateFormat.format(startDate.value)}")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // End Date Button
                    OutlinedButton(
                        onClick = { /* Date picker will go here later */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("End: ${dateFormat.format(endDate.value)}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter Button
                    Button(
                        onClick = { /* Filter logic will go here later */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Filter")
                    }
                }
            }

            // Text to show screen is working
            Text(
                text = "Expense List Screen - UI Updated!",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}