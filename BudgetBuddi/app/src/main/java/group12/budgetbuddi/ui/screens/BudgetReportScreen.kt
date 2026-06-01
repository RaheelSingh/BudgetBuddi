package group12.budgetbuddi.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// ─── Colour palette ───────────────────────────────────────────────────────────
private val G      = Color(0xFF0DAB76)      // Brand green – used for positive numbers
private val GD     = Color(0xFF139A43)      // Dark green
private val RD     = Color(0xFFE53935)      // Red – used for negative "left"
private val AMBER  = Color(0xFFF59E0B)
private val BG     = Color(0xFFF7FAF9)      // Page background
private val CARD   = Color(0xFFFFFFFF)      // Card background
private val TXT    = Color(0xFF1A2E25)      // Primary text
private val TXT2   = Color(0xFF4A6F5E)      // Secondary text
private val TXT3   = Color(0xFF8AADA0)      // Tertiary text
private val BORD   = Color(0xFFE0EDE8)      // Border colour

// NEW: Distinct slice colours for expense categories (not shades of green)
private val SLICE_COLORS = listOf(
    Color(0xFF2196F3),   // Blue
    Color(0xFFFFC107),   // Yellow/Amber
    Color(0xFFE91E63),   // Pink
    Color(0xFFFF9800),   // Orange
    Color(0xFF9C27B0),   // Purple
    Color(0xFF00BCD4),   // Cyan
    Color(0xFF795548),   // Brown
    Color(0xFF607D8B)    // Blue Grey
)

// Data class for a slice (expense category)
private data class CategorySlice(val label: String, val amount: Double, val color: Color)

private val MONTH_NAMES = listOf(
    "January","February","March","April","May","June",
    "July","August","September","October","November","December"
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun BudgetReportScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val allExpenses by expenseViewModel.allExpenses.collectAsState()

    val nowCal = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableStateOf(nowCal.get(Calendar.MONTH)) }
    var selectedYear  by remember { mutableStateOf(nowCal.get(Calendar.YEAR)) }

    val monthPrefix = remember(selectedMonth, selectedYear) {
        "%04d-%02d".format(selectedYear, selectedMonth + 1)
    }
    val monthExpenses = remember(allExpenses, monthPrefix) {
        allExpenses.filter { it.date.startsWith(monthPrefix) }
    }

    val incomeList  = remember(monthExpenses) { monthExpenses.filter { it.type == "income" } }
    val expenseList = remember(monthExpenses) { monthExpenses.filter { it.type == "expense" } }

    val totalIncome   = incomeList.sumOf  { it.amount }
    val totalExpenses = expenseList.sumOf { it.amount }
    val leftOver      = totalIncome - totalExpenses
    val savings       = if (leftOver > 0) leftOver else 0.0

    // Expense category slices – each gets a distinct colour from the list
    val categorySlices = remember(expenseList) {
        expenseList
            .groupBy { it.category }
            .entries
            .sortedByDescending { it.value.sumOf { e -> e.amount } }
            .mapIndexed { i, (cat, items) ->
                CategorySlice(
                    label  = cat,
                    amount = items.sumOf { it.amount },
                    color  = SLICE_COLORS[i % SLICE_COLORS.size]
                )
            }
    }

    // Daily totals for bar chart
    val dailyExpenses = remember(expenseList, selectedMonth, selectedYear) {
        val cal = Calendar.getInstance()
        cal.set(selectedYear, selectedMonth, 1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        (1..daysInMonth).map { day ->
            val prefix2 = "%04d-%02d-%02d".format(selectedYear, selectedMonth + 1, day)
            expenseList.filter { it.date == prefix2 }.sumOf { it.amount }.toFloat()
        }
    }

    val avgDaily = remember(expenseList) {
        val days = expenseList.map { it.date }.distinct().size
        if (days > 0) totalExpenses / days else 0.0
    }

    AppScaffold(
        navController = navController,
        selectedRoute = "budget_report",
        authViewModel = authViewModel
    ) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().background(BG),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Top header
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(G, GD)))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text("Budget Report", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)).clickable {},
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Month navigator
            item {
                MonthNavigator(
                    selectedMonth = selectedMonth,
                    selectedYear  = selectedYear,
                    onPrevious = {
                        if (selectedMonth == 0) { selectedMonth = 11; selectedYear-- }
                        else selectedMonth--
                    },
                    onNext = {
                        val now = Calendar.getInstance()
                        if (selectedYear < now.get(Calendar.YEAR) ||
                            (selectedYear == now.get(Calendar.YEAR) && selectedMonth < now.get(Calendar.MONTH))) {
                            if (selectedMonth == 11) { selectedMonth = 0; selectedYear++ }
                            else selectedMonth++
                        }
                    }
                )
            }

            // Summary mini cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMiniCard(label = "Income",   amount = totalIncome,   color = G,     modifier = Modifier.weight(1f))
                    SummaryMiniCard(label = "Expenses", amount = totalExpenses, color = RD,    modifier = Modifier.weight(1f))
                    SummaryMiniCard(
                        label = "Left",
                        amount = leftOver,
                        color = if (leftOver < 0) RD else G,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Donut chart (spending breakdown)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                BudgetDonutCard(
                    totalExpenses = totalExpenses,
                    leftOver      = leftOver,
                    slices        = categorySlices
                )
            }

            // Stats dashboard
            item {
                Spacer(modifier = Modifier.height(14.dp))
                StatsDashboardCard(
                    totalIncome   = totalIncome,
                    totalExpenses = totalExpenses,
                    savings       = savings,
                    avgDaily      = avgDaily
                )
            }

            // Budget goal progress bar
            item {
                Spacer(modifier = Modifier.height(14.dp))
                BudgetGoalProgressCard(spent = totalExpenses, budget = totalIncome)
            }

            // Daily bar chart
            item {
                Spacer(modifier = Modifier.height(14.dp))
                DailyBarChartCard(
                    dailyValues = dailyExpenses,
                    month       = selectedMonth,
                    year        = selectedYear
                )
            }

            // Category breakdown list
            if (categorySlices.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    CategoryBreakdownCard(slices = categorySlices, total = totalExpenses)
                }
            }
        }
    }
}

