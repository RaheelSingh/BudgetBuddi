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


// ExpenseListScreen.kt - Enhanced version
package group12.budgetbuddi.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.data.entity.ExpenseEntity
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BackgroundWhite
import group12.budgetbuddi.ui.theme.BrandGreen
import group12.budgetbuddi.ui.theme.TextBlack
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val allExpenses by expenseViewModel.allExpenses.collectAsState()

    // Filter to show only expenses
    val expensesOnly = remember(allExpenses) {
        allExpenses.filter { it.type == "expense" }
    }

    // State for expanded categories
    var expandedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }

    // State for date range filter
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStart by remember { mutableStateOf(true) }

    // Group expenses by category
    val groupedExpenses = remember(expensesOnly, startDate, endDate) {
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

        filtered.groupBy { it.category }
            .map { (category, expenses) ->
                category to expenses.sortedByDescending { it.date }
            }
            .sortedBy { it.first }
    }

    val totalExpenses = remember(expensesOnly, startDate, endDate) {
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
        filtered.sumOf { it.amount }
    }

    val dateDisplayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun toggleCategory(category: String) {
        expandedCategories = if (expandedCategories.contains(category)) {
            expandedCategories - category
        } else {
            expandedCategories + category
        }
    }

    AppScaffold(
        navController = navController,
        selectedRoute = "expense_list",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundWhite)
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BrandGreen,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Expense List",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track and manage your spending",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Stats row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Expenses",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "R${String.format("%.2f", totalExpenses)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandGreen
                        )
                    }

                    // Filter button
                    FilterChip(
                        onClick = { showDatePicker = true },
                        label = {
                            Text(
                                if (startDate != null || endDate != null) "Filter Active" else "Filter",
                                fontSize = 12.sp
                            )
                        },
                        selected = startDate != null || endDate != null,
                        leadingIcon = {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(50.dp))
                    )
                }
            }

            // Date filter indicators
            if (startDate != null || endDate != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (startDate != null) {
                        AssistChip(
                            onClick = { startDate = null },
                            label = {
                                Text("From: ${dateDisplayFormat.format(startDate)}", fontSize = 11.sp)
                            },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                    if (endDate != null) {
                        AssistChip(
                            onClick = { endDate = null },
                            label = {
                                Text("To: ${dateDisplayFormat.format(endDate)}", fontSize = 11.sp)
                            },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                    if (startDate != null || endDate != null) {
                        TextButton(
                            onClick = {
                                startDate = null
                                endDate = null
                            },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Clear All", fontSize = 11.sp, color = BrandGreen)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (groupedExpenses.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = BrandGreen.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = BrandGreen,
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }
                        Text(
                            text = "No Expenses Found",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )
                        Text(
                            text = if (startDate != null || endDate != null) {
                                "Try adjusting your date filter"
                            } else {
                                "Add your first expense to get started"
                            },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        if (startDate != null || endDate != null) {
                            TextButton(
                                onClick = {
                                    startDate = null
                                    endDate = null
                                }
                            ) {
                                Text("Clear Filters", color = BrandGreen)
                            }
                        } else {
                            Button(
                                onClick = { navController.navigate("add_transaction") },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Expense")
                            }
                        }
                    }
                }
            } else {
                // Expense list grouped by category
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(groupedExpenses) { (category, expenses) ->
                        val isExpanded = expandedCategories.contains(category)
                        val categoryTotal = expenses.sumOf { it.amount }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column {
                                // Category header (always visible)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { toggleCategory(category) }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Category icon
                                        Surface(
                                            modifier = Modifier.size(44.dp),
                                            shape = CircleShape,
                                            color = BrandGreen.copy(alpha = 0.1f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = getCategoryEmoji(category),
                                                    fontSize = 22.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = category,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextBlack
                                            )
                                            Text(
                                                text = "${expenses.size} transaction${if (expenses.size != 1) "s" else ""}",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "R${String.format("%.2f", categoryTotal)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BrandGreen
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                                            tint = Color.Gray
                                        )
                                    }
                                }

                                // Expanded content - list of expenses in this category
                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    ) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = Color.LightGray.copy(alpha = 0.3f)
                                        )
                                        expenses.forEach { expense ->
                                            CategoryExpenseItem(
                                                expense = expense,
                                                onClick = {
                                                    // Navigate to edit/delete expense detail
                                                }
                                            )
                                            if (expense != expenses.last()) {
                                                HorizontalDivider(
                                                    modifier = Modifier.padding(horizontal = 16.dp),
                                                    color = Color.LightGray.copy(alpha = 0.15f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text(if (isSelectingStart) "Select Start Date" else "Select End Date") },
            text = {
                DatePickerDialog(
                    onDateSelected = { date ->
                        if (isSelectingStart) {
                            startDate = date
                            isSelectingStart = false
                            showDatePicker = true // Keep open for end date
                        } else {
                            endDate = date
                            showDatePicker = false
                            isSelectingStart = true
                        }
                    },
                    onDismiss = {
                        showDatePicker = false
                        isSelectingStart = true
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CategoryExpenseItem(
    expense: ExpenseEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.subCategory.ifBlank { expense.category },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextBlack
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(expense.date),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                if (expense.paymentMethod.isNotBlank()) {
                    Text(
                        text = "• ${expense.paymentMethod}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                if (expense.note.isNotBlank()) {
                    Text(
                        text = "• ${expense.note.take(15)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Text(
            text = "-R${String.format("%.2f", expense.amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE53935)
        )
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "food", "groceries", "restaurant" -> "🍔"
        "transport", "car", "fuel" -> "🚗"
        "shopping", "clothing" -> "🛍️"
        "entertainment", "movies" -> "🎬"
        "bills", "utilities" -> "📄"
        "health", "medical" -> "🏥"
        "education" -> "📚"
        "housing", "rent" -> "🏠"
        "travel" -> "✈️"
        else -> "💸"
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}