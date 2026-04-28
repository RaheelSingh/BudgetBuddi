package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.*
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel

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

    var selectedColor by remember { mutableStateOf(colorFromHex("#4CAF50")) }
    val outlineColor = colorToHex(selectedColor)

    var expanded by remember { mutableStateOf(false) }

    AppScaffold(
        navController = navController,
        selectedRoute = "add_category",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    "Add Category",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main category with dropdown and input
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = mainCategory,
                    onValueChange = { mainCategory = it },
                    label = { Text("Main Category") },
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

            Spacer(modifier = Modifier.height(12.dp))

            // Subcategory
            OutlinedTextField(
                value = subCategory,
                onValueChange = { subCategory = it },
                label = { Text("Subcategory") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Category Visual", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            CategoryVisualPreview(
                visualType = visualType,
                visualValue = visualValue,
                outlineColor = outlineColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Visual type
            Row {
                FilterChip(
                    selected = visualType == "emoji",
                    onClick = {
                        visualType = "emoji"
                        visualValue = "🍳"
                    },
                    label = { Text("Emoji") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterChip(
                    selected = visualType == "icon",
                    onClick = {
                        visualType = "icon"
                        visualValue = "restaurant"
                    },
                    label = { Text("Icon") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (visualType == "emoji") {
                Text("Choose Emoji", style = MaterialTheme.typography.bodyMedium)
                EmojiPicker(
                    selectedEmoji = visualValue,
                    onEmojiSelected = { visualValue = it }
                )
            } else {
                Text("Choose Icon", style = MaterialTheme.typography.bodyMedium)
                IconPicker(
                    selectedIconKey = visualValue,
                    onIconSelected = { visualValue = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Choose Outline Colour", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            SimpleColorWheel(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Add Button
            Button(
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
                enabled = mainCategory.isNotBlank() && subCategory.isNotBlank() && visualValue.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Category")
            }
        }
    }
}