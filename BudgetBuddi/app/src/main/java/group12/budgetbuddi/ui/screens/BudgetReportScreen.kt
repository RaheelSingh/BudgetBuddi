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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import group12.budgetbuddi.repository.BudgetGoalRepository
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

private val ReportGreen = Color(0xFF139A43)
private val ReportBg = Color(0xFFE8F5E9)
private val ReportText = Color(0xFF1A1A1A)
private val ReportMuted = Color(0xFF6B6B6B)
private val ReportRed = Color(0xFFE53935)
private val ReportIncomeGreen = Color(0xFF43A047)
private val ReportAmber = Color(0xFFFF9800)
private val ReportBlue = Color(0xFF2196F3)
private val ReportPurple = Color(0xFF9C27B0)
private val ReportPink = Color(0xFFE91E63)
private val ReportTeal = Color(0xFF009688)
private val ReportBrown = Color(0xFF795548)

private val ReportSliceColors = listOf(
    ReportBlue,
    ReportAmber,
    ReportPink,
    ReportPurple,
    ReportTeal,
    ReportBrown,
    Color(0xFF607D8B),
    Color(0xFF3F51B5)
)

private data class ReportCategorySlice(
    val category: String,
    val amount: Double,
    val color: Color
)

@Composable
fun BudgetReportScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val allTransactions by expenseViewModel.allExpenses.collectAsState()

    val now = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableStateOf(now.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(now.get(Calendar.YEAR)) }

    val monthNames = remember {
        listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }

    val monthPrefix = remember(selectedMonth, selectedYear) {
        "%04d-%02d".format(selectedYear, selectedMonth + 1)
    }

    val monthTransactions = remember(allTransactions, monthPrefix) {
        allTransactions.filter { it.date.startsWith(monthPrefix) }
    }

    val monthExpenses = remember(monthTransactions) { monthTransactions.filter { it.type == "expense" } }
    val monthIncome = remember(monthTransactions) { monthTransactions.filter { it.type == "income" } }

    val totalIncome = remember(monthIncome) { monthIncome.sumOf { it.amount } }
    val totalExpenses = remember(monthExpenses) { monthExpenses.sumOf { it.amount } }
    val leftOver = totalIncome - totalExpenses
    val savings = if (leftOver > 0.0) leftOver else 0.0
    val averageDailyExpense = remember(monthExpenses) {
        val activeExpenseDays = monthExpenses.map { it.date }.distinct().size
        if (activeExpenseDays > 0) totalExpenses / activeExpenseDays else 0.0
    }

    val categorySlices = remember(monthExpenses) {
        monthExpenses
            .groupBy { it.category.ifBlank { "Uncategorised" } }
            .map { (category, list) -> category to list.sumOf { it.amount } }
            .sortedByDescending { it.second }
            .mapIndexed { index, pair ->
                ReportCategorySlice(
                    category = pair.first,
                    amount = pair.second,
                    color = ReportSliceColors[index % ReportSliceColors.size]
                )
            }
    }

    val dailyExpenses = remember(monthExpenses, selectedMonth, selectedYear) {
        val cal = Calendar.getInstance()
        cal.set(selectedYear, selectedMonth, 1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        (1..daysInMonth).map { day ->
            val date = "%04d-%02d-%02d".format(selectedYear, selectedMonth + 1, day)
            day to monthExpenses.filter { it.date == date }.sumOf { it.amount }
        }
    }

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val budgetGoalRepository = remember { BudgetGoalRepository() }
    var budgetGoalCount by remember { mutableStateOf(0) }

    LaunchedEffect(uid, selectedMonth, selectedYear) {
        if (!uid.isNullOrBlank()) {
            budgetGoalRepository.getAllGoalsForMonth(uid, selectedMonth + 1, selectedYear)
                .collect { goals -> budgetGoalCount = goals.size }
        } else {
            budgetGoalCount = 0
        }
    }

    AppScaffold(
        navController = navController,
        selectedRoute = "budget_report",
        authViewModel = authViewModel
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(ReportBg),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ReportGreen,
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Budget Report",
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${monthNames[selectedMonth]} $selectedYear",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                }
            }

            item {
                MonthNavigatorCard(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    monthNames = monthNames,
                    onPrevious = {
                        if (selectedMonth == 0) {
                            selectedMonth = 11
                            selectedYear--
                        } else {
                            selectedMonth--
                        }
                    },
                    onNext = {
                        val current = Calendar.getInstance()
                        val currentYear = current.get(Calendar.YEAR)
                        val currentMonth = current.get(Calendar.MONTH)
                        val canMoveForward = selectedYear < currentYear ||
                                (selectedYear == currentYear && selectedMonth < currentMonth)
                        if (canMoveForward) {
                            if (selectedMonth == 11) {
                                selectedMonth = 0
                                selectedYear++
                            } else {
                                selectedMonth++
                            }
                        }
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportMiniCard(
                        title = "Income",
                        amount = totalIncome,
                        icon = Icons.Default.TrendingUp,
                        color = ReportIncomeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    ReportMiniCard(
                        title = "Expenses",
                        amount = totalExpenses,
                        icon = Icons.Default.TrendingDown,
                        color = ReportRed,
                        modifier = Modifier.weight(1f)
                    )
                    ReportMiniCard(
                        title = "Left",
                        amount = leftOver,
                        icon = Icons.Default.AccountBalanceWallet,
                        color = if (leftOver >= 0) ReportIncomeGreen else ReportRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                ReportDashboardCard(
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    savings = savings,
                    averageDailyExpense = averageDailyExpense
                )
            }

            item {
                ReportCountsCard(
                    expensesCount = monthExpenses.size,
                    incomeCount = monthIncome.size,
                    budgetGoalCount = budgetGoalCount
                )
            }

            item {
                SpendingBreakdownCard(
                    totalExpenses = totalExpenses,
                    slices = categorySlices
                )
            }

            if (categorySlices.isNotEmpty()) {
                item {
                    CategoryBreakdownCard(
                        slices = categorySlices,
                        totalExpenses = totalExpenses
                    )
                }
            }

        }
    }
}

@Composable
private fun MonthNavigatorCard(
    selectedMonth: Int,
    selectedYear: Int,
    monthNames: List<String>,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = ReportGreen)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = monthNames[selectedMonth],
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ReportText
                )
                Text(
                    text = selectedYear.toString(),
                    fontSize = 12.sp,
                    color = ReportMuted
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = ReportGreen)
            }
        }
    }
}

