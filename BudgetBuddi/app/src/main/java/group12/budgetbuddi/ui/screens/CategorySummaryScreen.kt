package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import group12.budgetbuddi.data.entity.BudgetGoalEntity
import group12.budgetbuddi.data.entity.ExpenseEntity
import group12.budgetbuddi.repository.BudgetGoalRepository
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BrandGreenDark as BudgetGreen
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

private val SummaryBg = Color(0xFFE8F5E9)
private val SummaryText = Color(0xFF17231D)
private val SummaryMuted = Color(0xFF6B7D73)
private val SummaryBorder = Color(0xFFD7E8DE)
private val SummaryExpenseRed = Color(0xFFE53935)
private val SummaryAmber = Color(0xFFFF9800)
private val SummarySoftGreen = Color(0xFFEAF7EF)

private data class SummaryCategoryTotal(
    val category: String,
    val amount: Double,
    val subTotals: List<Pair<String, Double>>,
    val minGoal: Double?,
    val maxGoal: Double?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySummaryScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val allTransactions by expenseViewModel.allExpenses.collectAsState()
    val expensesOnly = remember(allTransactions) { allTransactions.filter { it.type == "expense" } }

    var startDate by remember { mutableStateOf<String?>(null) }
    var endDate by remember { mutableStateOf<String?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showCategorySelector by remember { mutableStateOf(false) }
    var expandedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }

    val dateDisplayFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateStorageFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val allCategories = remember(expensesOnly) {
        expensesOnly.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }

    LaunchedEffect(allCategories) {
        if (selectedCategories.isEmpty() && allCategories.isNotEmpty()) {
            selectedCategories = allCategories.toSet()
        }
    }

    val filteredExpenses = remember(expensesOnly, startDate, endDate, selectedCategories) {
        expensesOnly.filter { transaction ->
            val dateOk = (startDate == null || transaction.date >= startDate!!) &&
                    (endDate == null || transaction.date <= endDate!!)
            val categoryOk = selectedCategories.contains(transaction.category)
            dateOk && categoryOk
        }
    }

    val totalSpent = remember(filteredExpenses) { filteredExpenses.sumOf { it.amount } }

    val currentCal = remember { Calendar.getInstance() }
    val currentMonth = currentCal.get(Calendar.MONTH) + 1
    val currentYear = currentCal.get(Calendar.YEAR)
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val budgetGoalRepository = remember { BudgetGoalRepository() }
    var budgetGoals by remember { mutableStateOf<List<BudgetGoalEntity>>(emptyList()) }

    LaunchedEffect(uid, currentMonth, currentYear) {
        if (!uid.isNullOrBlank()) {
            budgetGoalRepository.getAllGoalsForMonth(uid, currentMonth, currentYear)
                .collect { goals -> budgetGoals = goals }
        }
    }

    val categoryTotals = remember(filteredExpenses, budgetGoals) {
        filteredExpenses
            .groupBy { it.category }
            .map { (category, list) ->
                val wholeGoal = budgetGoals.firstOrNull { goal ->
                    goal.goalType == "expense" &&
                            goal.categoryName == category &&
                            goal.subCategoryName.isBlank()
                }
                SummaryCategoryTotal(
                    category = category,
                    amount = list.sumOf { it.amount },
                    subTotals = list.groupBy { it.subCategory.ifBlank { "General" } }
                        .map { (sub, subList) -> sub to subList.sumOf { it.amount } }
                        .sortedByDescending { it.second },
                    minGoal = wholeGoal?.minGoal?.takeIf { it > 0.0 },
                    maxGoal = wholeGoal?.maxGoal?.takeIf { it > 0.0 }
                )
            }
            .sortedByDescending { it.amount }
    }

    AppScaffold(
        navController = navController,
        selectedRoute = "category_summary",
        authViewModel = authViewModel
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SummaryBg)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BudgetGreen,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Category Summary", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Expenses by category", fontSize = 13.sp, color = Color.White.copy(alpha = 0.82f))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    CategorySummaryFilterCard(
                        startDate = startDate,
                        endDate = endDate,
                        dateStorageFormat = dateStorageFormat,
                        dateDisplayFormat = dateDisplayFormat,
                        onStartClick = { showStartDatePicker = true },
                        onEndClick = { showEndDatePicker = true },
                        onClear = { startDate = null; endDate = null },
                        onCategoryClick = { showCategorySelector = true },
                        selectedCategoryCount = selectedCategories.size,
                        totalCategoryCount = allCategories.size
                    )
                }

                item { CategorySummaryTotalCard(totalSpent = totalSpent) }

                if (categoryTotals.isNotEmpty()) {
                    item { CategoryColumnGraphCard(items = categoryTotals) }
                }

                if (categoryTotals.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(58.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (selectedCategories.isEmpty()) "No categories selected" else "No expenses found",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SummaryText
                                )
                                Text(
                                    text = if (selectedCategories.isEmpty()) "Select at least one category to show the graph and breakdown." else "Try changing the date range or selected categories.",
                                    fontSize = 13.sp,
                                    color = SummaryMuted
                                )
                            }
                        }
                    }
                } else {
                    items(categoryTotals) { item ->
                        CategorySummaryDropdownRow(
                            item = item,
                            expanded = expandedCategories.contains(item.category),
                            onToggle = {
                                expandedCategories = if (expandedCategories.contains(item.category)) {
                                    expandedCategories - item.category
                                } else {
                                    expandedCategories + item.category
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showStartDatePicker) {
        SummaryDatePickerDialog(
            title = "Select Start Date",
            onDateSelected = {
                startDate = it
                if (endDate != null && endDate!! < it) endDate = it
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        SummaryDatePickerDialog(
            title = "Select End Date",
            onDateSelected = {
                if (startDate != null && it < startDate!!) {
                    startDate = it
                    endDate = it
                } else {
                    endDate = it
                }
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

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

@Composable
private fun CategorySummaryFilterCard(
    startDate: String?,
    endDate: String?,
    dateStorageFormat: SimpleDateFormat,
    dateDisplayFormat: SimpleDateFormat,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onClear: () -> Unit,
    onCategoryClick: () -> Unit,
    selectedCategoryCount: Int,
    totalCategoryCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SummaryBorder),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filters", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SummaryText)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onStartClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(startDate?.let { formatSummaryDate(it, dateStorageFormat, dateDisplayFormat) } ?: "Start", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(onClick = onEndClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(endDate?.let { formatSummaryDate(it, dateStorageFormat, dateDisplayFormat) } ?: "End", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onCategoryClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Categories: $selectedCategoryCount/$totalCategoryCount", fontSize = 12.sp)
                }
                if (startDate != null || endDate != null) {
                    TextButton(onClick = onClear) { Text("Clear", color = SummaryExpenseRed) }
                }
            }
        }
    }
}

@Composable
private fun CategorySummaryTotalCard(totalSpent: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SummaryBorder),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Expenses", fontSize = 13.sp, color = SummaryMuted)
                Text("R${String.format("%.2f", totalSpent)}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SummaryExpenseRed)
            }
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = SummarySoftGreen) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = BudgetGreen)
                }
            }
        }
    }
}