// ─── Month navigator ──────────────────────────────────────────────────────────

@Composable
private fun MonthNavigator(
    selectedMonth: Int,
    selectedYear: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CARD),
        border = androidx.compose.foundation.BorderStroke(1.dp, BORD),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = G, modifier = Modifier.size(24.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${MONTH_NAMES[selectedMonth]} $selectedYear", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TXT)
                Text("Monthly Budget", fontSize = 11.sp, color = TXT3)
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = G, modifier = Modifier.size(24.dp))
            }
        }
    }
}

// ─── Summary mini cards ───────────────────────────────────────────────────────

@Composable
private fun SummaryMiniCard(label: String, amount: Double, color: Color, modifier: Modifier) {
    val bg = when (color) {
        G     -> Color(0xFFE6F7F1)
        RD    -> Color(0xFFFFEBEE)
        AMBER -> Color(0xFFFFF8E1)
        else  -> Color(0xFFE6F7F1)
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.8f), letterSpacing = 0.3.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "R${String.format("%.0f", amount)}",
                fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color
            )
        }
    }
}

// ─── Donut (pie) chart card – with new legend (no Income/Expenses rows) ──────

@Composable
private fun BudgetDonutCard(
    totalExpenses: Double,
    leftOver: Double,
    slices: List<CategorySlice>
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(900, easing = EaseOutCubic))
    }
    val progress by animProgress.asState()

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CARD),
        border = androidx.compose.foundation.BorderStroke(1.dp, BORD),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Spending Breakdown", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = TXT)
            Text("Where your money went this month", fontSize = 11.sp, color = TXT3, modifier = Modifier.padding(top = 2.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut canvas
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawDonut(slices, totalExpenses, progress)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "R${String.format("%.0f", leftOver)}",
                            fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (leftOver < 0) RD else G
                        )
                        Text("left", fontSize = 11.sp, color = TXT3)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Legend – only expense categories, plus a total expenses line
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    slices.take(4).forEach { slice ->
                        LegendRow(color = slice.color, label = slice.label, amount = slice.amount)
                    }
                    if (slices.size > 4) {
                        Text("+${slices.size - 4} more", fontSize = 10.sp, color = TXT3)
                    }
                    HorizontalDivider(color = BORD, modifier = Modifier.padding(vertical = 4.dp))
                    LegendRow(
                        color = TXT3,
                        label = "Total Expenses",
                        amount = totalExpenses,
                        isTotal = true
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawDonut(slices: List<CategorySlice>, total: Double, progress: Float) {
    if (total <= 0.0) {
        drawArc(
            color       = Color(0xFFE0EDE8),
            startAngle  = -90f,
            sweepAngle  = 360f,
            useCenter   = false,
            style       = Stroke(width = 28.dp.toPx(), cap = StrokeCap.Round)
        )
        return
    }
    val strokeWidth = 32.dp.toPx()
    val inset = strokeWidth / 2f
    val rect = Size(size.width - inset * 2, size.height - inset * 2)
    val topLeft = Offset(inset, inset)

    var startAngle = -90f
    slices.forEach { slice ->
        val sweep = ((slice.amount / total).toFloat() * 360f * progress).coerceAtLeast(0f)
        drawArc(
            color       = slice.color,
            startAngle  = startAngle,
            sweepAngle  = sweep,
            useCenter   = false,
            topLeft     = topLeft,
            size        = rect,
            style       = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        startAngle += sweep
    }
    val usedAngle = (slices.sumOf { it.amount } / total).toFloat() * 360f * progress
    if (360f - usedAngle > 0.5f) {
        drawArc(
            color       = Color(0xFFE0EDE8),
            startAngle  = startAngle,
            sweepAngle  = 360f - usedAngle,
            useCenter   = false,
            topLeft     = topLeft,
            size        = rect,
            style       = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
    }
}

fun drawArc(
    color: Color,
    startAngle: Float,
    sweepAngle: Float,
    useCenter: Boolean,
    topLeft: Offset,
    size: Size,
    style: Stroke
) {
}

@Composable
private fun LegendRow(color: Color, label: String, amount: Double, isTotal: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            label, fontSize = if (isTotal) 12.sp else 11.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) TXT else TXT2,
            modifier = Modifier.weight(1f), maxLines = 1
        )
        Text(
            "R${String.format("%.0f", amount)}",
            fontSize = if (isTotal) 12.sp else 11.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isTotal) TXT else TXT
        )
    }
}

