package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import group12.budgetbuddi.ui.components.CategoryVisualPreview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel

@Composable
fun CategoriesScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {

    val searchQuery by categoryViewModel.searchQuery.collectAsState()
    val categories by categoryViewModel.filteredCategories.collectAsState()

    AppScaffold(
        navController = navController,
        selectedRoute = "categories",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // Search bar and buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { categoryViewModel.updateSearchQuery(it) },
                    label = { Text("Search categories") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { navController.navigate("add_category") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }

                IconButton(
                    onClick = { navController.navigate("edit_delete_category") }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit/Delete")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category list
            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No categories found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categories) { category ->
                        CategoryItem(category)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: group12.budgetbuddi.data.entity.CategoryEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryVisualPreview(
                visualType = category.visualType,
                visualValue = category.visualValue,
                outlineColor = category.outlineColor,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = category.mainCategory,
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = category.subCategory,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}