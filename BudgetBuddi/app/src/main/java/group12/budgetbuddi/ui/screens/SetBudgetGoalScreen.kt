package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
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

    // Navigate back on successful save
    LaunchedEffect(addState.isSaved) {
        if (addState.isSaved) {
            budgetGoalViewModel.resetSavedFlag()
            budgetGoalViewModel.resetForm()
            navController.navigate("category_totals") {
                popUpTo("budget_goals") { inclusive = true }
            }
        }
    }

    // Category picker dialog
    if (showCategoryPicker) {
        val categories = if (addState.selectedTab == 1)
            addState.incomeCategories else addState.expenseCategories

        CategoryPickerDialog(
            categories = categories,
            onCategorySelected = {
                budgetGoalViewModel.selectCategory(it)
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
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
            IconButton(onClick = {
                budgetGoalViewModel.resetForm()
                navController.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = PrimaryGreen
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (addState.isEditMode) "Edit Budget Goal" else "New Budget Goal",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextBlack
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$monthName $year",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (!addState.isEditMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFF0F0F0))
                    .padding(4.dp)
            ) {
                listOf("Expense", "Income").forEachIndexed { index, label ->
                    val selected = addState.selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (selected) PrimaryGreen else Color.Transparent)
                            .clickable { budgetGoalViewModel.selectTab(index) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) BackgroundWhite else Color.Gray,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        Text(
            text = "Category",
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    1.dp,
                    if (addState.selectedCategory != null) PrimaryGreen else Color(0xFFE0E0E0),
                    RoundedCornerShape(12.dp)
                )
                .clickable { showCategoryPicker = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (addState.selectedCategory != null) {
                Text(
                    text = "${addState.selectedCategory!!.visualValue} ${addState.selectedCategory!!.mainCategory} - ${addState.selectedCategory!!.subCategory}",
                    color = TextBlack,
                    fontSize = 15.sp
                )
            } else {
                Text(
                    text = "Select a category",
                    color = Color.LightGray,
                    fontSize = 15.sp
                )
            }
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        val minLabel = if (addState.selectedTab == 1)
            "Minimum to Earn (R)" else "Minimum Spending (R)"

        Text(
            text = minLabel,
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = addState.minGoal,
            onValueChange = { budgetGoalViewModel.updateMinGoal(it) },
            placeholder = { Text("0.00", color = Color.LightGray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                cursorColor = PrimaryGreen,
                focusedLabelColor = PrimaryGreen
            )
        )

        if (addState.selectedTab == 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Maximum Spending (R)",
                fontSize = 13.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = addState.maxGoal,
                onValueChange = { budgetGoalViewModel.updateMaxGoal(it) },
                placeholder = { Text("0.00", color = Color.LightGray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    cursorColor = PrimaryGreen,
                    focusedLabelColor = PrimaryGreen
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Recurring",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextBlack
                )
                Text(
                    text = "Repeat this goal next month",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Switch(
                checked = addState.isRecurring,
                onCheckedChange = { budgetGoalViewModel.toggleRecurring() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BackgroundWhite,
                    checkedTrackColor = PrimaryGreen,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFFE0E0E0)
                )
            )
        }

        if (addState.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = addState.error!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "💰",
            fontSize = 56.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { budgetGoalViewModel.saveGoal() },
            enabled = !addState.isSaving,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(160.dp)
                .height(48.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                contentColor = BackgroundWhite
            )
        ) {
            if (addState.isSaving) {
                CircularProgressIndicator(
                    color = BackgroundWhite,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (addState.isEditMode) "Update" else "Add",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CategoryPickerDialog(
    categories: List<CategoryEntity>,
    onCategorySelected: (CategoryEntity) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Select Category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextBlack
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                HorizontalDivider()

                if (categories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No categories found", color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        // Group by main category
                        val grouped = categories.groupBy { it.mainCategory }
                        grouped.forEach { (mainCat, subCategories) ->
                            item {
                                Text(
                                    text = mainCat,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = PrimaryGreen,
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        top = 12.dp,
                                        bottom = 4.dp
                                    )
                                )
                            }
                            items(subCategories) { category ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onCategorySelected(category) }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.visualValue,
                                        fontSize = 22.sp,
                                        modifier = Modifier.width(36.dp)
                                    )
                                    Text(
                                        text = category.subCategory,
                                        fontSize = 15.sp,
                                        color = TextBlack
                                    )
                                }
                                HorizontalDivider(
                                    color = Color(0xFFF5F5F5),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}