@Composable
private fun ReportMiniCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(formatRand(amount, noDecimals = true), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ReportText)
            Text(title, fontSize = 11.sp, color = ReportMuted)
        }
    }
}

@Composable
private fun ReportDashboardCard(
    totalIncome: Double,
    totalExpenses: Double,
    savings: Double,
    averageDailyExpense: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Dashboard", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ReportText)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardInfoTile(
                    title = "Monthly Budget",
                    description = "Income available this month",
                    value = formatRand(totalIncome),
                    color = ReportIncomeGreen,
                    modifier = Modifier.weight(1f)
                )
                DashboardInfoTile(
                    title = "Average",
                    description = "Average per spending day",
                    value = formatRand(averageDailyExpense),
                    color = ReportBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardInfoTile(
                    title = "Savings",
                    description = "Income left after expenses",
                    value = formatRand(savings),
                    color = ReportGreen,
                    modifier = Modifier.weight(1f)
                )
                DashboardInfoTile(
                    title = "Expenses",
                    description = "Total spent this month",
                    value = formatRand(totalExpenses),
                    color = ReportRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DashboardInfoTile(
    title: String,
    description: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ReportText)
            Text(description, fontSize = 10.sp, color = ReportMuted, lineHeight = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun ReportCountsCard(
    expensesCount: Int,
    incomeCount: Int,
    budgetGoalCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Monthly Activity", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ReportText)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountActionTile(Icons.Default.TrendingDown, expensesCount.toString(), "Expenses")
                CountActionTile(Icons.Default.TrendingUp, incomeCount.toString(), "Income")
                CountActionTile(Icons.Default.TrackChanges, budgetGoalCount.toString(), "Goals")
            }
        }
    }
}

