// HomeScreen.kt - Fixed Icons with ContentDescription
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BrandGreen
import group12.budgetbuddi.ui.theme.TextBlack
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import group12.budgetbuddi.viewmodel.UserProfileViewModel
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

    val userProfileViewModel: UserProfileViewModel = viewModel()
    val uiState by userProfileViewModel.uiState.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            userProfileViewModel.setUserId(userId)
            userProfileViewModel.updateLoginStreak()
        }
    }

    val currentStreak = uiState.userProfile?.currentStreak ?: 0
    val longestStreak = uiState.userProfile?.longestStreak ?: 0
    val selectedAvatarId = uiState.userProfile?.selectedAvatarBadgeId
    val selectedAchievement = uiState.unlockedAchievements.find { it.id == selectedAvatarId }

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
                .background(Color(0xFFE8F5E9))
                .graphicsLayer(alpha = contentAlpha)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp), clip = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    color = BrandGreen
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        // Greeting and Profile
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Good ${getTimeOfDay()},",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.85f),
                                )
                                Text(
                                    text = displayName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                )
                            }

                            // Profile Picture with Avatar Support
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable {
                                        navController.navigate("user_profile") {
                                            launchSingleTop = true
                                        }
                                    },
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (selectedAchievement != null) {
                                        AsyncImage(
                                            model = selectedAchievement.badgeUrl,
                                            contentDescription = "Profile Avatar",
                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    } else {
                                        Text(
                                            text = displayName.take(1).uppercase(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Streak Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Current Streak
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(70.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Fireplace,
                                        contentDescription = "Current Streak Icon",
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "$currentStreak",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Day Streak",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            // Longest Streak
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(70.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Longest Streak Icon",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "$longestStreak",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Best Streak",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // XP Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Lvl ${uiState.currentLevel}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(uiState.levelTitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                    }
                                    Text("${uiState.currentXP} XP", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color.White.copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(uiState.levelProgress)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFFFD700))
                                    )
                                }

                                Text("${uiState.xpToNextLevel} XP to next level", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }

            // Main Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monthly Summary
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = "Calendar Icon",
                                        tint = BrandGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("$currentMonthName Summary", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBlack)
                                }
                                Surface(
                                    modifier = Modifier.size(32.dp).clickable { navController.navigate("category_totals") },
                                    shape = CircleShape,
                                    color = BrandGreen.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = "View Monthly Report",
                                            tint = BrandGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                SimpleStatItem("Income", totalMonthlyIncome, Color(0xFF43A047), Icons.Default.TrendingUp, "Income Icon")
                                SimpleStatItem("Expenses", totalMonthlyExpenses, Color(0xFFE53935), Icons.Default.TrendingDown, "Expenses Icon")
                                SimpleStatItem("Net", netMonthly, if (netMonthly >= 0) Color(0xFF43A047) else Color(0xFFE53935), Icons.Default.AccountBalanceWallet, "Net Icon")
                            }
                        }
                    }
                }

                // Today's Spending
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandGreen.copy(alpha = 0.05f))
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
                                            contentDescription = "Today Icon",
                                            tint = BrandGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Today's Spending", fontSize = 12.sp, color = Color.Gray)
                                    Text(formatHomeDate(today), fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            Text("R${String.format("%.2f", todayExpenses)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                        }
                    }
                }

                // Pie Chart
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Income vs Expenses", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
                            Spacer(modifier = Modifier.height(12.dp))
                            SimplePieChart(totalMonthlyIncome, totalMonthlyExpenses)
                        }
                    }
                }

                // Top Categories
                if (topCategories.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Top Spending Categories", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
                                Spacer(modifier = Modifier.height(12.dp))
                                topCategories.forEach { (category, amount) ->
                                    SimpleCategoryRow(category, amount, totalMonthlyExpenses)
                                    Spacer(modifier = Modifier.height(8.dp))
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
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Recent Transactions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
                                    TextButton(onClick = { navController.navigate("expense_list") }) {
                                        Text("View All", color = BrandGreen, fontSize = 11.sp)
                                    }
                                }
                                recentTransactions.take(3).forEach { transaction ->
                                    SimpleTransactionRow(transaction)
                                    Spacer(modifier = Modifier.height(8.dp))
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
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Quick Actions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SimpleActionButton(Icons.Default.AddCircle, "Add", "Add Transaction") { navController.navigate("add_transaction") }
                                SimpleActionButton(Icons.Default.Category, "Categories", "Manage Categories") { navController.navigate("categories") }
                                SimpleActionButton(Icons.Default.EmojiEvents, "Goals", "Budget Goals") { navController.navigate("budget_goals") }
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

@Composable
fun SimpleStatItem(title: String, amount: Double, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, iconDescription: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Icon(icon, contentDescription = iconDescription, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text("R${String.format("%.0f", amount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextBlack)
        Text(title, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun SimpleCategoryRow(category: String, amount: Double, total: Double) {
    val percentage = if (total > 0) (amount / total * 100).toInt() else 0
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(getHomeCategoryEmoji(category), fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(category, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextBlack)
            }
            Text("R${String.format("%.0f", amount)} ($percentage%)", fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(BrandGreen)
            )
        }
    }
}

@Composable
fun SimpleTransactionRow(transaction: group12.budgetbuddi.data.entity.ExpenseEntity) {
    val isExpense = transaction.type == "expense"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = BrandGreen.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(getHomeCategoryEmoji(transaction.category), fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(transaction.category, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextBlack)
                Text(formatHomeDate(transaction.date), fontSize = 10.sp, color = Color.Gray)
            }
        }
        Text(
            "${if (isExpense) "-" else "+"}R${String.format("%.2f", transaction.amount)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isExpense) Color(0xFFE53935) else Color(0xFF43A047)
        )
    }
}

@Composable
fun SimpleActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, description: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(Color(0xFFF0F0F0))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = description, tint = BrandGreen, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextBlack)
    }
}

@Composable
fun SimplePieChart(income: Double, expenses: Double) {
    val total = income + expenses
    if (total == 0.0) {
        Box(modifier = Modifier.height(120.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PieChart, contentDescription = "Pie Chart Icon", tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                Text("No data yet", fontSize = 12.sp, color = Color.Gray)
            }
        }
        return
    }

    val incomePercent = (income / total * 100).toInt()
    val expensePercent = (expenses / total * 100).toInt()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2 - 5
            val incomeAngle = (income / total * 360).toFloat()

            if (income > 0) {
                drawArc(
                    color = Color(0xFF43A047),
                    startAngle = -90f,
                    sweepAngle = incomeAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }
            if (expenses > 0) {
                drawArc(
                    color = Color(0xFFE53935),
                    startAngle = -90f + incomeAngle,
                    sweepAngle = (expenses / total * 360).toFloat(),
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF43A047)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Income $incomePercent%", fontSize = 10.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE53935)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Expenses $expensePercent%", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

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