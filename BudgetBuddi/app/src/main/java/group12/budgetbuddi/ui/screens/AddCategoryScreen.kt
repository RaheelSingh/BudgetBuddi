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
import group12.budgetbuddi.ui.components.*
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BudgetGreen = Color(0xFF139A43)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    navController: NavController,
    initialCategoryType: String = "expense",
    authViewModel: AuthViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val allCategories by categoryViewModel.allCategories.collectAsState()
    val isSaving by categoryViewModel.isSaving.collectAsState()
    var isNavigating by remember { mutableStateOf(false) }

    var selectedCategoryType by remember {
        mutableStateOf(
            if (initialCategoryType == "income") "income" else "expense"
        )
    }

    var mainCategory by remember { mutableStateOf("") }
    var subCategory by remember { mutableStateOf("") }
    var visualType by remember { mutableStateOf("emoji") }
    var visualValue by remember { mutableStateOf("🍳") }
    var selectedColor by remember { mutableStateOf(colorFromHex("#139A43")) }
    var expanded by remember { mutableStateOf(false) }

    val outlineColor = colorToHex(selectedColor)

    val filteredMainCategories = remember(allCategories, selectedCategoryType) {
        allCategories
            .filter { it.categoryType == selectedCategoryType }
            .map { it.mainCategory }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val canAdd =
        selectedCategoryType.isNotBlank() &&
                mainCategory.isNotBlank() &&
                subCategory.isNotBlank() &&
                visualValue.isNotBlank()

    // Function to save and navigate
    fun saveAndNavigate() {
        if (isNavigating) return
        isNavigating = true

        scope.launch {
            // Save the category
            categoryViewModel.addCategory(
                categoryType = selectedCategoryType,
                mainCategory = mainCategory,
                subCategory = subCategory,
                visualType = visualType,
                visualValue = visualValue,
                outlineColor = outlineColor
            )

            // Wait for the operation to complete (500ms is enough for Firestore and achievement)
            delay(800)

            // Navigate back
            navController.popBackStack()
        }
    }

    AppScaffold(
        navController = navController,
        selectedRoute = "add_category",
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
                    text = "New Category",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { saveAndNavigate() },
                    enabled = canAdd && !isNavigating
                ) {
                    if (isNavigating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = BudgetGreen,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Category",
                            tint = BudgetGreen
                        )
                    }
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
                    onClick = {
                        selectedCategoryType = "expense"
                        mainCategory = ""
                        subCategory = ""
                    },
                    text = { Text("Expense") }
                )

                Tab(
                    selected = selectedCategoryType == "income",
                    onClick = {
                        selectedCategoryType = "income"
                        mainCategory = ""
                        subCategory = ""
                    },
                    text = { Text("Income") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = mainCategory,
                    onValueChange = { mainCategory = it },
                    label = { Text("Main Category") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BudgetGreen,
                        unfocusedBorderColor = BudgetGreen,
                        focusedLabelColor = BudgetGreen,
                        cursorColor = BudgetGreen
                    ),
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                            enabled = true
                        )
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add new category...") },
                        onClick = {
                            mainCategory = ""
                            expanded = false
                        }
                    )

                    filteredMainCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                mainCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = subCategory,
                onValueChange = { subCategory = it },
                label = { Text("Sub Category Type") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BudgetGreen,
                    unfocusedBorderColor = BudgetGreen,
                    focusedLabelColor = BudgetGreen,
                    cursorColor = BudgetGreen
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = BudgetGreen, thickness = 2.dp)

            Spacer(modifier = Modifier.height(16.dp))

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
                    visualType = visualType,
                    visualValue = visualValue,
                    outlineColor = outlineColor,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                AnimatedVisualTypeToggle(
                    selectedType = visualType,
                    onSelected = { newType ->
                        visualType = newType
                        visualValue = if (newType == "emoji") "🍳" else "restaurant"
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (visualType == "emoji") {
                Text(
                    text = "Choose Emoji",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                EmojiPicker(
                    selectedEmoji = visualValue,
                    onEmojiSelected = { visualValue = it }
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
                    selectedIconKey = visualValue,
                    onIconSelected = { visualValue = it }
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

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedButton(
                onClick = { saveAndNavigate() },
                enabled = canAdd && !isNavigating,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BudgetGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder(
                    enabled = canAdd && !isNavigating
                ).copy(
                    brush = SolidColor(BudgetGreen)
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(150.dp)
            ) {
                if (isNavigating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = BudgetGreen,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AnimatedVisualTypeToggle(
    selectedType: String,
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
                .background(BudgetGreen)
        )

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            ToggleOption(
                text = "Emoji",
                selected = selectedType == "emoji",
                modifier = Modifier.weight(1f),
                onClick = { onSelected("emoji") }
            )

            ToggleOption(
                text = "Icon",
                selected = selectedType == "icon",
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(50))
            .clickable { onClick() },
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