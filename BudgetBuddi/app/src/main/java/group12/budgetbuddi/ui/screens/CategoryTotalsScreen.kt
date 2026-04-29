package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.BudgetStatus
import group12.budgetbuddi.viewmodel.CategoryTotalItem
import group12.budgetbuddi.viewmodel.CategoryTotalsViewModel
import java.util.Calendar
fun formatDateForDisplay(dateStr: String): String {
    if (dateStr.length < 10) return dateStr
    val parts = dateStr.split("-")
    if (parts.size < 3) return dateStr
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val monthIndex = parts[1].toIntOrNull()?.minus(1) ?: return dateStr
    val monthName = monthNames.getOrElse(monthIndex) { parts[1] }
    return "${parts[2]} $monthName ${parts[0]}"
}

@Composable
fun CategoryTotalsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    categoryTotalsViewModel: CategoryTotalsViewModel = viewModel()
) {
    val state by categoryTotalsViewModel.totalsUiState.collectAsState()

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    if (showFromPicker) {
        SimpleDatePickerDialog(
            title = "Select Start Date",
            initialDateStr = state.fromDate,
            onDateSelected = { date ->
                categoryTotalsViewModel.setFromDate(date)
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false }
        )
    }

    if (showToPicker) {
        SimpleDatePickerDialog(
            title = "Select End Date",
            initialDateStr = state.toDate,
            onDateSelected = { date ->
                categoryTotalsViewModel.setToDate(date)
                showToPicker = false
            },
            onDismiss = { showToPicker = false }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Category Totals",
                style = MaterialTheme.typography.headlineMedium
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Filter Period", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showFromPicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.fromDate.isNotEmpty())
                                    formatDateForDisplay(state.fromDate)
                                else "From",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        OutlinedButton(
                            onClick = { showToPicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.toDate.isNotEmpty())
                                    formatDateForDisplay(state.toDate)
                                else "To",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            GrandTotalCard(
                grandTotal = state.grandTotal,
                minGoal = state.minGoal,
                maxGoal = state.maxGoal
            )
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No expenses found for this period",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Try adjusting the date range",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.items) { item ->
                        CategoryTotalRow(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun GrandTotalCard(grandTotal: Double, minGoal: Double, maxGoal: Double) {
    val overBudget = maxGoal > 0 && grandTotal > maxGoal
    val underBudget = minGoal > 0 && grandTotal < minGoal

    val statusText = when {
        overBudget -> "Over maximum goal!"
        underBudget -> "Below minimum goal"
        else -> "Within budget"
    }
    val statusColor = when {
        overBudget -> MaterialTheme.colorScheme.error
        underBudget -> Color(0xFFF57C00)
        else -> Color(0xFF388E3C)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Spending", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "R %.2f".format(grandTotal),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (minGoal > 0 || maxGoal > 0) {
                Text(
                    text = "Goal: R %.2f – R %.2f".format(minGoal, maxGoal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
fun CategoryTotalRow(item: CategoryTotalItem) {
    val indicatorColor = when (item.budgetStatus) {
        BudgetStatus.OVER -> MaterialTheme.colorScheme.error
        BudgetStatus.UNDER -> Color(0xFFF57C00)
        BudgetStatus.OK -> Color(0xFF388E3C)
    }
    val statusLabel = when (item.budgetStatus) {
        BudgetStatus.OVER -> "Over"
        BudgetStatus.UNDER -> "Under"
        BudgetStatus.OK -> "OK"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(48.dp)
                    .background(indicatorColor, shape = MaterialTheme.shapes.small)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.categoryName, style = MaterialTheme.typography.titleMedium)
                Text(
                    statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = indicatorColor
                )
            }
            Text(
                "R %.2f".format(item.totalSpent),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun SimpleDatePickerDialog(
    title: String,
    initialDateStr: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = initialDateStr.split("-")
    var year by remember { mutableStateOf(parts.getOrElse(0) { Calendar.getInstance().get(Calendar.YEAR).toString() }) }
    var month by remember { mutableStateOf(parts.getOrElse(1) { "01" }.trimStart('0').ifEmpty { "1" }) }
    var day by remember { mutableStateOf(parts.getOrElse(2) { "01" }.trimStart('0').ifEmpty { "1" }) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year (YYYY)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Month (1–12)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = day,
                    onValueChange = { day = it },
                    label = { Text("Day") },
                    singleLine = true
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val y = year.trim().toIntOrNull()
                val m = month.trim().toIntOrNull()
                val d = day.trim().toIntOrNull()
                if (y == null || m == null || d == null || m !in 1..12 || d !in 1..31 || y < 2000) {
                    error = "Invalid date — please check your values"
                } else {
                    val dateStr = "%04d-%02d-%02d".format(y, m, d)
                    onDateSelected(dateStr)
                }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}