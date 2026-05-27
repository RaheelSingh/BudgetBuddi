package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BackgroundWhite
import group12.budgetbuddi.ui.theme.PrimaryGreen
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

    // Delete confirmation dialog
    if (deleteDialog.isVisible) {
        AlertDialog(
            onDismissRequest = { budgetGoalViewModel.dismissDeleteDialog() },
            containerColor = BackgroundWhite,
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

    AppScaffold(
        navController = navController,
        selectedRoute = "category_totals",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Budget Goals",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )
                    Text(
                        text = "$monthName $year",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // Add goal button
                FloatingActionButton(
                    onClick = { navController.navigate("budget_goals") },
                    containerColor = PrimaryGreen,
                    contentColor = BackgroundWhite,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (listState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            } else if (listState.expenseGoals.isEmpty() && listState.incomeGoals.isEmpty()) {

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("💰", fontSize = 64.sp)
                        Text(
                            "No budget goals yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextBlack
                        )
                        Text(
                            "Tap + to add your first goal",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { navController.navigate("budget_goals") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Goal")
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    if (listState.expenseGoals.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Expense Goals")
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

                    if (listState.incomeGoals.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader(title = "Income Goals")
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

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}


@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = PrimaryGreen,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}


@Composable
fun ExpenseGoalCard(
    goalWithProgress: GoalWithProgress,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val goal = goalWithProgress.goal
    val fraction = goalWithProgress.progressFraction
    // Expense green when under budget, red when over
    val isOver = fraction > 1.0f
    val barColor = if (isOver) Color(0xFFE53935) else PrimaryGreen
    val statusText = if (isOver) "Over budget" else "Within budget"
    val statusColor = if (isOver) Color(0xFFE53935) else PrimaryGreen

    GoalCard(
        goal = goal,
        amountSpent = goalWithProgress.amountSpent,
        fraction = fraction,
        barColor = barColor,
        statusText = statusText,
        statusColor = statusColor,
        amountLabel = "Spent",
        limitLabel = "Max: R%.2f".format(goal.maxGoal),
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
    // Income: red when below target, green when reached/exceeded
    val isReached = fraction >= 1.0f
    val barColor = if (isReached) PrimaryGreen else Color(0xFFE53935)
    val statusText = if (isReached) "Goal reached!" else "Below target"
    val statusColor = if (isReached) PrimaryGreen else Color(0xFFE53935)

    GoalCard(
        goal = goal,
        amountSpent = goalWithProgress.amountSpent,
        fraction = fraction,
        barColor = barColor,
        statusText = statusText,
        statusColor = statusColor,
        amountLabel = "Earned",
        limitLabel = "Target: R%.2f".format(goal.minGoal),
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
    limitLabel: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, barColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            text = "🔁 Recurring",
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
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

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
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50.dp))
                        .background(barColor)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Amount and limit row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$amountLabel: R%.2f".format(amountSpent),
                    fontSize = 13.sp,
                    color = TextBlack,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = limitLabel,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = statusText,
                fontSize = 12.sp,
                color = statusColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}