@Composable
private fun CategoryColumnGraphCard(items: List<SummaryCategoryTotal>) {
    val graphItems = items.take(6)
    val maxReference = graphItems.maxOfOrNull { item ->
        max(item.amount, max(item.minGoal ?: 0.0, item.maxGoal ?: 0.0))
    }?.takeIf { it > 0.0 } ?: 1.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SummaryBorder),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Amount Spent per Category", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SummaryText)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Min/max lines appear only for whole-category budget goals.", fontSize = 11.sp, color = SummaryMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                graphItems.forEach { item ->
                    CategoryGraphColumn(item = item, maxReference = maxReference, modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LegendDot(BudgetGreen); Text(" Spent", fontSize = 11.sp, color = SummaryMuted)
                Spacer(modifier = Modifier.width(12.dp))
                LegendDot(SummaryAmber); Text(" Min", fontSize = 11.sp, color = SummaryMuted)
                Spacer(modifier = Modifier.width(12.dp))
                LegendDot(SummaryExpenseRed); Text(" Max", fontSize = 11.sp, color = SummaryMuted)
            }
        }
    }
}

@Composable
private fun CategoryGraphColumn(item: SummaryCategoryTotal, maxReference: Double, modifier: Modifier = Modifier) {
    val barFraction = (item.amount / maxReference).toFloat().coerceIn(0.02f, 1f)
    val minFraction = item.minGoal?.let { (it / maxReference).toFloat().coerceIn(0f, 1f) }
    val maxFraction = item.maxGoal?.let { (it / maxReference).toFloat().coerceIn(0f, 1f) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .fillMaxHeight(barFraction)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(BudgetGreen)
            )
            Canvas(modifier = Modifier.matchParentSize()) {
                minFraction?.let {
                    val y = size.height - size.height * it
                    drawLine(SummaryAmber, Offset(2f, y), Offset(size.width - 2f, y), strokeWidth = 4f)
                }
                maxFraction?.let {
                    val y = size.height - size.height * it
                    drawLine(SummaryExpenseRed, Offset(2f, y), Offset(size.width - 2f, y), strokeWidth = 4f)
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("R${String.format("%.0f", item.amount)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SummaryText, maxLines = 1)
        Text(item.category, fontSize = 10.sp, color = SummaryMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
}

@Composable
private fun CategorySummaryDropdownRow(
    item: SummaryCategoryTotal,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SummaryBorder),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = SummarySoftGreen) {
                    Box(contentAlignment = Alignment.Center) { Text(getCategoryEmojiForSummary(item.category), fontSize = 22.sp) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.category, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SummaryText)
                    val goalText = when {
                        item.minGoal != null && item.maxGoal != null -> "Goal range: R${String.format("%.2f", item.minGoal)} - R${String.format("%.2f", item.maxGoal)}"
                        item.maxGoal != null -> "Max goal: R${String.format("%.2f", item.maxGoal)}"
                        item.minGoal != null -> "Min goal: R${String.format("%.2f", item.minGoal)}"
                        else -> "Tap to view subcategory breakdown"
                    }
                    Text(goalText, fontSize = 11.sp, color = SummaryMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("R${String.format("%.2f", item.amount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SummaryExpenseRed)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = BudgetGreen)
            }

            if (expanded) {
                HorizontalDivider(color = SummaryBorder)
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) {
                    item.subTotals.forEach { (subCategory, amount) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(subCategory, fontSize = 13.sp, color = SummaryText, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("R${String.format("%.2f", amount)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SummaryMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryDatePickerDialog(
    title: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val outputFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val monthTitleFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    var visibleMonth by remember { mutableStateOf(Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }) }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }

    val daysInMonth = visibleMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOffset = remember(visibleMonth.timeInMillis) {
        val first = visibleMonth.clone() as Calendar
        first.set(Calendar.DAY_OF_MONTH, 1)
        first.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.94f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SummaryText)
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { visibleMonth = (visibleMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = BudgetGreen)
                    }
                    Text(monthTitleFormat.format(visibleMonth.time), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SummaryText)
                    IconButton(onClick = { visibleMonth = (visibleMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = BudgetGreen)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(day, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SummaryMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val totalCells = firstDayOffset + daysInMonth
                val rows = (totalCells + 6) / 7
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(rows) { rowIndex ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(7) { colIndex ->
                                val cellIndex = rowIndex * 7 + colIndex
                                val dayNumber = cellIndex - firstDayOffset + 1
                                val valid = dayNumber in 1..daysInMonth
                                val cellCal = if (valid) (visibleMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, dayNumber) } else null
                                val isSelected = selectedDate?.let { selected ->
                                    cellCal != null && selected.get(Calendar.YEAR) == cellCal.get(Calendar.YEAR) && selected.get(Calendar.DAY_OF_YEAR) == cellCal.get(Calendar.DAY_OF_YEAR)
                                } == true

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) BudgetGreen else Color.Transparent)
                                        .clickable(enabled = valid) { selectedDate = cellCal },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (valid) {
                                        Text(dayNumber.toString(), fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color.White else SummaryText)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = SummaryMuted) }
                    TextButton(
                        onClick = { selectedDate?.let { onDateSelected(outputFormat.format(it.time)) } },
                        enabled = selectedDate != null
                    ) { Text("OK", color = BudgetGreen, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

private fun formatSummaryDate(dateString: String, storage: SimpleDateFormat, display: SimpleDateFormat): String {
    return try {
        val parsed = storage.parse(dateString)
        if (parsed != null) display.format(parsed) else dateString
    } catch (e: Exception) {
        dateString
    }
}

private fun getCategoryEmojiForSummary(category: String): String {
    return when (category.lowercase()) {
        "food", "food & drink", "groceries", "restaurant" -> "🍔"
        "transport", "car", "fuel" -> "🚗"
        "shopping", "clothing" -> "🛍️"
        "entertainment", "movie", "movies" -> "🎬"
        "housing", "rent" -> "🏠"
        "health", "healthcare", "medical" -> "🏥"
        "education" -> "📚"
        "bills", "utilities" -> "📄"
        else -> "💸"
    }
}
