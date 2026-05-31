// HomeScreen.kt - Fixed version with clickable calendar icon
package group12.budgetbuddi.ui.screens

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
                .background(BackgroundWhite)
                .graphicsLayer(alpha = contentAlpha)
        ) {
            // Header with greeting
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BrandGreen,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hello,",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = displayName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = displayName.take(1).uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Welcome message
                    Text(
                        text = "Welcome to Budget Buddi!",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Here's your financial overview",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Monthly Summary Card with clickable calendar
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
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
                                    text = "$currentMonthName Summary",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextBlack
                                )
                                Surface(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable {
                                            navController.navigate("category_totals")
                                        },
                                    shape = CircleShape,
                                    color = BrandGreen.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = "View Monthly Report",
                                            tint = BrandGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Stats row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                HomeStatItem(
                                    title = "Income",
                                    amount = totalMonthlyIncome,
                                    color = Color(0xFF43A047),
                                    icon = Icons.Default.TrendingUp
                                )
                                HomeStatItem(
                                    title = "Expenses",
                                    amount = totalMonthlyExpenses,
                                    color = Color(0xFFE53935),
                                    icon = Icons.Default.TrendingDown
                                )
                                HomeStatItem(
                                    title = "Net",
                                    amount = netMonthly,
                                    color = if (netMonthly >= 0) Color(0xFF43A047) else Color(0xFFE53935),
                                    icon = Icons.Default.AccountBalanceWallet
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
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandGreen.copy(alpha = 0.05f)),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = BrandGreen.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Today,
                                            contentDescription = null,
                                            tint = BrandGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
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
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreen
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
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Top Spending Categories",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextBlack
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                topCategories.forEach { (category, amount) ->
                                    HomeCategoryProgressRow(
                                        category = category,
                                        amount = amount,
                                        total = totalMonthlyExpenses,
                                        maxAmount = topCategories.firstOrNull()?.second ?: amount
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
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
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
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
                                        onClick = { navController.navigate("expenses") }
                                    ) {
                                        Text("View All", color = BrandGreen, fontSize = 12.sp)
                                    }
                                }

                                recentTransactions.forEach { transaction ->
                                    HomeRecentTransactionItem(transaction = transaction)
                                    if (transaction != recentTransactions.last()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
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
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Quick Actions",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextBlack
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HomeQuickActionButton(
                                    icon = Icons.Default.AddCircle,
                                    label = "Add Expense",
                                    onClick = { navController.navigate("add_transaction") },
                                    modifier = Modifier.weight(1f)
                                )
                                HomeQuickActionButton(
                                    icon = Icons.Default.Category,
                                    label = "Categories",
                                    onClick = { navController.navigate("categories") },
                                    modifier = Modifier.weight(1f)
                                )
                                HomeQuickActionButton(
                                    icon = Icons.Default.TrackChanges,
                                    label = "Budget Goals",
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

@Composable
fun HomeStatItem(
    title: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "R${String.format("%.2f", amount)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun HomeCategoryProgressRow(
    category: String,
    amount: Double,
    total: Double,
    maxAmount: Double
) {
    val percentage = if (total > 0) (amount / total) else 0.0
    val barWidth = if (maxAmount > 0) (amount / maxAmount).toFloat().coerceIn(0f, 1f) else 0f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = getHomeCategoryEmoji(category),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
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
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(BrandGreen)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${(percentage * 100).toInt()}% of total expenses",
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun HomeRecentTransactionItem(transaction: group12.budgetbuddi.data.entity.ExpenseEntity) {
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
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = BrandGreen.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = getHomeCategoryEmoji(transaction.category),
                        fontSize = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = transaction.category,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextBlack
                )
                Text(
                    text = formatHomeDate(transaction.date),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        Text(
            text = "${if (isExpense) "-" else "+"}R${String.format("%.2f", transaction.amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isExpense) Color(0xFFE53935) else Color(0xFF43A047)
        )
    }
}

@Composable
fun HomeQuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = BrandGreen.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = BrandGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextBlack
        )
    }
}

// Helper functions with unique names
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