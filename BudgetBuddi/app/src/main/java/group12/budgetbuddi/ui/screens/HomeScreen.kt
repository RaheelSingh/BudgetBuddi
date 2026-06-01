// HomeScreen.kt - Colorful Premium Version
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BackgroundWhite
import group12.budgetbuddi.ui.theme.BrandGreen
import group12.budgetbuddi.ui.theme.TextBlack
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale

// Color palette (keeping BrandGreen as primary)
val AccentOrange = Color(0xFFFF9800)
val AccentBlue = Color(0xFF2196F3)
val AccentPurple = Color(0xFF9C27B0)
val AccentPink = Color(0xFFE91E63)
val AccentTeal = Color(0xFF009688)
val AccentYellow = Color(0xFFFFC107)
val SuccessGreen = Color(0xFF4CAF50)
val DangerRed = Color(0xFFE53935)
val IncomeGreen = Color(0xFF43A047)
val ExpenseRed = Color(0xFFE53935)
val CardGradientStart = Color(0xFFFFFFFF)
val CardGradientEnd = Color(0xFFF8F9FA)

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val displayName = currentUser?.username ?: "User"
    val allTransactions by expenseViewModel.allExpenses.collectAsState()

    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    val today = SimpleDateFormat("yyyy-MM-dd", LocalLocale.current.platformLocale).format(Date())

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val currentMonthName = monthNames[currentMonth]

    val monthlyTransactions = remember(allTransactions, currentMonth, currentYear) {
        allTransactions.filter { transaction ->
            try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(transaction.date)
                val cal = Calendar.getInstance().apply { time = date ?: Date() }
                cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
            } catch (e: Exception) { false }
        }
    }

    val monthlyExpenses = remember(monthlyTransactions) { monthlyTransactions.filter { it.type == "expense" } }
    val monthlyIncome = remember(monthlyTransactions) { monthlyTransactions.filter { it.type == "income" } }

    val totalMonthlyExpenses = remember(monthlyExpenses) { monthlyExpenses.sumOf { it.amount } }
    val totalMonthlyIncome = remember(monthlyIncome) { monthlyIncome.sumOf { it.amount } }
    val netMonthly = totalMonthlyIncome - totalMonthlyExpenses

    val todayExpenses = remember(allTransactions, today) {
        allTransactions.filter { it.date == today && it.type == "expense" }.sumOf { it.amount }
    }

    val topCategories = remember(monthlyExpenses) {
        monthlyExpenses.groupBy { it.category }
            .map { (category, expenses) -> category to expenses.sumOf { it.amount } }
            .sortedByDescending { it.second }
            .take(5)
    }

    val recentTransactions = remember(monthlyTransactions) {
        monthlyTransactions.sortedByDescending { it.date }.take(5)
    }

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "contentAlpha"
    )

    AppScaffold(
        navController = navController,
        selectedRoute = "home",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundWhite)
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
                                        text = "Good ${getTimeOfDay()}!",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.9f),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = displayName,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    // Colorful motivational message that changes
                                    Text(
                                        text = getMotivationalMessage(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .shadow(12.dp, CircleShape),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = displayName.take(1).uppercase(),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Colorful Welcome Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.15f)
                                ),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Animated sparkle
                                    val infiniteTransition = rememberInfiniteTransition()
                                    val sparkleScale by infiniteTransition.animateFloat(
                                        initialValue = 0.8f,
                                        targetValue = 1.2f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = FastOutSlowInEasing),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(24.dp).graphicsLayer(scaleX = sparkleScale, scaleY = sparkleScale)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "You're doing great! 🎉",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Keep tracking your expenses daily",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Monthly Summary Card - Colorful Stats
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(4.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = BrandGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$currentMonthName Summary",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextBlack
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { navController.navigate("category_totals") },
                                    shape = CircleShape,
                                    color = BrandGreen.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = "View Report",
                                            tint = BrandGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ColorfulStatItem(
                                    title = "Income",
                                    amount = totalMonthlyIncome,
                                    color = IncomeGreen,
                                    icon = Icons.Default.TrendingUp,
                                    bgColor = IncomeGreen.copy(alpha = 0.15f)
                                )
                                ColorfulStatItem(
                                    title = "Expenses",
                                    amount = totalMonthlyExpenses,
                                    color = ExpenseRed,
                                    icon = Icons.Default.TrendingDown,
                                    bgColor = ExpenseRed.copy(alpha = 0.15f)
                                )
                                ColorfulStatItem(
                                    title = "Net",
                                    amount = netMonthly,
                                    color = if (netMonthly >= 0) IncomeGreen else ExpenseRed,
                                    icon = Icons.Default.AccountBalanceWallet,
                                    bgColor = if (netMonthly >= 0) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }

                // Today's Spending Card - Colorful accent
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(4.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(50.dp),
                                    shape = CircleShape,
                                    color = AccentOrange.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Today,
                                            contentDescription = null,
                                            tint = AccentOrange,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Today's Spending",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = formatHomeDate(today),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Text(
                                text = "R${String.format("%.2f", todayExpenses)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreen
                            )
                        }
                    }
                }

                // Income vs Expense Pie Chart Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(4.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PieChart,
                                    contentDescription = null,
                                    tint = BrandGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Income vs Expenses",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextBlack
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            ColorfulPieChart(
                                income = totalMonthlyIncome,
                                expenses = totalMonthlyExpenses
                            )
                        }
                    }
                }

                // Top Spending Categories - Colorful ranking
                if (topCategories.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .shadow(4.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Top Spending Categories",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextBlack
                                    )
                                    Icon(
                                        Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = BrandGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                topCategories.forEachIndexed { index, (category, amount) ->
                                    ColorfulCategoryProgressRow(
                                        category = category,
                                        amount = amount,
                                        total = totalMonthlyExpenses,
                                        maxAmount = topCategories.firstOrNull()?.second ?: amount,
                                        rank = index + 1
                                    )
                                    if (index < topCategories.size - 1) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Transactions - Colorful items
                if (recentTransactions.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .shadow(4.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recent Transactions",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextBlack
                                    )
                                    TextButton(
                                        onClick = { navController.navigate("expense_list") }
                                    ) {
                                        Text("View All", color = BrandGreen, fontSize = 13.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                recentTransactions.forEachIndexed { index, transaction ->
                                    ColorfulRecentTransactionItem(transaction = transaction)
                                    if (index < recentTransactions.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            color = Color.LightGray.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Actions - Colorful buttons
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(4.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Quick Actions",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextBlack
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ColorfulQuickActionButton(
                                    icon = Icons.Default.AddCircle,
                                    label = "Add",
                                    subtitle = "Transaction",
                                    color = BrandGreen,
                                    onClick = { navController.navigate("add_transaction") },
                                    modifier = Modifier.weight(1f)
                                )
                                ColorfulQuickActionButton(
                                    icon = Icons.Default.Category,
                                    label = "Manage",
                                    subtitle = "Categories",
                                    color = AccentPurple,
                                    onClick = { navController.navigate("categories") },
                                    modifier = Modifier.weight(1f)
                                )
                                ColorfulQuickActionButton(
                                    icon = Icons.Default.TrackChanges,
                                    label = "Budget",
                                    subtitle = "Goals",
                                    color = AccentOrange,
                                    onClick = { navController.navigate("budget_goals") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getTimeOfDay(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Morning"
        in 12..16 -> "Afternoon"
        else -> "Evening"
    }
}

fun getMotivationalMessage(): String {
    val messages = listOf(
        "💰 Every rand saved is a step closer to your dreams!",
        "📊 Track today, conquer tomorrow!",
        "🎯 You're 1% closer to your financial goals!",
        "💪 Small savings add up to big results!",
        "🌟 Keep going! You're doing amazing!",
        "📈 Your financial journey starts here!",
        "🦸‍♂️ Be the hero of your finances!",
        "🚀 Sky's the limit for your savings!"
    )
    return messages.random()
}

@Composable
fun ColorfulStatItem(
    title: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "R${String.format("%.2f", amount)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack
        )
        Text(
            text = title,
            fontSize = 11.sp,
            color = color
        )
    }
}

@Composable
fun ColorfulCategoryProgressRow(
    category: String,
    amount: Double,
    total: Double,
    maxAmount: Double,
    rank: Int
) {
    val percentage = if (total > 0) (amount / total) else 0.0
    val barWidth = if (maxAmount > 0) (amount / maxAmount).toFloat().coerceIn(0f, 1f) else 0f

    // Colorful rank badges
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> AccentBlue.copy(alpha = 0.5f)
    }

    val categoryColor = when (category.lowercase()) {
        "food", "groceries", "restaurant" -> AccentOrange
        "transport", "car", "fuel" -> AccentBlue
        "shopping", "clothing" -> AccentPurple
        "entertainment", "movies" -> AccentPink
        "bills", "utilities" -> Color(0xFF607D8B)
        "health", "medical" -> SuccessGreen
        "education" -> AccentTeal
        else -> BrandGreen
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(rankColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (rank <= 3) "#$rank" else "",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) Color.Black else Color.White
            )
            if (rank > 3) {
                Icon(Icons.Default.Straighten, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getHomeCategoryEmoji(category),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextBlack
                    )
                }
                Text(
                    text = "R${String.format("%.2f", amount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = categoryColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(categoryColor)
                )
            }

            Text(
                text = "${(percentage * 100).toInt()}% of total spending",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ColorfulRecentTransactionItem(transaction: group12.budgetbuddi.data.entity.ExpenseEntity) {
    val isExpense = transaction.type == "expense"
    val transactionColor = if (isExpense) ExpenseRed else IncomeGreen

    val categoryColor = when (transaction.category.lowercase()) {
        "food", "groceries", "restaurant" -> AccentOrange
        "transport", "car", "fuel" -> AccentBlue
        "shopping", "clothing" -> AccentPurple
        "entertainment", "movies" -> AccentPink
        else -> BrandGreen
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = categoryColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = getHomeCategoryEmoji(transaction.category),
                        fontSize = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = transaction.category,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextBlack
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatHomeDate(transaction.date),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    if (transaction.subCategory.isNotBlank()) {
                        Text(
                            text = "• ${transaction.subCategory.take(12)}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        Text(
            text = "${if (isExpense) "-" else "+"}R${String.format("%.2f", transaction.amount)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = transactionColor
        )
    }
}

@Composable
fun ColorfulQuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(color.copy(alpha = 0.1f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ColorfulPieChart(
    income: Double,
    expenses: Double
) {
    val total = income + expenses

    if (total == 0.0) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PieChart,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No data yet",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Add income or expenses",
                        fontSize = 11.sp,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }
        }
        return
    }

    val incomePercentage = (income / total).toFloat()
    val expensePercentage = (expenses / total).toFloat()

    val incomeAngle = incomePercentage * 360f
    val expenseAngle = expensePercentage * 360f

    val incomeColor = IncomeGreen
    val expenseColor = ExpenseRed

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier.size(160.dp)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2 - 10

            var startAngle = -90f

            if (income > 0) {
                drawArc(
                    color = incomeColor,
                    startAngle = startAngle,
                    sweepAngle = incomeAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )
                startAngle += incomeAngle
            }

            if (expenses > 0) {
                drawArc(
                    color = expenseColor,
                    startAngle = startAngle,
                    sweepAngle = expenseAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(incomeColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Income (${(incomePercentage * 100).toInt()}%)",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(expenseColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Expenses (${(expensePercentage * 100).toInt()}%)",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Net: ",
                fontSize = 13.sp,
                color = Color.Gray
            )
            Text(
                text = "R${String.format("%.2f", income - expenses)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (income - expenses >= 0) incomeColor else expenseColor
            )
        }
    }
}

// Existing helper functions
fun getHomeCategoryEmoji(category: String): String {
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

fun formatHomeDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}