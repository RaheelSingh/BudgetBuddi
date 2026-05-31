package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.HamburgerMenu
import group12.budgetbuddi.ui.theme.BrandGreen
import group12.budgetbuddi.ui.theme.BrandGreenDark
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySummaryScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val context = LocalContext.current
    val allExpenses by expenseViewModel.allExpenses.collectAsState()

    // Drawer state for hamburger menu
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Filter to show only expenses (not income)
    val expensesOnly = remember(allExpenses) {
        allExpenses.filter { it.type == "expense" }
    }

    // Date filter state
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Category selection state
    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showCategorySelector by remember { mutableStateOf(false) }

    val dateDisplayFormat = SimpleDateFormat("MM/dd/yyyy", LocalLocale.current.platformLocale)

    // Get all unique categories from expenses
    val allCategories = remember(expensesOnly) {
        expensesOnly.map { it.category }.distinct().sorted()
    }

    // Initialize selected categories to all when data loads
    LaunchedEffect(allCategories) {
        if (selectedCategories.isEmpty() && allCategories.isNotEmpty()) {
            selectedCategories = allCategories.toSet()
        }
    }

    // Filter expenses by date range
    val filteredExpenses = remember(expensesOnly, startDate, endDate) {
        var filtered = expensesOnly

        startDate?.let { start ->
            filtered = filtered.filter { expense ->
                try {
                    val expenseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expense.date)
                    expenseDate != null && expenseDate >= start
                } catch (e: Exception) { false }
            }
        }

        endDate?.let { end ->
            filtered = filtered.filter { expense ->
                try {
                    val expenseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expense.date)
                    expenseDate != null && expenseDate <= end
                } catch (e: Exception) { false }
            }
        }

        filtered
    }

    // Further filter by selected categories
    val displayExpenses = remember(filteredExpenses, selectedCategories) {
        filteredExpenses.filter { selectedCategories.contains(it.category) }
    }

    // Group expenses by category and subcategory
    val categorySummary = remember(displayExpenses) {
        val summary = mutableMapOf<String, MutableMap<String, Double>>()
        displayExpenses.forEach { expense ->
            val category = expense.category
            val subCategory = expense.subCategory.ifBlank { "General" }

            summary.getOrPut(category) { mutableMapOf() }
            val currentAmount = summary[category]?.get(subCategory) ?: 0.0
            summary[category]?.put(subCategory, currentAmount + expense.amount)
        }
        summary
    }

    // Calculate total spent
    val totalSpent = remember(displayExpenses) {
        displayExpenses.sumOf { it.amount }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HamburgerMenu(
                selectedRoute = "category_summary",
                onHomeClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("home") {
                        popUpTo("category_summary") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCategoriesClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("categories") {
                        launchSingleTop = true
                    }
                },
                onExpensesClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("expense_list") {
                        launchSingleTop = true
                    }
                },
                onCategorySummaryClick = {
                    scope.launch { drawerState.close() }
                    // Already on category summary, just close drawer
                },
                onBudgetGoalsClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("budget_goals") {
                        launchSingleTop = true
                    }
                },
                onCategoryTotalsClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("category_totals") {
                        launchSingleTop = true
                    }
                },
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    authViewModel.logout()
                    navController.navigate("welcome") {
                        popUpTo("category_summary") { inclusive = true }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Category Summary") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BrandGreen,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("category_summary") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp) }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            navController.navigate("daily_report") {
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Daily Report") },
                        label = { Text("Daily", fontSize = 11.sp) }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            navController.navigate("add_transaction") {
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add Transaction") },
                        label = { Text("Add", fontSize = 11.sp) }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.open() }
                        },
                        icon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
                        label = { Text("Menu", fontSize = 11.sp) }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Date Filter Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Filter by Date Range",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Date row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showStartDatePicker = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = BrandGreen
                                )
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start: ${startDate?.let { dateDisplayFormat.format(it) } ?: "Select"}")
                            }

                            OutlinedButton(
                                onClick = { showEndDatePicker = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = BrandGreen
                                )
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("End: ${endDate?.let { dateDisplayFormat.format(it) } ?: "Select"}")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filter and Category Select buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Filter applied",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandGreen
                                ),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Icon(Icons.Default.FilterList, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Filter")
                            }

                            OutlinedButton(
                                onClick = { showCategorySelector = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = BrandGreen
                                )
                            ) {
                                Icon(Icons.Default.Checklist, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Select Categories")
                            }
                        }

                        // Clear All Filters button
                        if (startDate != null || endDate != null || selectedCategories.size != allCategories.size) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    startDate = null
                                    endDate = null
                                    selectedCategories = allCategories.toSet()
                                    android.widget.Toast.makeText(
                                        context,
                                        "Cleared all filters - showing all categories",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear All Filters (Dates & Categories)")
                            }
                        }
                    }
                }

                // Selected categories info
                if (selectedCategories.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Showing: ${selectedCategories.size} of ${allCategories.size} categories",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandGreen
                        )
                    }
                }

                // Summary List
                if (displayExpenses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No expenses found for selected period",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(categorySummary.keys.toList().sorted()) { category ->
                            val subCategories = categorySummary[category] ?: emptyMap()
                            val categoryTotal = subCategories.values.sum()

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    // Main Category Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = category.uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandGreen
                                        )
                                        Text(
                                            text = "R${String.format("%.2f", categoryTotal)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandGreenDark
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Sub Categories
                                    subCategories.toList().sortedBy { it.first }.forEach { (subCategory, amount) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp, top = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "• $subCategory",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "R${String.format("%.2f", amount)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Total Spent Card - Brand Green background
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = BrandGreen
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "TOTAL SPENT",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "R${String.format("%.2f", totalSpent)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    // Category Selector Dialog
    if (showCategorySelector) {
        CategorySelectorDialog(
            allCategories = allCategories,
            selectedCategories = selectedCategories,
            onConfirm = { selected ->
                selectedCategories = selected
                showCategorySelector = false
            },
            onDismiss = { showCategorySelector = false }
        )
    }
}