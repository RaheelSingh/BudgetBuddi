package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.data.entity.ExpenseEntity
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySummaryScreen(
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

    // Category selection state
    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showCategorySelector by remember { mutableStateOf(false) }

    val dateDisplayFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    // Get all unique categories from expenses
    val allCategories = remember(expensesOnly) {
        expensesOnly.map { it.category }.distinct().sorted()
    }

    // Initialize selected categories to all when data loads
    LaunchedEffect(allCategories) {
        if (selectedCategories.isEmpty() && allCategories.isNotEmpty()) {
            selectedCategories = allCategories.toSet()
        }
    }

    // Filter expenses by date range
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

    // Further filter by selected categories
    val displayExpenses = remember(filteredExpenses, selectedCategories) {
        filteredExpenses.filter { selectedCategories.contains(it.category) }
    }

    // Group expenses by category and subcategory
    val categorySummary = remember(displayExpenses) {
        val summary = mutableMapOf<String, MutableMap<String, Double>>()
        displayExpenses.forEach { expense ->
            val category = expense.category
            val subCategory = expense.subCategory.ifBlank { "General" }

            summary.getOrPut(category) { mutableMapOf() }
            val currentAmount = summary[category]?.get(subCategory) ?: 0.0
            summary[category]?.put(subCategory, currentAmount + expense.amount)
        }
        summary
    }

    // Calculate total spent
    val totalSpent = remember(displayExpenses) {
        displayExpenses.sumOf { it.amount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Category Summary") }
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start: ${startDate?.let { dateDisplayFormat.format(it) } ?: "Select"}")
                        }

                        OutlinedButton(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("End: ${endDate?.let { dateDisplayFormat.format(it) } ?: "Select"}")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter and Category Select buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                android.widget.Toast.makeText(
                                    context,
                                    "Filter applied",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Filter")
                        }

                        OutlinedButton(
                            onClick = { showCategorySelector = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Checklist, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Select Categories")
                        }
                    }

                    // Clear filter button
                    if (startDate != null || endDate != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                startDate = null
                                endDate = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Date Filter")
                        }
                    }
                }
            }

            // Selected categories chips
            if (selectedCategories.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Showing: ${selectedCategories.size} categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Summary List
            if (displayExpenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No expenses found for selected period",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categorySummary.keys.toList().sorted()) { category ->
                        val subCategories = categorySummary[category] ?: emptyMap()
                        val categoryTotal = subCategories.values.sum()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // Main Category Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = category.uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "R${String.format("%.2f", categoryTotal)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Sub Categories
                                subCategories.toList().sortedBy { it.first }.forEach { (subCategory, amount) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• $subCategory",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "R${String.format("%.2f", amount)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Total Spent Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "TOTAL SPENT",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "R${String.format("%.2f", totalSpent)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    // Category Selector Dialog
    if (showCategorySelector) {
        CategorySelectorDialog(
            allCategories = allCategories,
            selectedCategories = selectedCategories,
            onConfirm = { selected ->
                selectedCategories = selected
                showCategorySelector = false
            },
            onDismiss = { showCategorySelector = false }
        )
    }
}