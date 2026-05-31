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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun DailyReportScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val allTransactions by expenseViewModel.allExpenses.collectAsState()

    // Get today's date in yyyy-MM-dd format
    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Filter transactions for today only
    val todayTransactions = remember(allTransactions, today) {
        allTransactions.filter { it.date == today }
    }

    val todayExpenses = remember(todayTransactions) {
        todayTransactions.filter { it.type == "expense" }
    }

    val todayIncome = remember(todayTransactions) {
        todayTransactions.filter { it.type == "income" }
    }

    val totalExpenses = remember(todayExpenses) {
        todayExpenses.sumOf { it.amount }
    }

    val totalIncome = remember(todayIncome) {
        todayIncome.sumOf { it.amount }
    }

    val netTotal = totalIncome - totalExpenses

    // Group expenses by category for bar chart
    val expensesByCategory = remember(todayExpenses) {
        todayExpenses.groupBy { it.category }
            .map { (category, items) -> category to items.sumOf { it.amount } }
            .sortedByDescending { it.second }
    }

    val maxExpenseAmount = remember(expensesByCategory) {
        expensesByCategory.maxOfOrNull { it.second } ?: 1.0
    }

    val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val formattedDate = dateFormatter.format(Date())

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
        selectedRoute = "daily_report",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .graphicsLayer(alpha = contentAlpha)
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BrandGreen,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Daily Report",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedDate,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (todayTransactions.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
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
                            text = "No Transactions Today",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )

                        Text(
                            text = "You haven't added any expenses or income for today.\nTap the button below to get started.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )

                        Button(
                            onClick = {
                                navController.navigate("add_transaction") {
                                    launchSingleTop = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .width(200.dp)
                                .height(52.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Transaction", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Summary cards
                    item {
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DailySummaryCard(
                                title = "Expenses",
                                amount = totalExpenses,
                                icon = Icons.Default.TrendingDown,
                                iconColor = Color(0xFFE53935),
                                modifier = Modifier.weight(1f)
                            )

                            DailySummaryCard(
                                title = "Income",
                                amount = totalIncome,
                                icon = Icons.Default.TrendingUp,
                                iconColor = Color(0xFF43A047),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Net total card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (netTotal >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ),
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
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = if (netTotal >= 0) Color(0xFF43A047) else Color(0xFFE53935),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Net Total",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextBlack
                                    )
                                }
                                Text(
                                    text = "R${String.format("%.2f", kotlin.math.abs(netTotal))}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netTotal >= 0) Color(0xFF43A047) else Color(0xFFE53935)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Bar Graph Section (only if there are expenses)
                    if (expensesByCategory.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(3.dp)
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
                                            text = "Spending by Category",
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

                                    // Horizontal Bar Chart
                                    expensesByCategory.forEach { (category, amount) ->
                                        val percentage = (amount / maxExpenseAmount).toFloat()
                                        DailyBarChartRow(
                                            category = category,
                                            amount = amount,
                                            percentage = percentage,
                                            maxAmount = maxExpenseAmount
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Section title for transactions
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Transactions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextBlack
                            )
                            TextButton(
                                onClick = { navController.navigate("expense_list") }
                            ) {
                                Text("View All", color = BrandGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Transactions list
                    items(todayTransactions.sortedByDescending { it.createdAt }) { transaction ->
                        DailyTransactionItem(
                            transaction = transaction,
                            onClick = { }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun DailySummaryCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
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
                    text = title,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "R${String.format("%.2f", amount)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
        }
    }
}

@Composable
fun DailyBarChartRow(
    category: String,
    amount: Double,
    percentage: Float,
    maxAmount: Double
) {
    // Animated bar width
    val animatedWidth by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "barWidth"
    )

    val barColor = when (category.lowercase()) {
        "food", "groceries", "restaurant" -> Color(0xFFFF9800)
        "transport", "car", "fuel" -> Color(0xFF2196F3)
        "shopping", "clothing" -> Color(0xFF9C27B0)
        "entertainment", "movies" -> Color(0xFFE91E63)
        "bills", "utilities" -> Color(0xFF607D8B)
        "health", "medical" -> Color(0xFF4CAF50)
        "education" -> Color(0xFF3F51B5)
        else -> BrandGreen
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = getDailyCategoryEmoji(category),
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

        Spacer(modifier = Modifier.height(4.dp))

        // Bar background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF0F0F0))
        ) {
            // Animated bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(barColor)
            )

            // Percentage text overlay
            if (percentage > 0.15f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else if (percentage > 0.05f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun DailyTransactionItem(
    transaction: ExpenseEntity,
    onClick: () -> Unit
) {
    val isExpense = transaction.type == "expense"
    val amountColor = if (isExpense) Color(0xFFE53935) else Color(0xFF43A047)
    val amountPrefix = if (isExpense) "-R" else "+R"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                            text = getDailyCategoryEmoji(transaction.category),
                            fontSize = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.category,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextBlack
                    )
                    if (transaction.subCategory.isNotBlank()) {
                        Text(
                            text = transaction.subCategory,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = transaction.paymentMethod,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${String.format("%.2f", transaction.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note.take(20),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

fun getDailyCategoryEmoji(category: String): String {
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
        "salary", "income" -> "💰"
        else -> if (category.lowercase() == "income") "💰" else "💸"
    }
}