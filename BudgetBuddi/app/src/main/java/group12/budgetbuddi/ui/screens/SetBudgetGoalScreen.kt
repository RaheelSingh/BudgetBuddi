package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.BudgetGoalViewModel
import java.util.Calendar

private val BudgetGreen = Color(0xFF139A43)
private val LightGreen = Color(0xFFE8F5E9)
private val TextDark = Color(0xFF1A1A1A)
private val ErrorRed = Color(0xFFD32F2F)

@Composable
fun SetBudgetGoalScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    budgetGoalViewModel: BudgetGoalViewModel = viewModel()
) {
    val addState by budgetGoalViewModel.addGoalUiState.collectAsState()
    var showCategoryPicker by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val cal = Calendar.getInstance()
    val monthName = monthNames.getOrElse(cal.get(Calendar.MONTH)) { "" }
    val year = cal.get(Calendar.YEAR)

    LaunchedEffect(addState.isSaved) {
        if (addState.isSaved) {
            budgetGoalViewModel.resetSavedFlag()
            budgetGoalViewModel.resetForm()
            navController.navigate("category_totals") {
                popUpTo("category_totals") { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    if (showCategoryPicker) {
        val categories = if (addState.selectedTab == 1) {
            addState.incomeCategories
        } else {
            addState.expenseCategories
        }

        CategoryPickerDialog(
            title = if (addState.selectedTab == 1) "Choose Income Category" else "Choose Expense Category",
            categories = categories,
            isIncome = addState.selectedTab == 1,
            onCategorySelected = {
                budgetGoalViewModel.selectCategory(it)
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
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
                .background(LightGreen)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BudgetGreen,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 20.dp)
                ) {
                    IconButton(
                        onClick = {
                            budgetGoalViewModel.resetForm()
                            navController.navigate("category_totals") {
                                popUpTo("category_totals") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Back to budget goals",
                            tint = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (addState.isEditMode) "Edit Budget Goal" else "Add Budget Goal",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$monthName $year",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "Goal Type",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(LightGreen)
                                    .padding(4.dp)
                            ) {
                                GoalTabButton(
                                    text = "Expense",
                                    selected = addState.selectedTab == 0,
                                    modifier = Modifier.weight(1f),
                                    onClick = { budgetGoalViewModel.selectTab(0) }
                                )

                                GoalTabButton(
                                    text = "Income",
                                    selected = addState.selectedTab == 1,
                                    modifier = Modifier.weight(1f),
                                    onClick = { budgetGoalViewModel.selectTab(1) }
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(42.dp),
                                    shape = CircleShape,
                                    color = BudgetGreen.copy(alpha = 0.12f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (addState.selectedTab == 1) Icons.Default.Savings else Icons.Default.TrackChanges,
                                            contentDescription = null,
                                            tint = BudgetGreen,
                                            modifier = Modifier.size(23.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = if (addState.selectedTab == 1) "Income Goal" else "Expense Goal",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = if (addState.selectedTab == 1) {
                                            "Set the minimum income you want to reach."
                                        } else {
                                            "Set your minimum and maximum spending target."
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            FormLabel("Category")
                            Spacer(modifier = Modifier.height(8.dp))

                            CategorySelectBox(
                                selectedCategory = addState.selectedCategory,
                                selectedTab = addState.selectedTab,
                                onClick = { showCategoryPicker = true }
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            FormLabel(
                                if (addState.selectedTab == 1) "Minimum Income Amount" else "Minimum Spending Amount"
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            GoalAmountField(
                                value = addState.minGoal,
                                onValueChange = { budgetGoalViewModel.updateMinGoal(it) },
                                placeholder = "0.00"
                            )

                            if (addState.selectedTab == 0) {
                                Spacer(modifier = Modifier.height(18.dp))

                                FormLabel("Maximum Spending Amount")
                                Spacer(modifier = Modifier.height(8.dp))

                                GoalAmountField(
                                    value = addState.maxGoal,
                                    onValueChange = { budgetGoalViewModel.updateMaxGoal(it) },
                                    placeholder = "0.00"
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(LightGreen)
                                    .clickable { budgetGoalViewModel.toggleRecurring() }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Repeat next month",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "Automatically create this goal again next month",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Switch(
                                    checked = addState.isRecurring,
                                    onCheckedChange = { budgetGoalViewModel.toggleRecurring() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BudgetGreen
                                    )
                                )
                            }

                            addState.error?.let { error ->
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = error,
                                    color = ErrorRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { budgetGoalViewModel.saveGoal() },
                        enabled = !addState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BudgetGreen,
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        if (addState.isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (addState.isEditMode) "Save Changes" else "Save Goal",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalTabButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) BudgetGreen else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark
    )
}

@Composable
private fun GoalAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.LightGray, fontWeight = FontWeight.Medium) },
        prefix = { Text("R", fontWeight = FontWeight.Bold, color = TextDark) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(14.dp),
        textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BudgetGreen,
            unfocusedBorderColor = BudgetGreen.copy(alpha = 0.45f),
            cursorColor = BudgetGreen
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CategorySelectBox(
    selectedCategory: CategoryEntity?,
    selectedTab: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BudgetGreen.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val text = selectedCategory?.let { category ->
                if (category.subCategory.isBlank()) {
                    "${category.visualValue} ${category.mainCategory}"
                } else {
                    "${category.visualValue} ${category.mainCategory} - ${category.subCategory}"
                }
            } ?: if (selectedTab == 1) "Select income category" else "Select expense category"

            Text(
                text = text,
                color = if (selectedCategory == null) Color.LightGray else TextDark,
                fontSize = 15.sp,
                fontWeight = if (selectedCategory == null) FontWeight.Medium else FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Choose category",
                tint = BudgetGreen
            )
        }
    }
}

@Composable
private fun CategoryPickerDialog(
    title: String,
    categories: List<CategoryEntity>,
    isIncome: Boolean,
    onCategorySelected: (CategoryEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    val entireCategory = categories.firstOrNull {
        it.subCategory.isBlank() &&
                (it.mainCategory == "Entire Expense" || it.mainCategory == "Entire Income")
    }

    val realCategories = categories.filterNot {
        it.mainCategory == "Entire Expense" || it.mainCategory == "Entire Income"
    }

    val groupedCategories = realCategories
        .filter { it.mainCategory.isNotBlank() }
        .groupBy { it.mainCategory }
        .toSortedMap()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 500.dp)
                ) {
                    if (entireCategory != null) {
                        item {
                            EntireCategoryPickerItem(
                                category = entireCategory,
                                onClick = { onCategorySelected(entireCategory) }
                            )
                        }
                    }

                    if (groupedCategories.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isIncome) "No income categories found" else "No expense categories found",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        groupedCategories.forEach { (mainCategory, items) ->
                            item {
                                val isExpanded = expandedMap[mainCategory] ?: false
                                val first = items.first()

                                MainCategoryHeader(
                                    visualValue = first.visualValue,
                                    mainCategory = mainCategory,
                                    isExpanded = isExpanded,
                                    onClick = { expandedMap[mainCategory] = !isExpanded }
                                )
                            }

                            if (expandedMap[mainCategory] == true) {
                                val wholeCategoryOption = items.firstOrNull { it.subCategory.isBlank() }
                                val subCategories = items
                                    .filter { it.subCategory.isNotBlank() }
                                    .sortedBy { it.subCategory.lowercase() }

                                if (wholeCategoryOption != null) {
                                    item {
                                        ChildCategoryPickerItem(
                                            label = "$mainCategory - Whole Category",
                                            subtitle = "Track all $mainCategory ${if (isIncome) "income" else "spending"}",
                                            visualValue = wholeCategoryOption.visualValue,
                                            onClick = { onCategorySelected(wholeCategoryOption) }
                                        )
                                    }
                                }

                                subCategories.forEach { category ->
                                    item {
                                        ChildCategoryPickerItem(
                                            label = category.subCategory,
                                            subtitle = category.mainCategory,
                                            visualValue = category.visualValue,
                                            onClick = { onCategorySelected(category) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel", color = BudgetGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EntireCategoryPickerItem(
    category: CategoryEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreen)
            .border(1.dp, BudgetGreen, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(BudgetGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.visualValue, fontSize = 21.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.mainCategory,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Track everything in this type",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun MainCategoryHeader(
    visualValue: String,
    mainCategory: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF4F4F4))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = visualValue, fontSize = 21.sp)

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = mainCategory,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = BudgetGreen
        )
    }
}

@Composable
private fun ChildCategoryPickerItem(
    label: String,
    subtitle: String,
    visualValue: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFAFAFA))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = visualValue, fontSize = 19.sp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
