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
import group12.budgetbuddi.ui.components.*
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
    val mainCategories by categoryViewModel.mainCategories.collectAsState()

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

    val filteredSubCategories = categories.filter {
        it.mainCategory == selectedMainCategory
    }

    fun saveChanges() {
        selectedSubCategory?.let {
            categoryViewModel.updateCategory(
                it.copy(
                    mainCategory = editedMainCategory,
                    subCategory = editedSubCategory,
                    visualType = editedVisualType,
                    visualValue = editedVisualValue,
                    outlineColor = editedOutlineColor
                )
            )
        }

        if (
            selectedMainCategory.isNotBlank() &&
            editedMainCategory.isNotBlank() &&
            editedMainCategory != selectedMainCategory
        ) {
            filteredSubCategories.forEach {
                categoryViewModel.updateCategory(
                    it.copy(mainCategory = editedMainCategory)
                )
            }
        }

        navController.popBackStack()
    }

    val canSave = selectedMainCategory.isNotBlank() &&
            editedMainCategory.isNotBlank() &&
            (
                    selectedSubCategory == null ||
                            editedSubCategory.isNotBlank()
                    )

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
                        tint = BudgetGreen
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

            Text(
                text = "Select Category",
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
                    label = { Text("Name") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = mainExpanded)
                    },
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
                    mainCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedMainCategory = category
                                selectedSubCategory = null

                                editedMainCategory = category
                                editedSubCategory = ""

                                editedVisualType = "emoji"
                                editedVisualValue = "🍳"
                                selectedColor = colorFromHex("#139A43")

                                mainExpanded = false
                            }
                        )
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
                    label = { Text("Sub Category Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = subExpanded)
                    },
                    enabled = selectedMainCategory.isNotBlank(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BudgetGreen,
                        unfocusedBorderColor = BudgetGreen,
                        disabledBorderColor = BudgetGreen.copy(alpha = 0.5f),
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
                    expanded = subExpanded,
                    onDismissRequest = { subExpanded = false }
                ) {
                    filteredSubCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.subCategory) },
                            onClick = {
                                selectedSubCategory = category

                                editedMainCategory = category.mainCategory
                                editedSubCategory = category.subCategory
                                editedVisualType = category.visualType
                                editedVisualValue = category.visualValue
                                selectedColor = colorFromHex(category.outlineColor)

                                subExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = BudgetGreen, thickness = 2.dp)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Edit Selected Category",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = BudgetGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = editedMainCategory,
                onValueChange = { editedMainCategory = it },
                label = { Text("Edit Category Name") },
                enabled = selectedMainCategory.isNotBlank(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BudgetGreen,
                    unfocusedBorderColor = BudgetGreen,
                    disabledBorderColor = BudgetGreen.copy(alpha = 0.5f),
                    focusedLabelColor = BudgetGreen,
                    cursorColor = BudgetGreen
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = editedSubCategory,
                onValueChange = { editedSubCategory = it },
                label = { Text("Edit Subcategory Name") },
                enabled = selectedSubCategory != null,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BudgetGreen,
                    unfocusedBorderColor = BudgetGreen,
                    disabledBorderColor = BudgetGreen.copy(alpha = 0.5f),
                    focusedLabelColor = BudgetGreen,
                    cursorColor = BudgetGreen
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = BudgetGreen, thickness = 2.dp)

            Spacer(modifier = Modifier.height(14.dp))

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

            if (selectedSubCategory != null) {
                if (editedVisualType == "emoji") {
                    Text(
                        text = "Choose Emoji",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    EmojiPicker(
                        selectedEmoji = editedVisualValue,
                        onEmojiSelected = { editedVisualValue = it }
                    )
                } else {
                    Text(
                        text = "Choose Icon",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    IconPicker(
                        selectedIconKey = editedVisualValue,
                        onIconSelected = { editedVisualValue = it }
                    )
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
                    text = "Select a subcategory to edit its emoji/icon and colour.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
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
                    brush = SolidColor(BudgetGreen)
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(160.dp)
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = {
                    selectedSubCategory?.let {
                        categoryViewModel.deleteSubCategory(it)
                    }
                    navController.popBackStack()
                },
                enabled = selectedSubCategory != null,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = ButtonDefaults.outlinedButtonBorder(
                    enabled = selectedSubCategory != null
                ).copy(
                    brush = SolidColor(MaterialTheme.colorScheme.error)
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(220.dp)
            ) {
                Text("Delete Subcategory")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    if (selectedMainCategory.isNotBlank()) {
                        categoryViewModel.deleteWholeCategory(selectedMainCategory)
                    }
                    navController.popBackStack()
                },
                enabled = selectedMainCategory.isNotBlank(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = ButtonDefaults.outlinedButtonBorder(
                    enabled = selectedMainCategory.isNotBlank()
                ).copy(
                    brush = SolidColor(MaterialTheme.colorScheme.error)
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(220.dp)
            ) {
                Text("Delete Whole Category")
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun AnimatedVisualTypeToggle(
    selectedType: String,
    enabled: Boolean,
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
            .background(Color(0xFFE0E0E0))
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(optionWidth - 6.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(if (enabled) BudgetGreen else BudgetGreen.copy(alpha = 0.35f))
        )

        Row(modifier = Modifier.fillMaxSize()) {
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
            color = if (selected) Color.White else Color.Gray,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}