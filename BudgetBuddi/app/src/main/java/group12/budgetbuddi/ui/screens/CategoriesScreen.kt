package group12.budgetbuddi.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.components.CategoryVisualPreview
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel

/*
Author: Android Developers
Description: Used as reference for collecting state from a ViewModel in Jetpack Compose and updating the UI when state changes.
Reference: Android Developers. 2024. State and Jetpack Compose. [Online]. Available at: https://developer.android.com/develop/ui/compose/state [Accessed 29 April 2026].
*/

/*
Author: Android Developers
Description: Used as reference for using lazy layouts such as LazyColumn and LazyVerticalGrid to display scrollable lists and grid content.
Reference: Android Developers. 2024. Lists and grids. [Online]. Available at: https://developer.android.com/develop/ui/compose/lists [Accessed 29 April 2026.
*/

/*
Author: Android Developers
Description: Used as reference for simple Compose state-based animations such as animateFloatAsState.
Reference: Android Developers. 2025. Value-based animations. [Online]. Available at: https://developer.android.com/develop/ui/compose/animation/value-based [Accessed 29 April 2026].
*/

private val BudgetGreen = Color(0xFF139A43)

@Composable
fun CategoriesScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    // The screen observes search text and filtered categories from the ViewModel.
    val searchQuery by categoryViewModel.searchQuery.collectAsState()
    val categories by categoryViewModel.filteredCategories.collectAsState()

    // Categories are grouped so each main category can have its own section.
    val groupedCategories = categories.groupBy { it.mainCategory }

    AppScaffold(
        navController = navController,
        selectedRoute = "categories",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BudgetGreen
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .border(1.5.dp, BudgetGreen, RoundedCornerShape(50))
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigate("edit_delete_category") },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit/Delete",
                            tint = BudgetGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { navController.navigate("add_category") },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = BudgetGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            RoundedSearchBar(
                searchQuery = searchQuery,
                onSearchChanged = { categoryViewModel.updateSearchQuery(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                    groupedCategories.forEach { (mainCategory, subCategories) ->

                        item {
                            CategorySectionHeader(mainCategory)
                        }

                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp),
                                userScrollEnabled = false
                            ) {
                                items(subCategories) { category ->
                                    SubCategoryGridItem(category)
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
}

@Composable
private fun RoundedSearchBar(
    searchQuery: String,
    onSearchChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChanged,
        placeholder = {
            Text(
                text = "Search",
                fontSize = 15.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BudgetGreen,
            unfocusedBorderColor = Color.LightGray,
            cursorColor = BudgetGreen
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    )
}

@Composable
private fun CategorySectionHeader(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp
            ),
            color = Color.DarkGray,
            modifier = Modifier.padding(start = 2.dp)
        )

        Spacer(modifier = Modifier.height(5.dp))

        HorizontalDivider(
            color = BudgetGreen,
            thickness = 1.7.dp
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SubCategoryGridItem(category: CategoryEntity) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // This gives the category item a small press animation.
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "categoryPressScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 1.dp, vertical = 6.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                // No action for now. This exists only for subtle press feedback.
            }
    ) {
        CategoryVisualPreview(
            visualType = category.visualType,
            visualValue = category.visualValue,
            outlineColor = category.outlineColor,
            modifier = Modifier.size(70.dp)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = category.subCategory,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.5.sp
            ),
            maxLines = 1,
            color = Color.DarkGray
        )
    }
}