@Composable
private fun CountActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        modifier = Modifier.width(95.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = ReportGreen.copy(alpha = 0.1f), modifier = Modifier.size(42.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = ReportGreen, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ReportText)
        Text(label, fontSize = 11.sp, color = ReportMuted)
    }
}

@Composable
private fun SpendingBreakdownCard(
    totalExpenses: Double,
    slices: List<ReportCategorySlice>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Spending Breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ReportText)
            Text("Expense categories for the selected month", fontSize = 12.sp, color = ReportMuted)
            Spacer(modifier = Modifier.height(14.dp))

            if (slices.isEmpty() || totalExpenses <= 0.0) {
                Box(modifier = Modifier.height(150.dp), contentAlignment = Alignment.Center) {
                    Text("No expenses this month", fontSize = 13.sp, color = ReportMuted)
                }
            } else {
                ReportDonutChart(slices = slices, total = totalExpenses)
            }
        }
    }
}

@Composable
private fun ReportDonutChart(
    slices: List<ReportCategorySlice>,
    total: Double
) {
    Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(165.dp)) {
            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.amount / total * 360f).toFloat()
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(8f, 8f),
                    size = Size(size.width - 16f, size.height - 16f),
                    style = Stroke(width = 28f)
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total", fontSize = 11.sp, color = ReportMuted)
            Text(formatRand(total), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = ReportText)
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    slices: List<ReportCategorySlice>,
    totalExpenses: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Category Breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ReportText)
            Text("How each category contributes to monthly spending", fontSize = 12.sp, color = ReportMuted)
            Spacer(modifier = Modifier.height(12.dp))
            slices.forEach { slice ->
                val percent = if (totalExpenses > 0.0) (slice.amount / totalExpenses).toFloat() else 0f
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(slice.color))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(slice.category, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ReportText, maxLines = 1)
                    }
                    Text("${formatRand(slice.amount)} • ${(percent * 100).toInt()}%", fontSize = 12.sp, color = ReportMuted)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percent.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(slice.color)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun DailyExpenseChartCard(
    dailyExpenses: List<Pair<Int, Double>>,
    monthName: String
) {
    val maxValue = dailyExpenses.maxOfOrNull { it.second } ?: 0.0
    val nonZeroValues = dailyExpenses.map { it.second }.filter { it > 0.0 }
    val averageValue = if (nonZeroValues.isNotEmpty()) nonZeroValues.average() else 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Daily Expenses", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ReportText)
            Text(
                text = "Each column shows how much was spent on that day in $monthName. Taller columns mean higher daily spending.",
                fontSize = 11.sp,
                color = ReportMuted,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (maxValue <= 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No daily expenses to display", fontSize = 13.sp, color = ReportMuted)
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val barCount = dailyExpenses.size.coerceAtLeast(1)
                    val gap = 3.dp.toPx()
                    val barWidth = ((size.width - gap * (barCount - 1)) / barCount).coerceAtLeast(2f)
                    dailyExpenses.forEachIndexed { index, (_, value) ->
                        val barHeight = (value / maxValue * size.height).toFloat().coerceAtLeast(if (value > 0) 3f else 0f)
                        val x = index * (barWidth + gap)
                        drawRoundRect(
                            color = if (value > averageValue && averageValue > 0.0) ReportRed else ReportGreen,
                            topLeft = Offset(x, size.height - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1", fontSize = 10.sp, color = ReportMuted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ReportGreen))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("At/below average", fontSize = 10.sp, color = ReportMuted)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ReportRed))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Above average", fontSize = 10.sp, color = ReportMuted)
                    }
                    Text(dailyExpenses.lastOrNull()?.first?.toString() ?: "", fontSize = 10.sp, color = ReportMuted)
                }
            }
        }
    }
}

private fun formatRand(amount: Double, noDecimals: Boolean = false): String {
    val format = if (noDecimals) "%.0f" else "%.2f"
    return if (amount < 0) {
        "-R${String.format(format, abs(amount))}"
    } else {
        "R${String.format(format, amount)}"
    }
}
