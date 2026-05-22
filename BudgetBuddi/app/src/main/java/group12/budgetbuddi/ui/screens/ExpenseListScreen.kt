// Code Attributions

/* Stack Overflow (2023). How to Use Null as Default Value in Compose MutableState [Online].
* Available at: https://stackoverflow.com/questions/75425433/how-to-use-null-as-default-value-compose-mutablestate
* (Accessed: 29 April 2026)
*
* Stack Overflow (2024). Jetpack Compose Button Alignment [Online].
* Available at: https://stackoverflow.com/questions/79085592/jetpack-compose-button-alignment
* (Accessed: 29 April 2026)
*
* Android Developers (2024). Lists and Grids in Jetpack Compose [Online].
* Available at: https://developer.android.com/develop/ui/compose/lists
* (Accessed: 29 April 2026)
*/


package group12.budgetbuddi.ui.screens

import android.widget.Toast
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import group12.budgetbuddi.ui.screens.DatePickerDialog
import group12.budgetbuddi.ui.screens.ExpenseListItem
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import group12.budgetbuddi.ui.theme.BrandGreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    navController: NavController,
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val context = LocalContext.current
    val allExpenses by expenseViewModel.allExpenses.collectAsState()

    // Filter to show only expenses (not income)
    val expensesOnly = remember(allExpenses) {
        allExpenses.filter { it.type == "expense" }
    }

    // Date filter state
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Filtered expenses based on date range
    val filteredExpenses = remember(expensesOnly, startDate, endDate) {
        var filtered = expensesOnly

        startDate?.let { start ->
            filtered = filtered.filter { expense ->
                try {
                    val expenseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expense.date)
                    expenseDate != null && expenseDate >= start
                } catch (e: Exception) { false }
            }
        }

        endDate?.let { end ->
            filtered = filtered.filter { expense ->
                try {
                    val expenseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expense.date)
                    expenseDate != null && expenseDate <= end
                } catch (e: Exception) { false }
            }
        }

        filtered
    }

    val dateDisplayFormat = remember { SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense List") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

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

                    // Start Date Button
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start: ${startDate?.let { dateDisplayFormat.format(it) } ?: "Select date"}")
                    }

                    Spacer(modifier = Modifier.height(8.dp))


                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("End: ${endDate?.let { dateDisplayFormat.format(it) } ?: "Select date"}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter Button
                    Button(
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                "Filter applied",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Filter")
                    }


                    if (startDate != null || endDate != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                startDate = null
                                endDate = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Filter")
                        }
                    }
                }
            }


            Text(
                text = "Recent Expenses (${filteredExpenses.size})",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )


            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expenses found for selected date range")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredExpenses) { expense ->
                        ExpenseListItem(
                            expense = expense,
                            navController = navController,
                            expenseViewModel = expenseViewModel,  // ← ADDED THIS
                            onItemClick = {
                                Toast.makeText(
                                    context,
                                    "Details: ${expense.category} - R${expense.amount}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onDeleteClick = {  // ← ADDED THIS
                                Toast.makeText(
                                    context,
                                    "Expense deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }

 // Date Picker Dialog
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

        if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}