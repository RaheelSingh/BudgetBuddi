package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel

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
    var editedVisualValue by remember { mutableStateOf("") }
    var editedOutlineColor by remember { mutableStateOf("") }

    var mainExpanded by remember { mutableStateOf(false) }
    var subExpanded by remember { mutableStateOf(false) }

    val filteredSubCategories = categories.filter {
        it.mainCategory == selectedMainCategory
    }

    AppScaffold(
        navController = navController,
        selectedRoute = "edit_delete_category",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    "Edit / Delete Category",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = mainExpanded,
                onExpandedChange = { mainExpanded = !mainExpanded }
            ) {
                OutlinedTextField(
                    value = selectedMainCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Main Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = mainExpanded)
                    },
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
                                editedVisualValue = ""
                                editedOutlineColor = ""

                                mainExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    label = { Text("Select Subcategory (optional)") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = subExpanded)
                    },
                    enabled = selectedMainCategory.isNotBlank(),
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
                                editedVisualValue = category.visualValue
                                editedOutlineColor = category.outlineColor

                                subExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Edit Fields", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedMainCategory,
                onValueChange = { editedMainCategory = it },
                label = { Text("Main Category Name") },
                enabled = selectedMainCategory.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedSubCategory,
                onValueChange = { editedSubCategory = it },
                label = { Text("Subcategory Name") },
                enabled = selectedSubCategory != null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedVisualValue,
                onValueChange = { editedVisualValue = it },
                label = { Text("Emoji / Icon") },
                enabled = selectedSubCategory != null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedOutlineColor,
                onValueChange = { editedOutlineColor = it },
                label = { Text("Outline Color") },
                enabled = selectedSubCategory != null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    selectedSubCategory?.let {
                        categoryViewModel.updateCategory(
                            it.copy(
                                mainCategory = editedMainCategory,
                                subCategory = editedSubCategory,
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
                },
                enabled = selectedMainCategory.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    selectedSubCategory?.let {
                        categoryViewModel.deleteSubCategory(it)
                    }

                    navController.popBackStack()
                },
                enabled = selectedSubCategory != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Subcategory")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (selectedMainCategory.isNotBlank()) {
                        categoryViewModel.deleteWholeCategory(selectedMainCategory)
                    }

                    navController.popBackStack()
                },
                enabled = selectedMainCategory.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Whole Category")
            }
        }
    }
}