// HomeScreen.kt - Fixed Icons with ContentDescription
package group12.budgetbuddi.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import group12.budgetbuddi.data.AchievementsData
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BrandGreenDark as BudgetGreen
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

    val weekRange = remember(today) {
        val mondayCal = Calendar.getInstance()
        mondayCal.firstDayOfWeek = Calendar.MONDAY
        while (mondayCal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            mondayCal.add(Calendar.DAY_OF_MONTH, -1)
        }

        val saturdayCal = mondayCal.clone() as Calendar
        saturdayCal.add(Calendar.DAY_OF_MONTH, 5)

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        format.format(mondayCal.time) to format.format(saturdayCal.time)
    }

    val weekStart = weekRange.first
    val weekEnd = weekRange.second

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

    val weekExpenses = remember(allTransactions, weekStart, weekEnd) {
        allTransactions.filter {
            it.type == "expense" &&
                    it.date >= weekStart &&
                    it.date <= weekEnd
        }
    }

    val topCategoriesThisWeek = remember(weekExpenses) {
        weekExpenses.groupBy { it.category }
            .map { (category, expenses) -> category to expenses.sumOf { it.amount } }
            .sortedByDescending { it.second }
            .take(3)
    }

    val totalWeeklyExpenses = remember(weekExpenses) {
        weekExpenses.sumOf { it.amount }
    }

    val recentTransactions = remember(allTransactions) {
        allTransactions.sortedWith(
            compareByDescending<group12.budgetbuddi.data.entity.ExpenseEntity> { it.createdAt }
                .thenByDescending { it.updatedAt }
        ).take(5)
    }

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "contentAlpha"
    )

    val homeListState = rememberLazyListState()
    val rawCollapseProgress by remember {
        derivedStateOf {
            if (homeListState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (homeListState.firstVisibleItemScrollOffset / 190f).coerceIn(0f, 1f)
            }
        }
    }

    val collapseProgress by animateFloatAsState(
        targetValue = rawCollapseProgress,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "homeHeaderCollapseProgress"
    )

    val expandedAlpha = 1f - collapseProgress
    val compactAlpha = collapseProgress

    val headerVerticalPadding = (16f - (6f * collapseProgress)).dp
    val avatarSize = (48f - (6f * collapseProgress)).dp
    val greetingFontSize = 14f - (2f * collapseProgress)
    val nameFontSize = 22f - (4f * collapseProgress)
    val avatarTextFontSize = 20f - (2f * collapseProgress)
    val headerSpacerHeight = (12f - (4f * collapseProgress)).dp
    val expandedStatsHeight = (150f * expandedAlpha).dp
    val compactStatsHeight = (52f * compactAlpha).dp

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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp), clip = false),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                color = BudgetGreen
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = headerVerticalPadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Good ${getTimeOfDay()},",
                                fontSize = greetingFontSize.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = displayName,
                                fontSize = nameFontSize.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .size(avatarSize)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable {
                                    navController.navigate("user_profile") { launchSingleTop = true }
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
                                        fontSize = avatarTextFontSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(headerSpacerHeight))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(compactStatsHeight)
                            .clip(RoundedCornerShape(12.dp))
                            .graphicsLayer(alpha = compactAlpha)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HomeHeaderMiniStat(Icons.Default.Fireplace, Color(0xFFFF9800), currentStreak.toString(), "Streak", Modifier.weight(1f))
                            HomeHeaderMiniStat(Icons.Default.Star, Color(0xFFFFD700), longestStreak.toString(), "Best", Modifier.weight(1f))
                            HomeHeaderMiniStat(Icons.Default.EmojiEvents, Color(0xFFFFD700), "Lvl ${uiState.currentLevel}", uiState.levelTitle, Modifier.weight(1f))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(expandedStatsHeight)
                            .clip(RoundedCornerShape(12.dp))
                            .graphicsLayer(alpha = expandedAlpha)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f).height(70.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Fireplace, contentDescription = "Current Streak Icon", tint = Color(0xFFFF9800), modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("$currentStreak", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("Day Streak", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f).height(70.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = "Longest Streak Icon", tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("$longestStreak", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("Best Streak", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    val nextLevelXP = AchievementsData.getXpRequiredForLevel(uiState.currentLevel + 1)
                                    val safeXpProgress = if (nextLevelXP > 0) {
                                        (uiState.currentXP.toFloat() / nextLevelXP.toFloat()).coerceIn(0f, 1f)
                                    } else {
                                        1f
                                    }
                                    val xpTargetText = if (nextLevelXP > 0) {
                                        "${uiState.currentXP} / $nextLevelXP XP"
                                    } else {
                                        "${uiState.currentXP} XP"
                                    }
                                    val xpToNextText = if (uiState.xpToNextLevel > 0) {
                                        "${uiState.xpToNextLevel} XP to next level"
                                    } else {
                                        "Max level reached"
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Lvl ${uiState.currentLevel}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(uiState.levelTitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                        }
                                        Text(
                                            text = xpTargetText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    WhiteXpProgressBar(
                                        progress = safeXpProgress,
                                        modifier = Modifier.height(6.dp)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = xpToNextText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Main Content
            LazyColumn(
                state = homeListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Today's Spending
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                    color = BudgetGreen.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Today,
                                            contentDescription = "Today Icon",
                                            tint = BudgetGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Today's Spending", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBlack)
                                    Text(formatHomeDate(today), fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            Text("R${String.format("%.2f", todayExpenses)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BudgetGreen)
                        }
                    }
                }

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
                                        tint = BudgetGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("$currentMonthName Summary", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBlack)
                                }
                                Surface(
                                    modifier = Modifier.size(32.dp).clickable { navController.navigate("budget_report") },
                                    shape = CircleShape,
                                    color = BudgetGreen.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = "View Monthly Report",
                                            tint = BudgetGreen,
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
                            Text("Income vs Expenses (This Month)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                SimplePieChart(totalMonthlyIncome, totalMonthlyExpenses)
                            }
                        }
                    }
                }

                // Top Categories This Week
                if (topCategoriesThisWeek.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Top Spending Categories This Week", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
                                Spacer(modifier = Modifier.height(12.dp))
                                TopWeeklyCategoryCards(
                                    categories = topCategoriesThisWeek,
                                    totalWeeklyExpenses = totalWeeklyExpenses
                                )
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
                                        Text("View All", color = BudgetGreen, fontSize = 11.sp)
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
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SimpleActionButton(Icons.Default.AddCircle, "Add", "Add Transaction") { navController.navigate("add_transaction") }
                                SimpleActionButton(Icons.Default.Category, "Categories", "Manage Categories") { navController.navigate("categories") }
                                SimpleActionButton(Icons.Default.EmojiEvents, "Goals", "Budget Goals") { navController.navigate("category_totals") }
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
        val amountText = if (amount < 0) {
            "-R${String.format("%.0f", kotlin.math.abs(amount))}"
        } else {
            "R${String.format("%.0f", amount)}"
        }
        Text(amountText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextBlack)
        Text(title, fontSize = 10.sp, color = Color.Gray)
    }
}


@Composable
fun TopWeeklyCategoryCards(
    categories: List<Pair<String, Double>>,
    totalWeeklyExpenses: Double
) {
    val paddedCategories = categories.take(3)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        paddedCategories.forEach { (category, amount) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = getHomeCategoryEmoji(category),
                    fontSize = 26.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = category,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "R${String.format("%.0f", amount)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BudgetGreen
                )

            }
        }

        repeat(3 - paddedCategories.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
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
                    .background(BudgetGreen)
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
                color = BudgetGreen.copy(alpha = 0.1f)
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
        Icon(icon, contentDescription = description, tint = BudgetGreen, modifier = Modifier.size(22.dp))
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

@Composable
private fun WhiteXpProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val safeProgress = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(safeProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White)
        )
    }
}

@Composable
private fun HomeHeaderMiniStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                Text(label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.75f), maxLines = 1)
            }
        }
    }
}
