// HomeScreen.kt - Complete Fancy Version (No Errors)
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


@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val displayName = currentUser?.username ?: "User"
    val allTransactions by expenseViewModel.allExpenses.collectAsState()

    // Get current month and date info
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    val today = SimpleDateFormat("yyyy-MM-dd", LocalLocale.current.platformLocale).format(Date())

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val currentMonthName = monthNames[currentMonth]

    // Filter transactions for current month
    val monthlyTransactions = remember(allTransactions, currentMonth, currentYear) {
        allTransactions.filter { transaction ->
            try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(transaction.date)
                val cal = Calendar.getInstance().apply { time = date ?: Date() }
                cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
            } catch (e: Exception) { false }
        }
    }

    val monthlyExpenses = remember(monthlyTransactions) {
        monthlyTransactions.filter { it.type == "expense" }
    }

    val monthlyIncome = remember(monthlyTransactions) {
        monthlyTransactions.filter { it.type == "income" }
    }

    val totalMonthlyExpenses = remember(monthlyExpenses) { monthlyExpenses.sumOf { it.amount } }
    val totalMonthlyIncome = remember(monthlyIncome) { monthlyIncome.sumOf { it.amount } }
    val netMonthly = totalMonthlyIncome - totalMonthlyExpenses

    val todayExpenses = remember(allTransactions, today) {
        allTransactions.filter { it.date == today && it.type == "expense" }.sumOf { it.amount }
    }

    // Get top spending categories
    val topCategories = remember(monthlyExpenses) {
        monthlyExpenses.groupBy { it.category }
            .map { (category, expenses) -> category to expenses.sumOf { it.amount } }
            .sortedByDescending { it.second }
            .take(5)
    }

    // Recent transactions (last 5)
    val recentTransactions = remember(monthlyTransactions) {
        monthlyTransactions.sortedByDescending { it.date }.take(5)
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
        selectedRoute = "home",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E9))
                .graphicsLayer(alpha = contentAlpha)
        ) {
            // Fancy Header with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                        clip = false
                    )
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
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        BrandGreen,
                                        Color(0xFF0D8B5E)
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
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
                                        text = "Good ${getTimeOfDay()},",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = displayName,
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = "💰 Ready to save today?",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White.copy(alpha = 0.7f),
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

                            Spacer(modifier = Modifier.height(24.dp))

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
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Welcome to Budget Buddi!",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Track your spending and reach your goals",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.8f)
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
                // Monthly Summary Card
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
                                        .clickable {
                                            navController.navigate("category_totals")
                                        },
                                    shape = CircleShape,
                                    color = BrandGreen.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = "View Monthly Report",
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
                                FancyStatItem(
                                    title = "Income",
                                    amount = totalMonthlyIncome,
                                    color = Color(0xFF43A047),
                                    icon = Icons.Default.TrendingUp,
                                    bgColor = Color(0xFFE8F5E9)
                                )
                                FancyStatItem(
                                    title = "Expenses",
                                    amount = totalMonthlyExpenses,
                                    color = Color(0xFFE53935),
                                    icon = Icons.Default.TrendingDown,
                                    bgColor = Color(0xFFFFEBEE)
                                )
                                FancyStatItem(
                                    title = "Net",
                                    amount = netMonthly,
                                    color = if (netMonthly >= 0) Color(0xFF43A047) else Color(0xFFE53935),
                                    icon = Icons.Default.AccountBalanceWallet,
                                    bgColor = if (netMonthly >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                )
                            }
                        }
                    }
                }

                // Today's Spending Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(4.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandGreen.copy(alpha = 0.05f))
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
                                    color = BrandGreen.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Today,
                                            contentDescription = null,
                                            tint = BrandGreen,
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
                            Text(
                                text = "Income vs Expenses",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextBlack,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            HomeIncomeExpensePieChart(
                                income = totalMonthlyIncome,
                                expenses = totalMonthlyExpenses
                            )
                        }
                    }
                }

                // Top Spending Categories
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
                                        Icons.Default.ShowChart,
                                        contentDescription = null,
                                        tint = BrandGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                topCategories.forEachIndexed { index, (category, amount) ->
                                    FancyCategoryProgressRow(
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

                // Recent Transactions
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
                                    FancyRecentTransactionItem(transaction = transaction)
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

                // Quick Actions
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
                                FancyQuickActionButton(
                                    icon = Icons.Default.AddCircle,
                                    label = "Add",
                                    subtitle = "Transaction",
                                    onClick = { navController.navigate("add_transaction") },
                                    modifier = Modifier.weight(1f)
                                )
                                FancyQuickActionButton(
                                    icon = Icons.Default.Category,
                                    label = "Manage",
                                    subtitle = "Categories",
                                    onClick = { navController.navigate("categories") },
                                    modifier = Modifier.weight(1f)
                                )
                                FancyQuickActionButton(
                                    icon = Icons.Default.TrackChanges,
                                    label = "Budget",
                                    subtitle = "Goals",
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

// Helper function to get time of day
fun getTimeOfDay(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Morning"
        in 12..16 -> "Afternoon"
        else -> "Evening"
    }
}

@Composable
fun FancyStatItem(
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
            color = Color.Gray
        )
    }
}

@Composable
fun FancyCategoryProgressRow(
    category: String,
    amount: Double,
    total: Double,
    maxAmount: Double,
    rank: Int
) {
    val percentage = if (total > 0) (amount / total) else 0.0
    val barWidth = if (maxAmount > 0) (amount / maxAmount).toFloat().coerceIn(0f, 1f) else 0f

    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.Gray.copy(alpha = 0.3f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(rankColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) Color.Black else Color.White
            )
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextBlack
                    )
                }
                Text(
                    text = "R${String.format("%.2f", amount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextBlack
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
                        .background(BrandGreen)
                )
            }

            Text(
                text = "${(percentage * 100).toInt()}% of total • Rank #$rank",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun FancyRecentTransactionItem(transaction: group12.budgetbuddi.data.entity.ExpenseEntity) {
    val isExpense = transaction.type == "expense"
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
                color = BrandGreen.copy(alpha = 0.1f)
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
            color = if (isExpense) Color(0xFFE53935) else Color(0xFF43A047)
        )
    }
}

@Composable
fun FancyQuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(Color(0xFFF8F9FA))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = BrandGreen.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = BrandGreen,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun HomeIncomeExpensePieChart(
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

    val incomeColor = Color(0xFF43A047)
    val expenseColor = Color(0xFFE53935)

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

// Helper functions
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