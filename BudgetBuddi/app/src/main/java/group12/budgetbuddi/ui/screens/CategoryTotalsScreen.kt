// CategoryTotalsScreen.kt - Enhanced with charts and progress
package group12.budgetbuddi.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import group12.budgetbuddi.viewmodel.BudgetGoalViewModel
import group12.budgetbuddi.viewmodel.GoalWithProgress
import java.util.Calendar

@Composable
fun CategoryTotalsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    budgetGoalViewModel: BudgetGoalViewModel = viewModel()
) {
    val listState by budgetGoalViewModel.listUiState.collectAsState()
    val deleteDialog by budgetGoalViewModel.deleteDialogState.collectAsState()

    val cal = Calendar.getInstance()
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthName = monthNames.getOrElse(cal.get(Calendar.MONTH)) { "" }
    val year = cal.get(Calendar.YEAR)

    // Animation
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "contentAlpha"
    )

    // Delete confirmation dialog
    if (deleteDialog.isVisible) {
        AlertDialog(
            onDismissRequest = { budgetGoalViewModel.dismissDeleteDialog() },
            containerColor = BackgroundWhite,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Delete Goal",
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${deleteDialog.goalName}\"?",
                    color = TextBlack
                )
            },
            confirmButton = {
                TextButton(onClick = { budgetGoalViewModel.confirmDelete() }) {
                    Text("Delete", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { budgetGoalViewModel.dismissDeleteDialog() }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Calculate overall progress
    val totalSpent = listState.expenseGoals.sumOf { it.amountSpent }
    val totalBudget = listState.expenseGoals.sumOf { it.goal.maxGoal }
    val overallProgress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else 0f

    val totalEarned = listState.incomeGoals.sumOf { it.amountSpent }
    val totalTarget = listState.incomeGoals.sumOf { it.goal.minGoal }
    val incomeProgress = if (totalTarget > 0) (totalEarned / totalTarget).toFloat().coerceIn(0f, 1f) else 0f

    AppScaffold(
        navController = navController,
        selectedRoute = "category_totals",
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
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "Budget Report",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$monthName $year",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            if (listState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandGreen)
                }
            } else if (listState.expenseGoals.isEmpty() && listState.incomeGoals.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = BrandGreen.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📊", fontSize = 48.sp)
                            }
                        }
                        Text(
                            text = "No Budget Goals Yet",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )
                        Text(
                            text = "Set up budget goals to track your spending and income",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { navController.navigate("budget_goals") },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Your First Goal")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Overall Progress Cards
                    if (listState.expenseGoals.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = "Budget Overview",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextBlack
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Overall Spending vs Budget
                                    Text(
                                        text = "Overall Spending",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "R${String.format("%.2f", totalSpent)} / R${String.format("%.2f", totalBudget)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextBlack
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.LightGray.copy(alpha = 0.3f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(overallProgress)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (overallProgress > 0.9f) Color(0xFFE53935) else BrandGreen
                                                )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Income Progress
                                    if (listState.incomeGoals.isNotEmpty()) {
                                        Text(
                                            text = "Income Progress",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "R${String.format("%.2f", totalEarned)} / R${String.format("%.2f", totalTarget)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextBlack
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(12.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.LightGray.copy(alpha = 0.3f))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(incomeProgress)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (incomeProgress >= 1f) BrandGreen else Color(0xFFFF9800)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Expense Goals Section
                    if (listState.expenseGoals.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Expense Goals", icon = Icons.Default.TrendingDown)
                        }
                        items(listState.expenseGoals) { goalWithProgress ->
                            ExpenseGoalCard(
                                goalWithProgress = goalWithProgress,
                                onEdit = {
                                    budgetGoalViewModel.startEditing(goalWithProgress)
                                    navController.navigate("budget_goals")
                                },
                                onDelete = {
                                    budgetGoalViewModel.showDeleteDialog(
                                        goalId = goalWithProgress.goal.id,
                                        goalName = goalWithProgress.goal.goalName
                                    )
                                }
                            )
                        }
                    }

                    // Income Goals Section
                    if (listState.incomeGoals.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader(title = "Income Goals", icon = Icons.Default.TrendingUp)
                        }
                        items(listState.incomeGoals) { goalWithProgress ->
                            IncomeGoalCard(
                                goalWithProgress = goalWithProgress,
                                onEdit = {
                                    budgetGoalViewModel.startEditing(goalWithProgress)
                                    navController.navigate("budget_goals")
                                },
                                onDelete = {
                                    budgetGoalViewModel.showDeleteDialog(
                                        goalId = goalWithProgress.goal.id,
                                        goalName = goalWithProgress.goal.goalName
                                    )
                                }
                            )
                        }
                    }

                    // Add New Goal Button
                    item {
                        Button(
                            onClick = { navController.navigate("budget_goals") },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(52.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add New Goal", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = BrandGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack
        )
    }
}

@Composable
fun ExpenseGoalCard(
    goalWithProgress: GoalWithProgress,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val goal = goalWithProgress.goal
    val fraction = goalWithProgress.progressFraction
    val isOver = fraction > 1.0f
    val barColor = if (isOver) Color(0xFFE53935) else BrandGreen
    val statusText = if (isOver) "Over budget!" else if (fraction > 0.8f) "Approaching limit" else "On track"
    val statusColor = if (isOver) Color(0xFFE53935) else if (fraction > 0.8f) Color(0xFFFF9800) else BrandGreen

    GoalCard(
        goal = goal,
        amountSpent = goalWithProgress.amountSpent,
        fraction = fraction,
        barColor = barColor,
        statusText = statusText,
        statusColor = statusColor,
        amountLabel = "Spent",
        limitLabel = "Max: R%.2f".format(goal.maxGoal),
        targetLabel = null,
        onEdit = onEdit,
        onDelete = onDelete
    )
}

@Composable
fun IncomeGoalCard(
    goalWithProgress: GoalWithProgress,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val goal = goalWithProgress.goal
    val fraction = goalWithProgress.progressFraction
    val isReached = fraction >= 1.0f
    val barColor = if (isReached) BrandGreen else Color(0xFFFF9800)
    val statusText = if (isReached) "Goal reached! 🎉" else if (fraction > 0.7f) "Almost there!" else "In progress"
    val statusColor = if (isReached) BrandGreen else Color(0xFFFF9800)

    GoalCard(
        goal = goal,
        amountSpent = goalWithProgress.amountSpent,
        fraction = fraction,
        barColor = barColor,
        statusText = statusText,
        statusColor = statusColor,
        amountLabel = "Earned",
        limitLabel = null,
        targetLabel = "Target: R%.2f".format(goal.minGoal),
        onEdit = onEdit,
        onDelete = onDelete
    )
}

@Composable
fun GoalCard(
    goal: group12.budgetbuddi.data.entity.BudgetGoalEntity,
    amountSpent: Double,
    fraction: Float,
    barColor: Color,
    statusText: String,
    statusColor: Color,
    amountLabel: String,
    limitLabel: String?,
    targetLabel: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.goalName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextBlack
                    )
                    if (goal.isRecurring) {
                        Text(
                            text = "🔁 Recurring monthly",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0))
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = BrandGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF0F0))
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(5.dp))
                        .background(barColor)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Amount and limit row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$amountLabel: R${String.format("%.2f", amountSpent)}",
                    fontSize = 14.sp,
                    color = TextBlack,
                    fontWeight = FontWeight.Medium
                )
                limitLabel?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                targetLabel?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(fraction * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = barColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}