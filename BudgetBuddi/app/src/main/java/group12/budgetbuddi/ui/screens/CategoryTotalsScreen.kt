package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.R
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.BudgetStatus
import group12.budgetbuddi.viewmodel.CategoryTotalItem
import group12.budgetbuddi.viewmodel.CategoryTotalsViewModel
import java.util.Calendar

private val BrandGreen = Color(0xFF4CAF50)
private val LightGreen = Color(0xFFE8F5E9)
private val WarningOrange = Color(0xFFFF9800)
private val ErrorRed = Color(0xFFF44336)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTotalsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    categoryTotalsViewModel: CategoryTotalsViewModel = viewModel()
) {
    val state by categoryTotalsViewModel.totalsUiState.collectAsState()
    val goalState by categoryTotalsViewModel.goalUiState.collectAsState()

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
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BrandGreen
                    )
                }

                Text(
                    text = "Budget Goals",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logo Image
            Image(
                painter = painterResource(id = R.drawable.bb_og),
                contentDescription = "Budget Buddi Logo",
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Budget Period Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LightGreen)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Budget Period",
                        fontSize = 12.sp,
                        color = BrandGreen,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date Range Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // From Date Button
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .clickable { showFromPicker = true }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "From",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = if (state.fromDate.isNotEmpty())
                                        formatDateForDisplay(state.fromDate)
                                    else "Select date",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (state.fromDate.isNotEmpty()) Color.Black else Color.LightGray
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = BrandGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // To Date Button
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .clickable { showToPicker = true }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "To",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = if (state.toDate.isNotEmpty())
                                        formatDateForDisplay(state.toDate)
                                    else "Select date",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (state.toDate.isNotEmpty()) Color.Black else Color.LightGray
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = BrandGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Grand Total Card
            GrandTotalCard(
                grandTotal = state.grandTotal,
                minGoal = state.minGoal,
                maxGoal = state.maxGoal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Budgets Section Title
            Text(
                text = "Category Budgets",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Category List
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandGreen)
                }
            } else if (state.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No expenses found for this period",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Try adjusting the date range",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.items) { item ->
                        CategoryBudgetCard(item = item)
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
        overBudget -> "Over Budget"
        underBudget -> "Under Budget"
        else -> "Within Budget"
    }

    val statusColor = when {
        overBudget -> ErrorRed
        underBudget -> WarningOrange
        else -> BrandGreen
    }

    val statusIcon = when {
        overBudget -> Icons.Default.Warning
        underBudget -> Icons.Default.Info
        else -> Icons.Default.CheckCircle
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreen)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Total Amount",
                fontSize = 12.sp,
                color = BrandGreen,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "R %.2f".format(grandTotal),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            if (minGoal > 0 || maxGoal > 0) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Target: R %.2f – R %.2f".format(minGoal, maxGoal),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetCard(item: CategoryTotalItem) {
    val indicatorColor = when (item.budgetStatus) {
        BudgetStatus.OVER -> ErrorRed
        BudgetStatus.UNDER -> WarningOrange
        BudgetStatus.OK -> BrandGreen
    }

    val statusLabel = when (item.budgetStatus) {
        BudgetStatus.OVER -> "Over Budget"
        BudgetStatus.UNDER -> "Under Budget"
        BudgetStatus.OK -> "On Track"
    }

    val statusIcon = when (item.budgetStatus) {
        BudgetStatus.OVER -> Icons.Default.Warning
        BudgetStatus.UNDER -> Icons.Default.Info
        BudgetStatus.OK -> Icons.Default.CheckCircle
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Category Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.categoryName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = indicatorColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = indicatorColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Budget Period Placeholder
            Text(
                text = "Budget Period",
                fontSize = 10.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Amount Allowed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Amount Allowed",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "R %.2f".format(item.totalSpent),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Status badge
                when (item.budgetStatus) {
                    BudgetStatus.OVER -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ErrorRed.copy(alpha = 0.1f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "Exceeded",
                                fontSize = 11.sp,
                                color = ErrorRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    BudgetStatus.UNDER -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarningOrange.copy(alpha = 0.1f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "Below Target",
                                fontSize = 11.sp,
                                color = WarningOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    BudgetStatus.OK -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandGreen.copy(alpha = 0.1f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "On Track",
                                fontSize = 11.sp,
                                color = BrandGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
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
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = BrandGreen
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year (YYYY)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandGreen,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Month (1–12)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandGreen,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = day,
                    onValueChange = { day = it },
                    label = { Text("Day") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandGreen,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val y = year.trim().toIntOrNull()
                    val m = month.trim().toIntOrNull()
                    val d = day.trim().toIntOrNull()
                    if (y == null || m == null || d == null || m !in 1..12 || d !in 1..31 || y < 2000) {
                        error = "Invalid date — please check your values"
                    } else {
                        val dateStr = "%04d-%02d-%02d".format(y, m, d)
                        onDateSelected(dateStr)
                    }
                }
            ) {
                Text("OK", color = BrandGreen, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}