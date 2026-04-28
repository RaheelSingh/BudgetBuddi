package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.data.entity.ExpenseEntity
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

private val BrandGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    AppScaffold(
        navController = navController,
        selectedRoute = "expenses",
        authViewModel = authViewModel
    ) { modifier ->

        val expenses by expenseViewModel.expensesOnly.collectAsState()
        val allCategories by categoryViewModel.allCategories.collectAsState()

        // Selected category for drill-down (null = overview)
        var selectedCategory by remember { mutableStateOf<String?>(null) }

        // Date range filter state
        var fromDate by remember { mutableStateOf("") }
        var toDate by remember { mutableStateOf("") }
        var showFromDatePicker by remember { mutableStateOf(false) }
        var showToDatePicker by remember { mutableStateOf(false) }

        // Filter expenses by date range
        val filteredExpenses = remember(expenses, fromDate, toDate) {
            expenses.filter { expense ->
                val afterFrom = fromDate.isBlank() || expense.date >= fromDate
                val beforeTo = toDate.isBlank() || expense.date <= toDate
                afterFrom && beforeTo
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
        ) {

            // ── Top bar ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (selectedCategory != null) {
                    IconButton(onClick = { selectedCategory = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandGreen)
                    }
                } else {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandGreen)
                    }
                }

                Text(
                    text = selectedCategory ?: "Expenses",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Add expense shortcut
                IconButton(onClick = { navController.navigate("add_transaction") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = BrandGreen)
                }
            }

            // ── Date range filter row ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // From date
                Row(
                    modifier = Modifier
                        .clickable { showFromDatePicker = true }
                        .border(1.dp, BrandGreen, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (fromDate.isBlank()) "From" else formatDisplayDate(fromDate),
                        fontSize = 11.sp,
                        color = BrandGreen
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // To date
                Row(
                    modifier = Modifier
                        .clickable { showToDatePicker = true }
                        .border(1.dp, BrandGreen, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (toDate.isBlank()) "To" else formatDisplayDate(toDate),
                        fontSize = 11.sp,
                        color = BrandGreen
                    )
                }

                if (fromDate.isNotBlank() || toDate.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { fromDate = ""; toDate = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear filter", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Divider(color = Color.LightGray)

            if (filteredExpenses.isEmpty()) {
                // Empty state
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No expenses yet", color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { navController.navigate("add_transaction") }) {
                            Text("Add your first expense", color = BrandGreen)
                        }
                    }
                }
            } else if (selectedCategory == null) {
                // ── Overview: grouped by category ────────────────────
                val grouped = filteredExpenses.groupBy { it.category }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Categories header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Categories", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
                        }
                    }

                    grouped.forEach { (mainCat, catExpenses) ->
                        val total = catExpenses.sumOf { it.amount }

                        // Category header row
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(mainCat, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Total",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFFF3E0))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "R%.2f".format(total),
                                            color = Color(0xFFE65100),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Sub-category rows
                        val subGrouped = catExpenses.groupBy { it.subCategory }
                        subGrouped.forEach { (subCat, subExpenses) ->
                            val subTotal = subExpenses.sumOf { it.amount }
                            val emoji = allCategories.find {
                                it.mainCategory == mainCat && it.subCategory == subCat
                            }?.visualValue ?: "💰"

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCategory = mainCat
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Emoji icon circle
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .border(1.5.dp, BrandGreen, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(emoji, fontSize = 16.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(subCat, fontSize = 14.sp)
                                    }
                                    Text(
                                        text = "R%.2f".format(subTotal),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Divider(color = Color(0xFFEEEEEE))
                            }
                        }
                    }
                }
            } else {
                // ── Drill-down: expenses for a specific category ──────
                val categoryExpenses = filteredExpenses.filter { it.category == selectedCategory }
                val monthYear = categoryExpenses.firstOrNull()?.date?.let {
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val d = sdf.parse(it)
                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(d!!)
                    } catch (e: Exception) { "" }
                } ?: ""

                // Sub-heading
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(monthYear, fontSize = 13.sp, color = Color.Gray)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(categoryExpenses) { expense ->
                        ExpenseListItem(expense = expense)
                        Divider(color = Color(0xFFEEEEEE))
                    }
                }
            }
        }

        // ── Date pickers ─────────────────────────────────────────────
        if (showFromDatePicker) {
            val state = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showFromDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let { millis ->
                            fromDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                        }
                        showFromDatePicker = false
                    }) { Text("OK", color = BrandGreen) }
                },
                dismissButton = {
                    TextButton(onClick = { showFromDatePicker = false }) { Text("Cancel") }
                }
            ) { DatePicker(state = state) }
        }

        if (showToDatePicker) {
            val state = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showToDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let { millis ->
                            toDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                        }
                        showToDatePicker = false
                    }) { Text("OK", color = BrandGreen) }
                },
                dismissButton = {
                    TextButton(onClick = { showToDatePicker = false }) { Text("Cancel") }
                }
            ) { DatePicker(state = state) }
        }
    }
}

@Composable
private fun ExpenseListItem(expense: ExpenseEntity) {
    val displayDate = remember(expense.date) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val d = sdf.parse(expense.date)
            SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(d!!)
        } catch (e: Exception) { expense.date }
    }

    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(displayDate, fontSize = 11.sp, color = Color.Gray)
            Text(
                text = "-R%.2f".format(expense.amount),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(expense.category, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(1.dp, BrandGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("💰", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(expense.subCategory, fontSize = 13.sp, color = Color.DarkGray)
            if (!expense.photoPath.isNullOrBlank()) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Image, contentDescription = "Has photo", tint = BrandGreen, modifier = Modifier.size(18.dp))
            }
        }
        if (expense.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(expense.note, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

private fun formatDisplayDate(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val d = sdf.parse(dateStr) ?: return dateStr
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(d)
    } catch (e: Exception) { dateStr }
}