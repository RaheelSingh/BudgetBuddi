package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
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
    var visualValue by remember { mutableStateOf("") }
    var outlineColor by remember { mutableStateOf("#4CAF50") }

    var expanded by remember { mutableStateOf(false) }

    val uiMessage by categoryViewModel.uiMessage.collectAsState()

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

            Text("Add Category", style = MaterialTheme.typography.headlineMedium)

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
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
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

            Spacer(modifier = Modifier.height(12.dp))

            // Visual type
            Row {
                FilterChip(
                    selected = visualType == "emoji",
                    onClick = { visualType = "emoji" },
                    label = { Text("Emoji") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterChip(
                    selected = visualType == "icon",
                    onClick = { visualType = "icon" },
                    label = { Text("Icon") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))


            // Visual value
            OutlinedTextField(
                value = visualValue,
                onValueChange = { visualValue = it },
                label = { Text("Emoji or Icon Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Simple color input
            OutlinedTextField(
                value = outlineColor,
                onValueChange = { outlineColor = it },
                label = { Text("Outline Color (Hex)") },
                modifier = Modifier.fillMaxWidth()
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
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Category")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message
            uiMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}