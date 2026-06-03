package group12.budgetbuddi.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BackgroundWhite
import group12.budgetbuddi.ui.theme.BrandGreenDark as BudgetGreen
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
    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val monthName = monthNames.getOrElse(cal.get(Calendar.MONTH)) { "" }
    val year = cal.get(Calendar.YEAR)

    if (deleteDialog.isVisible) {
        AlertDialog(
            onDismissRequest = { budgetGoalViewModel.dismissDeleteDialog() },
            containerColor = BackgroundWhite,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Delete Goal", fontWeight = FontWeight.Bold, color = TextBlack) },
            text = { Text("Are you sure you want to delete \"${deleteDialog.goalName}\"?", color = TextBlack) },
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

    AppScaffold(
        navController = navController,
        selectedRoute = "category_totals",
        authViewModel = authViewModel
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E9))
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BudgetGreen,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Budget Goals", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("$monthName $year", fontSize = 14.sp, color = Color.White.copy(alpha = 0.82f))
                    }
                    IconButton(
                        onClick = { navController.navigate("budget_goals") },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(48.dp)
                            .height(38.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = BudgetGreen, modifier = Modifier.size(25.dp))
                    }
                }
            }

            when {
                listState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BudgetGreen)
                    }
                }
                listState.expenseGoals.isEmpty() && listState.incomeGoals.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = BudgetGreen.copy(alpha = 0.1f)) {
                                Box(contentAlignment = Alignment.Center) { Text("🎯", fontSize = 48.sp) }
                            }
                            Text("No Budget Goals Yet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextBlack)
                            Text("Add a goal to track your monthly spending or income.", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            Button(onClick = { navController.navigate("budget_goals") }, colors = ButtonDefaults.buttonColors(containerColor = BudgetGreen), shape = RoundedCornerShape(50.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Goal")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (listState.expenseGoals.isNotEmpty()) {
                            item { BudgetGoalSectionHeader(title = "Expense Goals", icon = Icons.Default.TrendingDown) }
                            items(listState.expenseGoals) { goalWithProgress ->
                                ExpenseGoalCard(
                                    goalWithProgress = goalWithProgress,
                                    onEdit = {
                                        budgetGoalViewModel.startEditing(goalWithProgress)
                                        navController.navigate("budget_goals")
                                    },
                                    onDelete = {
                                        budgetGoalViewModel.showDeleteDialog(goalWithProgress.goal.id, goalWithProgress.goal.goalName)
                                    }
                                )
                            }
                        }

                        if (listState.incomeGoals.isNotEmpty()) {
                            item { BudgetGoalSectionHeader(title = "Income Goals", icon = Icons.Default.TrendingUp) }
                            items(listState.incomeGoals) { goalWithProgress ->
                                IncomeGoalCard(
                                    goalWithProgress = goalWithProgress,
                                    onEdit = {
                                        budgetGoalViewModel.startEditing(goalWithProgress)
                                        navController.navigate("budget_goals")
                                    },
                                    onDelete = {
                                        budgetGoalViewModel.showDeleteDialog(goalWithProgress.goal.id, goalWithProgress.goal.goalName)
                                    }
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
fun BudgetGoalSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BudgetGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlack)
    }
}

@Composable
fun ExpenseGoalCard(goalWithProgress: GoalWithProgress, onEdit: () -> Unit, onDelete: () -> Unit) {
    val goal = goalWithProgress.goal
    val fraction = goalWithProgress.progressFraction
    val isOver = fraction > 1.0f
    GoalCard(
        goal = goal,
        amountSpent = goalWithProgress.amountSpent,
        fraction = fraction,
        barColor = if (isOver) Color(0xFFE53935) else BudgetGreen,
        statusText = if (isOver) "Over budget" else if (fraction > 0.8f) "Approaching limit" else "On track",
        statusColor = if (isOver) Color(0xFFE53935) else if (fraction > 0.8f) Color(0xFFFF9800) else BudgetGreen,
        amountLabel = "Spent",
        limitLabel = "Max: R%.2f".format(goal.maxGoal),
        targetLabel = null,
        onEdit = onEdit,
        onDelete = onDelete
    )
}

@Composable
fun IncomeGoalCard(goalWithProgress: GoalWithProgress, onEdit: () -> Unit, onDelete: () -> Unit) {
    val goal = goalWithProgress.goal
    val fraction = goalWithProgress.progressFraction
    val isReached = fraction >= 1.0f
    GoalCard(
        goal = goal,
        amountSpent = goalWithProgress.amountSpent,
        fraction = fraction,
        barColor = if (isReached) BudgetGreen else Color(0xFFFF9800),
        statusText = if (isReached) "Goal reached" else if (fraction > 0.7f) "Almost there" else "In progress",
        statusColor = if (isReached) BudgetGreen else Color(0xFFFF9800),
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.goalName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
                    if (goal.isRecurring) Text("Repeats monthly", fontSize = 11.sp, color = Color.Gray)
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFF0F0F0))) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BudgetGreen, modifier = Modifier.size(17.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFFFF0F0))) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935), modifier = Modifier.size(17.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFE0E0E0))) {
                Box(modifier = Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(barColor))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("$amountLabel: R${String.format("%.2f", amountSpent)}", fontSize = 14.sp, color = TextBlack, fontWeight = FontWeight.Medium)
                Text(limitLabel ?: targetLabel ?: "", fontSize = 13.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(statusText, fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.Medium)
                Text("${(fraction * 100).toInt()}%", fontSize = 12.sp, color = barColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}
