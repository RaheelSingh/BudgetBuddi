package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.R
import group12.budgetbuddi.ui.components.*
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel

private val BudgetGreen = Color(0xFF139A43)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val mainCategories by categoryViewModel.mainCategories.collectAsState()

    var mainCategory by remember { mutableStateOf("") }
    var subCategory by remember { mutableStateOf("") }

    var visualType by remember { mutableStateOf("emoji") }
    var visualValue by remember { mutableStateOf("🍳") }

    var selectedColor by remember { mutableStateOf(colorFromHex("#139A43")) }
    val outlineColor = colorToHex(selectedColor)

    var expanded by remember { mutableStateOf(false) }

    val canAdd = mainCategory.isNotBlank() &&
            subCategory.isNotBlank() &&
            visualValue.isNotBlank()

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
                    onClick = {
                        categoryViewModel.addCategory(
                            mainCategory = mainCategory,
                            subCategory = subCategory,
                            visualType = visualType,
                            visualValue = visualValue,
                            outlineColor = outlineColor
                        )
                        navController.popBackStack()
                    },
                    enabled = canAdd
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save Category",
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

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = mainCategory,
                    onValueChange = { mainCategory = it },
                    label = { Text("Name") },
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

                    mainCategories.forEach { category ->
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

            HorizontalDivider(
                color = BudgetGreen,
                thickness = 2.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = BudgetGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryVisualPreview(
                    visualType = visualType,
                    visualValue = visualValue,
                    outlineColor = outlineColor,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                FilterChip(
                    selected = visualType == "icon",
                    onClick = {
                        visualType = "icon"
                        visualValue = "restaurant"
                    },
                    label = { Text("Icon") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BudgetGreen.copy(alpha = 0.35f)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterChip(
                    selected = visualType == "emoji",
                    onClick = {
                        visualType = "emoji"
                        visualValue = "🍳"
                    },
                    label = { Text("Emoji") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BudgetGreen.copy(alpha = 0.35f)
                    )
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
                onClick = {
                    categoryViewModel.addCategory(
                        mainCategory = mainCategory,
                        subCategory = subCategory,
                        visualType = visualType,
                        visualValue = visualValue,
                        outlineColor = outlineColor
                    )
                    navController.popBackStack()
                },
                enabled = canAdd,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BudgetGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder(
                    enabled = canAdd
                ).copy(
                    brush = SolidColor(BudgetGreen)
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(150.dp)
            ) {
                Text("Add")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}