// ─── Stats dashboard card ─────────────────────────────────────────────────────

@Composable
private fun StatsDashboardCard(
    totalIncome: Double,
    totalExpenses: Double,
    savings: Double,
    avgDaily: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CARD),
        border = androidx.compose.foundation.BorderStroke(1.dp, BORD),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📊", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dashboard", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TXT)
            }
            Spacer(modifier = Modifier.height(14.dp))

            StatRow(label = "Monthly Budget",   value = "R${String.format("%.0f", totalIncome)}",   valueColor = G)
            StatRow(label = "Monthly Average",  value = "R${String.format("%.0f", avgDaily)}",      valueColor = TXT2)
            StatRow(
                label = "Monthly Savings",
                value = "R${String.format("%.0f", savings)}",
                valueColor = if (savings < 0) RD else G
            )
            StatRow(label = "Monthly Expenses", value = "R${String.format("%.0f", totalExpenses)}", valueColor = RD)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = TXT2, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
    }
}

// ─── Budget goal progress bar ─────────────────────────────────────────────────

@Composable
private fun BudgetGoalProgressCard(spent: Double, budget: Double) {
    val pct = if (budget > 0) (spent / budget * 100).toInt().coerceIn(0, 100) else 0
    val barColor = when {
        pct >= 90 -> RD
        pct >= 70 -> AMBER
        else      -> G
    }

    val animWidth = remember { Animatable(0f) }
    LaunchedEffect(pct) {
        animWidth.animateTo(pct / 100f, animationSpec = tween(800, easing = EaseOutCubic))
    }
    val fraction by animWidth.asState()

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CARD),
        border = androidx.compose.foundation.BorderStroke(1.dp, BORD),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Budget Goal", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TXT)
                Text("$pct% used", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = barColor)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(10.dp)
                    .clip(RoundedCornerShape(10.dp)).background(Color(0xFFE0EDE8))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(fraction).fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp)).background(barColor)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Spent: R${String.format("%.0f", spent)}", fontSize = 11.sp, color = TXT3)
                Text("Budget: R${String.format("%.0f", budget)}", fontSize = 11.sp, color = TXT3)
            }
        }
    }
}

// ─── Daily bar chart ──────────────────────────────────────────────────────────

@Composable
private fun DailyBarChartCard(dailyValues: List<Float>, month: Int, year: Int) {
    val maxVal = dailyValues.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(dailyValues) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700, easing = EaseOutCubic))
    }
    val progress by animProgress.asState()

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CARD),
        border = androidx.compose.foundation.BorderStroke(1.dp, BORD),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Daily Expenses", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = TXT)
            Text("${MONTH_NAMES[month]} $year", fontSize = 11.sp, color = TXT3, modifier = Modifier.padding(top = 2.dp))
            Spacer(modifier = Modifier.height(14.dp))

            if (dailyValues.all { it == 0f }) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    Text("No expenses this month", fontSize = 13.sp, color = TXT3)
                }
            } else {
                Canvas(
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                ) {
                    val barCount   = dailyValues.size
                    val totalGap   = 2.dp.toPx() * (barCount - 1)
                    val barWidth   = (size.width - totalGap) / barCount
                    val chartH     = size.height

                    dailyValues.forEachIndexed { i, value ->
                        val barH    = (value / maxVal) * chartH * progress
                        val left    = i * (barWidth + 2.dp.toPx())
                        val top     = chartH - barH
                        val barColor = if (value == dailyValues.max()) RD else G

                        drawRoundRect(
                            color       = barColor,
                            topLeft     = Offset(left, top),
                            size        = Size(barWidth, barH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(1, 5, 10, 15, 20, 25, dailyValues.size).forEach { day ->
                        Text("$day", fontSize = 9.sp, color = TXT3)
                    }
                }
            }
        }
    }
}

// ─── Category breakdown list (with the new distinct colours) ──────────────────

@Composable
private fun CategoryBreakdownCard(slices: List<CategorySlice>, total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CARD),
        border = androidx.compose.foundation.BorderStroke(1.dp, BORD),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Category Breakdown", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = TXT)
            Spacer(modifier = Modifier.height(12.dp))

            slices.forEach { slice ->
                val pct = if (total > 0) (slice.amount / total * 100).toInt() else 0
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(slice.color))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(slice.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TXT)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("$pct%", fontSize = 11.sp, color = TXT3)
                            Text("R${String.format("%.2f", slice.amount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RD)
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFE0EDE8))) {
                        Box(modifier = Modifier.fillMaxWidth(pct / 100f).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(slice.color))
                    }
                }
            }
        }
    }
}