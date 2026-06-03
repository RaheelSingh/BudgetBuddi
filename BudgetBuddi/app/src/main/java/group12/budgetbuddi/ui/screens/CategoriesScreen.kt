package group12.budgetbuddi.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.components.getBudgetIcon
import group12.budgetbuddi.ui.components.colorFromHex
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel

private val BudgetGreen = Color(0xFF139A43)
private val ScreenBg = Color(0xFFE8F5E9)
private val SoftGreen = Color(0xFFEAF7EF)
private val SoftBorder = Color(0xFFD7E8DE)
private val TextBlack = Color(0xFF17231D)

@Composable
fun CategoriesScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val searchQuery by categoryViewModel.searchQuery.collectAsState()
    val categories by categoryViewModel.filteredCategories.collectAsState()

    var selectedCategoryType by remember { mutableStateOf("expense") }

    val visibleCategories = remember(categories, selectedCategoryType) {
        categories.filter { it.categoryType == selectedCategoryType }
    }

    val groupedCategories = remember(visibleCategories) {
        visibleCategories.groupBy { it.mainCategory }.toSortedMap()
    }

    AppScaffold(
        navController = navController,
        selectedRoute = "categories",
        authViewModel = authViewModel
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ScreenBg)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BudgetGreen,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Categories",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White)
                                .padding(horizontal = 2.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { navController.navigate("edit_delete_category") },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit/Delete", tint = BudgetGreen, modifier = Modifier.size(21.dp))
                            }

                            IconButton(
                                onClick = { navController.navigate("add_category/$selectedCategoryType") },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = BudgetGreen, modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    CategoryTypeSlider(
                        selectedCategoryType = selectedCategoryType,
                        onSelected = { selectedCategoryType = it }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                RoundedSearchBar(
                    searchQuery = searchQuery,
                    onSearchChanged = { categoryViewModel.updateSearchQuery(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (visibleCategories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedCategoryType == "income") "No income categories found" else "No expense categories found",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        groupedCategories.forEach { (mainCategory, subCategories) ->
                            item {
                                CategoryGroupCard(
                                    mainCategory = mainCategory,
                                    subCategories = subCategories
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTypeSlider(
    selectedCategoryType: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(4.dp)
    ) {
        listOf("expense" to "Expenses", "income" to "Income").forEach { (value, label) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (selectedCategoryType == value) Color.White else Color.Transparent)
                    .clickable { onSelected(value) }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selectedCategoryType == value) BudgetGreen else Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
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
        placeholder = { Text(text = "Search categories", fontSize = 15.sp) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = BudgetGreen)
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BudgetGreen,
            unfocusedBorderColor = SoftBorder,
            cursorColor = BudgetGreen,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}

@Composable
private fun CategoryGroupCard(
    mainCategory: String,
    subCategories: List<CategoryEntity>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SoftBorder),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
            Text(
                text = mainCategory,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BudgetGreen.copy(alpha = 0.22f), thickness = 1.2.dp)
            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
                userScrollEnabled = false,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(subCategories) { category ->
                    SubCategoryGridItem(category)
                }
            }
        }
    }
}

@Composable
fun SubCategoryGridItem(category: CategoryEntity) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        label = "categoryPressScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = 6.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) {}
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(SoftGreen),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .border(2.dp, colorFromHex(category.outlineColor), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (category.visualType == "icon") {
                    Icon(
                        imageVector = getBudgetIcon(category.visualValue),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = TextBlack
                    )
                } else {
                    Text(
                        text = category.visualValue.ifBlank { "?" },
                        fontSize = 31.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = category.subCategory,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = TextBlack
        )
    }
}
