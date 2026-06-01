package group12.budgetbuddi.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.data.entity.ExpenseEntity
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BrandGreen
import group12.budgetbuddi.ui.theme.TextBlack
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale

// Light green background color
val LightGreenBg = Color(0xFFE8F5E9)

// Colorful accent colors
val AccentOrange = Color(0xFFFF9800)
val AccentBlue = Color(0xFF2196F3)
val AccentPurple = Color(0xFF9C27B0)
val AccentPink = Color(0xFFE91E63)
val AccentTeal = Color(0xFF009688)
val ExpenseRed = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
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

    // State for expanded categories
    var expandedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }

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

    // Group expenses by category
    val groupedExpenses = remember(filteredExpenses) {
        filteredExpenses.groupBy { it.category }
            .map { (category, expenses) ->
                category to expenses.sortedByDescending { it.date }
            }
            .sortedBy { it.first }
    }

    val totalExpenses = remember(filteredExpenses) {
        filteredExpenses.sumOf { it.amount }
    }

    val dateDisplayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun toggleCategory(category: String) {
        expandedCategories = if (expandedCategories.contains(category)) {
            expandedCategories - category
        } else {
            expandedCategories + category
        }
    }

    // Animation
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "contentAlpha"
    )

    AppScaffold(
        navController = navController,
        selectedRoute = "expense_list",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(LightGreenBg)  // <-- Light green background added here
                .graphicsLayer(alpha = contentAlpha)
        ) {
            // Colorful Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), clip = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                    color = BrandGreen
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        BrandGreen,
                                        Color(0xFF0D8B5E),
                                        Color(0xFF139A43),
                                        Color(0xFF1EAB5A)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 28.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "Expense List",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Track all your spending",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .shadow(8.dp, CircleShape),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Stats and Filter Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Total Expenses Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Expenses",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "R${String.format("%.2f", totalExpenses)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }

                        // Filter button with date indicator
                        FilterChip(
                            onClick = { showStartDatePicker = true },
                            label = {
                                Text(
                                    if (startDate != null || endDate != null) "Filter Active" else "Set Date Range",
                                    fontSize = 12.sp
                                )
                            },
                            selected = startDate != null || endDate != null,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandGreen.copy(alpha = 0.1f),
                                selectedLabelColor = BrandGreen
                            ),
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(50.dp))
                        )
                    }

                    // Date Range Display
                    if (startDate != null || endDate != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = BrandGreen.copy(alpha = 0.05f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = BrandGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "From: ${startDate?.let { dateDisplayFormat.format(it) } ?: "Any"}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = BrandGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "To: ${endDate?.let { dateDisplayFormat.format(it) } ?: "Any"}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            startDate = null
                                            endDate = null
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expense List
            if (groupedExpenses.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = CircleShape,
                            color = BrandGreen.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = BrandGreen,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                        Text(
                            text = "No Expenses Found",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )
                        Text(
                            text = if (startDate != null || endDate != null) {
                                "No expenses in this date range"
                            } else {
                                "Add your first expense to get started"
                            },
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (startDate != null || endDate != null) {
                            OutlinedButton(
                                onClick = {
                                    startDate = null
                                    endDate = null
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = BrandGreen
                                ),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear Filters")
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(groupedExpenses) { (category, expenses) ->
                        val isExpanded = expandedCategories.contains(category)
                        val categoryTotal = expenses.sumOf { it.amount }
                        val categoryColor = getCategoryColor(category)

                        // Category Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column {
                                // Category Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { toggleCategory(category) }
                                        .background(categoryColor.copy(alpha = 0.05f))
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Category Icon
                                        Surface(
                                            modifier = Modifier.size(48.dp),
                                            shape = CircleShape,
                                            color = categoryColor.copy(alpha = 0.15f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = getExpenseCategoryEmoji(category),
                                                    fontSize = 24.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
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

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "R${String.format("%.2f", categoryTotal)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = categoryColor
                                        )
                                        Icon(
                                            if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                                            tint = categoryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Expanded Expenses List
                                if (isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .padding(bottom = 8.dp)
                                    ) {
                                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

                                        expenses.forEachIndexed { index, expense ->
                                            ColorfulExpenseListItem(
                                                expense = expense,
                                                onClick = {
                                                    Toast.makeText(
                                                        context,
                                                        "${expense.category}: R${expense.amount}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                            if (index < expenses.size - 1) {
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

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        DatePickerDialog(
            title = "Select Start Date",
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
                // Open end date picker after selecting start date
                showEndDatePicker = true
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        DatePickerDialog(
            title = "Select End Date",
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
                Toast.makeText(context, "Filter applied: ${dateDisplayFormat.format(startDate)} - ${dateDisplayFormat.format(date)}", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
fun ColorfulExpenseListItem(
    expense: ExpenseEntity,
    onClick: () -> Unit
) {
    val categoryColor = getCategoryColor(expense.category)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small colored dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(categoryColor)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = expense.subCategory.ifBlank { expense.category },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextBlack
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatExpenseDate(expense.date),
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
        }

        Text(
            text = "-R${String.format("%.2f", expense.amount)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = ExpenseRed
        )
    }
}

@Composable
fun DatePickerDialog(
    title: String,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                DatePicker(state = datePickerState, showModeToggle = false)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Date(millis))
                    }
                }
            ) {
                Text("OK", color = BrandGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

// Helper functions
fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "food", "groceries", "restaurant" -> AccentOrange
        "transport", "car", "fuel" -> AccentBlue
        "shopping", "clothing" -> AccentPurple
        "entertainment", "movies" -> AccentPink
        "bills", "utilities" -> Color(0xFF607D8B)
        "health", "medical" -> Color(0xFF4CAF50)
        "education" -> AccentTeal
        else -> BrandGreen
    }
}

fun getExpenseCategoryEmoji(category: String): String {
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

fun formatExpenseDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}