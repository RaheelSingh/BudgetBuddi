package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.components.CategoryVisualPreview
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel

private val Green = Color(0xFF139A43)

@Composable
fun CategoriesScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {

    val searchQuery by categoryViewModel.searchQuery.collectAsState()
    val categories by categoryViewModel.filteredCategories.collectAsState()

    val grouped = categories.groupBy { it.mainCategory }

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

            // Page title
            Text(
                text = "Categories",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Green
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search + buttons row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { categoryViewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { navController.navigate("edit_delete_category") }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Green)
                }

                IconButton(
                    onClick = { navController.navigate("add_category") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Green)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category sections
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {

                grouped.forEach { (mainCategory, subCategories) ->

                    item {
                        Text(
                            text = mainCategory,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.Black
                        )
                    }

                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp),
                            userScrollEnabled = false
                        ) {
                            items(subCategories) { category ->
                                SubCategoryItem(category)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SubCategoryItem(category: group12.budgetbuddi.data.entity.CategoryEntity) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(6.dp)
    ) {

        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            CategoryVisualPreview(
                visualType = category.visualType,
                visualValue = category.visualValue,
                outlineColor = category.outlineColor,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.subCategory,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}