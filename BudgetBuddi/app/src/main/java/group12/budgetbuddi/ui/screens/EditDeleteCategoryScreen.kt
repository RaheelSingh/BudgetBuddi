package group12.budgetbuddi.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.R
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.ui.components.CategoryVisualPreview
import group12.budgetbuddi.ui.components.EmojiPicker
import group12.budgetbuddi.ui.components.IconPicker
import group12.budgetbuddi.ui.components.SimpleColorWheel
import group12.budgetbuddi.ui.components.colorFromHex
import group12.budgetbuddi.ui.components.colorToHex
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel

private val BudgetGreen = Color(0xFF139A43)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeleteCategoryScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val categories by categoryViewModel.allCategories.collectAsState()

    var selectedCategoryType by remember { mutableStateOf("expense") }

    var selectedMainCategory by remember { mutableStateOf("") }
    var selectedSubCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    var editedMainCategory by remember { mutableStateOf("") }
    var editedSubCategory by remember { mutableStateOf("") }
    var editedVisualType by remember { mutableStateOf("emoji") }
    var editedVisualValue by remember { mutableStateOf("🍳") }
    var selectedColor by remember { mutableStateOf(colorFromHex("#139A43")) }

    var mainExpanded by remember { mutableStateOf(false) }
    var subExpanded by remember { mutableStateOf(false) }

    val editedOutlineColor = colorToHex(selectedColor)

    val categoriesForSelectedType = remember(categories, selectedCategoryType) {
        categories.filter { it.categoryType == selectedCategoryType }
    }

    val mainCategoriesForSelectedType = remember(categoriesForSelectedType) {
        categoriesForSelectedType
            .map { it.mainCategory }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val filteredSubCategories = remember(
        categoriesForSelectedType,
        selectedMainCategory
    ) {
        categoriesForSelectedType
            .filter { it.mainCategory == selectedMainCategory }
            .sortedBy { it.subCategory }
    }

    fun resetSelectionForTabChange(newType: String) {
        selectedCategoryType = newType

        selectedMainCategory = ""
        selectedSubCategory = null

        editedMainCategory = ""
        editedSubCategory = ""
        editedVisualType = "emoji"
        editedVisualValue = "🍳"
        selectedColor = colorFromHex("#139A43")

        mainExpanded = false
        subExpanded = false
    }

    fun selectMainCategory(mainCategory: String) {
        selectedMainCategory = mainCategory
        selectedSubCategory = null

        editedMainCategory = mainCategory
        editedSubCategory = ""
        editedVisualType = "emoji"
        editedVisualValue = "🍳"
        selectedColor = colorFromHex("#139A43")

        mainExpanded = false
        subExpanded = false
    }

    fun selectSubCategory(category: CategoryEntity) {
        selectedSubCategory = category

        editedMainCategory = category.mainCategory
        editedSubCategory = category.subCategory
        editedVisualType = category.visualType
        editedVisualValue = category.visualValue
        selectedColor = colorFromHex(category.outlineColor)

        subExpanded = false
    }

    fun saveChanges() {
        if (selectedMainCategory.isBlank() || editedMainCategory.isBlank()) {
            return
        }

        val selectedUpdatedCategory = selectedSubCategory?.copy(
            categoryType = selectedCategoryType,
            mainCategory = editedMainCategory.trim(),
            subCategory = editedSubCategory.trim(),
            visualType = editedVisualType,
            visualValue = editedVisualValue,
            outlineColor = editedOutlineColor
        )

        categoryViewModel.updateCategoryAndMaybeRenameMainCategory(
            oldMainCategory = selectedMainCategory,
            newMainCategory = editedMainCategory.trim(),
            selectedCategory = selectedUpdatedCategory
        )

        navController.popBackStack()
    }

    fun deleteSelectedSubCategory() {
        selectedSubCategory?.let { category ->
            categoryViewModel.deleteCategory(category)
            navController.popBackStack()
        }
    }

    fun deleteSelectedMainCategory() {
        if (selectedMainCategory.isNotBlank()) {
            categoryViewModel.deleteWholeMainCategory(
                mainCategory = selectedMainCategory,
                categoryType = selectedCategoryType
            )
            navController.popBackStack()
        }
    }

    val canSave =
        selectedMainCategory.isNotBlank() &&
                editedMainCategory.isNotBlank() &&
                (
                        selectedSubCategory == null ||
                                editedSubCategory.isNotBlank()
                        )

    val canDeleteMainCategory = selectedMainCategory.isNotBlank()
    val canDeleteSubCategory = selectedSubCategory != null

    AppScaffold(
        navController = navController,
        selectedRoute = "edit_delete_category",
        authViewModel = authViewModel
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { navController.popBackStack() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "<",
                        color = BudgetGreen,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Edit / Delete",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { saveChanges() },
                    enabled = canSave
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save Changes",
                        tint = if (canSave) BudgetGreen else Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Image(
                painter = painterResource(id = R.drawable.category_bubu),
                contentDescription = "Category Image",
                modifier = Modifier
                    .size(95.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(22.dp))

            TabRow(
                selectedTabIndex = if (selectedCategoryType == "expense") 0 else 1,
                containerColor = Color.White,
                contentColor = BudgetGreen
            ) {
                Tab(
                    selected = selectedCategoryType == "expense",
                    onClick = { resetSelectionForTabChange("expense") },
                    text = { Text("Expense") }
                )

                Tab(
                    selected = selectedCategoryType == "income",
                    onClick = { resetSelectionForTabChange("income") },
                    text = { Text("Income") }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = if (selectedCategoryType == "income") {
                    "Select Income Category"
                } else {
                    "Select Expense Category"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = BudgetGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = mainExpanded,
                onExpandedChange = { mainExpanded = !mainExpanded }
            ) {
                OutlinedTextField(
                    value = selectedMainCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Main Category") },
                    placeholder = {
                        Text(
                            if (selectedCategoryType == "income") {
                                "Choose income category"
                            } else {
                                "Choose expense category"
                            }
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BudgetGreen,
                        unfocusedBorderColor = BudgetGreen,
                        focusedLabelColor = BudgetGreen,
                        cursorColor = BudgetGreen
                    ),
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = mainExpanded,
                    onDismissRequest = { mainExpanded = false }
                ) {
                    if (mainCategoriesForSelectedType.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (selectedCategoryType == "income") {
                                        "No income categories available"
                                    } else {
                                        "No expense categories available"
                                    }
                                )
                            },
                            onClick = { mainExpanded = false }
                        )
                    } else {
                        mainCategoriesForSelectedType.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = { selectMainCategory(category) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = subExpanded,
                onExpandedChange = {
                    if (selectedMainCategory.isNotBlank()) {
                        subExpanded = !subExpanded
                    }
                }
            ) {
                OutlinedTextField(
                    value = selectedSubCategory?.subCategory ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sub Category") },
                    placeholder = {
                        Text(
                            if (selectedMainCategory.isBlank()) {
                                "Choose main category first"
                            } else {
                                "Choose subcategory"
                            }
                        )
                    },
                    singleLine = true,
                    enabled = selectedMainCategory.isNotBlank(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BudgetGreen,
                        unfocusedBorderColor = BudgetGreen,
                        focusedLabelColor = BudgetGreen,
                        cursorColor = BudgetGreen,
                        disabledBorderColor = Color.LightGray,
                        disabledLabelColor = Color.Gray,
                        disabledTextColor = Color.Gray
                    ),
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = selectedMainCategory.isNotBlank()
                        )
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = subExpanded,
                    onDismissRequest = { subExpanded = false }
                ) {
                    if (filteredSubCategories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No subcategories available") },
                            onClick = { subExpanded = false }
                        )
                    } else {
                        filteredSubCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.subCategory) },
                                onClick = { selectSubCategory(category) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = BudgetGreen, thickness = 2.dp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Edit Details",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = BudgetGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = editedMainCategory,
                onValueChange = { editedMainCategory = it },
                label = { Text("Main Category Name") },
                singleLine = true,
                enabled = selectedMainCategory.isNotBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BudgetGreen,
                    unfocusedBorderColor = BudgetGreen,
                    focusedLabelColor = BudgetGreen,
                    cursorColor = BudgetGreen,
                    disabledBorderColor = Color.LightGray,
                    disabledLabelColor = Color.Gray,
                    disabledTextColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = editedSubCategory,
                onValueChange = { editedSubCategory = it },
                label = { Text("Sub Category Name") },
                singleLine = true,
                enabled = selectedSubCategory != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BudgetGreen,
                    unfocusedBorderColor = BudgetGreen,
                    focusedLabelColor = BudgetGreen,
                    cursorColor = BudgetGreen,
                    disabledBorderColor = Color.LightGray,
                    disabledLabelColor = Color.Gray,
                    disabledTextColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = BudgetGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryVisualPreview(
                    visualType = editedVisualType,
                    visualValue = editedVisualValue,
                    outlineColor = editedOutlineColor,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                AnimatedVisualTypeToggle(
                    selectedType = editedVisualType,
                    enabled = selectedSubCategory != null,
                    onSelected = { newType ->
                        editedVisualType = newType
                        editedVisualValue = if (newType == "emoji") "🍳" else "restaurant"
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (editedVisualType == "emoji") {
                Text(
                    text = "Choose Emoji",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedSubCategory != null) {
                    EmojiPicker(
                        selectedEmoji = editedVisualValue,
                        onEmojiSelected = { editedVisualValue = it }
                    )
                } else {
                    Text(
                        text = "Select a subcategory first to edit its visual appearance.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            } else {
                Text(
                    text = "Choose Icon",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedSubCategory != null) {
                    IconPicker(
                        selectedIconKey = editedVisualValue,
                        onIconSelected = { editedVisualValue = it }
                    )
                } else {
                    Text(
                        text = "Select a subcategory first to edit its visual appearance.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Outline Colour",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedSubCategory != null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SimpleColorWheel(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it }
                    )
                }
            } else {
                Text(
                    text = "Select a subcategory first to edit its outline colour.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedButton(
                onClick = { saveChanges() },
                enabled = canSave,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BudgetGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder(
                    enabled = canSave
                ).copy(
                    brush = SolidColor(if (canSave) BudgetGreen else Color.LightGray)
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(160.dp)
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { deleteSelectedSubCategory() },
                enabled = canDeleteSubCategory,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = ButtonDefaults.outlinedButtonBorder(
                    enabled = canDeleteSubCategory
                ).copy(
                    brush = SolidColor(
                        if (canDeleteSubCategory) {
                            MaterialTheme.colorScheme.error
                        } else {
                            Color.LightGray
                        }
                    )
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(220.dp)
            ) {
                Text("Delete Selected Subcategory")
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { deleteSelectedMainCategory() },
                enabled = canDeleteMainCategory,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = ButtonDefaults.outlinedButtonBorder(
                    enabled = canDeleteMainCategory
                ).copy(
                    brush = SolidColor(
                        if (canDeleteMainCategory) {
                            MaterialTheme.colorScheme.error
                        } else {
                            Color.LightGray
                        }
                    )
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(220.dp)
            ) {
                Text("Delete Whole Main Category")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AnimatedVisualTypeToggle(
    selectedType: String,
    enabled: Boolean = true,
    onSelected: (String) -> Unit
) {
    val toggleWidth = 184.dp
    val optionWidth = 92.dp

    val indicatorOffset by animateDpAsState(
        targetValue = if (selectedType == "emoji") 0.dp else optionWidth,
        label = "visualToggleSlide"
    )

    Box(
        modifier = Modifier
            .width(toggleWidth)
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) Color(0xFFE0E0E0) else Color(0xFFF1F1F1))
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(optionWidth - 6.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(if (enabled) BudgetGreen else Color.LightGray)
        )

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            ToggleOption(
                text = "Emoji",
                selected = selectedType == "emoji",
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = { onSelected("emoji") }
            )

            ToggleOption(
                text = "Icon",
                selected = selectedType == "icon",
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = { onSelected("icon") }
            )
        }
    }
}

@Composable
private fun ToggleOption(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(50))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = when {
                !enabled -> Color.Gray
                selected -> Color.White
                else -> Color.Gray
            },
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}