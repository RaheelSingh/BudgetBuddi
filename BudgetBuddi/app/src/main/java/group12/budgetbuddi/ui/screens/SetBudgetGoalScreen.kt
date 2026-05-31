package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import group12.budgetbuddi.ui.theme.BackgroundWhite
import group12.budgetbuddi.ui.theme.PrimaryGreen
import group12.budgetbuddi.ui.theme.TextBlack
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.BudgetGoalViewModel
import java.util.Calendar

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
                popUpTo("budget_goals") { inclusive = true }
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
            onDismiss = {
                showCategoryPicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    budgetGoalViewModel.resetForm()
                    navController.popBackStack()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = PrimaryGreen
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (addState.isEditMode) "Edit Goal" else "Set Budget Goal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )

            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$monthName $year",
            color = Color.Gray,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFE0E0E0))
                .padding(4.dp)
        ) {
            TabButton(
                text = "Expense",
                selected = addState.selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { budgetGoalViewModel.selectTab(0) }
            )

            TabButton(
                text = "Income",
                selected = addState.selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { budgetGoalViewModel.selectTab(1) }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = if (addState.selectedTab == 1) {
                "Income Goal"
            } else {
                "Expense Goal"
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (addState.selectedTab == 1) {
                "Set a minimum income amount to reach this month."
            } else {
                "Set your minimum and maximum spending target for this month."
            },
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Category",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, PrimaryGreen, RoundedCornerShape(14.dp))
                .clickable { showCategoryPicker = true }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (addState.selectedCategory == null) {
                    Text(
                        text = if (addState.selectedTab == 1) {
                            "Select income category"
                        } else {
                            "Select expense category"
                        },
                        color = Color.Gray,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    val category = addState.selectedCategory!!

                    Text(
                        text = if (category.subCategory.isBlank()) {
                            "${category.visualValue} ${category.mainCategory}"
                        } else {
                            "${category.visualValue} ${category.mainCategory} - ${category.subCategory}"
                        },
                        color = TextBlack,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Choose category",
                    tint = PrimaryGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (addState.selectedTab == 1) {
                "Minimum Income Amount"
            } else {
                "Minimum Spending Amount"
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = addState.minGoal,
            onValueChange = { budgetGoalViewModel.updateMinGoal(it) },
            placeholder = { Text("0.00") },
            prefix = { Text("R") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = PrimaryGreen,
                cursorColor = PrimaryGreen
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (addState.selectedTab == 0) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Maximum Spending Amount",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextBlack
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = addState.maxGoal,
                onValueChange = { budgetGoalViewModel.updateMaxGoal(it) },
                placeholder = { Text("0.00") },
                prefix = { Text("R") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = PrimaryGreen,
                    cursorColor = PrimaryGreen
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFE8F5E9))
                .clickable { budgetGoalViewModel.toggleRecurring() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Repeat next month",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextBlack
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
                    checkedThumbColor = BackgroundWhite,
                    checkedTrackColor = PrimaryGreen
                )
            )
        }

        if (addState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = addState.error ?: "",
                color = Color(0xFFD32F2F),
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { budgetGoalViewModel.saveGoal() },
            enabled = !addState.isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (addState.isSaving) {
                CircularProgressIndicator(
                    color = BackgroundWhite,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = if (addState.isEditMode) "Save Changes" else "Save Goal",
                    color = BackgroundWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) PrimaryGreen else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) BackgroundWhite else Color.Gray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
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
                (
                        it.mainCategory == "Entire Expense" ||
                                it.mainCategory == "Entire Income"
                        )
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
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
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
                                    text = if (isIncome) {
                                        "No income categories found"
                                    } else {
                                        "No expense categories found"
                                    },
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
                                    onClick = {
                                        expandedMap[mainCategory] = !isExpanded
                                    }
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

                                items
                                    .filter { it.subCategory.isBlank() }
                                    .drop(1)
                                    .forEach { extraWholeOption ->
                                        item {
                                            ChildCategoryPickerItem(
                                                label = "${extraWholeOption.mainCategory} - Whole Category",
                                                subtitle = "Track all ${extraWholeOption.mainCategory}",
                                                visualValue = extraWholeOption.visualValue,
                                                onClick = { onCategorySelected(extraWholeOption) }
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

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel", color = PrimaryGreen)
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
            .background(Color(0xFFE8F5E9))
            .border(1.dp, PrimaryGreen, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(PrimaryGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.visualValue,
                fontSize = 21.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.mainCategory,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
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
        Text(
            text = visualValue,
            fontSize = 21.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = mainCategory,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = if (isExpanded) {
                Icons.Default.KeyboardArrowDown
            } else {
                Icons.Default.KeyboardArrowRight
            },
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = PrimaryGreen
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
        Text(
            text = visualValue,
            fontSize = 19.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextBlack
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}