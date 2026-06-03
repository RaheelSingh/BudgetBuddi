package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
private val ScreenBg = Color(0xFFE8F5E9)
private val TextBlack = Color(0xFF17231D)

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

    var selectedCategoryType by remember { mutableStateOf(if (initialCategoryType == "income") "income" else "expense") }
    var mainCategory by remember { mutableStateOf("") }
    var subCategory by remember { mutableStateOf("") }
    var visualType by remember { mutableStateOf("emoji") }
    var visualValue by remember { mutableStateOf("🍳") }
    var selectedColor by remember { mutableStateOf(colorFromHex("#139A43")) }
    var expanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val outlineColor = colorToHex(selectedColor)

    val filteredMainCategories = remember(allCategories, selectedCategoryType) {
        allCategories
            .filter { it.categoryType == selectedCategoryType }
            .map { it.mainCategory }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val canSave = mainCategory.isNotBlank() && subCategory.isNotBlank() && visualValue.isNotBlank() && !isSaving

    fun saveCategory() {
        if (!canSave) return
        isSaving = true
        scope.launch {
            categoryViewModel.addCategory(
                categoryType = selectedCategoryType,
                mainCategory = mainCategory,
                subCategory = subCategory,
                visualType = visualType,
                visualValue = visualValue,
                outlineColor = outlineColor
            )
            delay(650)
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
                .background(ScreenBg)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BudgetGreen,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("New Category", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Create your own budget category", fontSize = 13.sp, color = Color.White.copy(alpha = 0.82f))
                    }

                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.category_bubu),
                    contentDescription = "Category Image",
                    modifier = Modifier.size(92.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                CategoryTypeSliderAdd(
                    selectedCategoryType = selectedCategoryType,
                    onSelected = {
                        selectedCategoryType = it
                        mainCategory = ""
                        subCategory = ""
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Category details", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextBlack)
                        Spacer(modifier = Modifier.height(14.dp))

                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = mainCategory,
                                onValueChange = { mainCategory = it },
                                label = { Text("Main Category") },
                                placeholder = { Text("e.g. Food, Transport, Salary") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BudgetGreen, unfocusedBorderColor = BudgetGreen.copy(alpha = 0.55f), focusedLabelColor = BudgetGreen, cursorColor = BudgetGreen),
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(text = { Text("Add new category...") }, onClick = { mainCategory = ""; expanded = false })
                                filteredMainCategories.forEach { category ->
                                    DropdownMenuItem(text = { Text(category) }, onClick = { mainCategory = category; expanded = false })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = subCategory,
                            onValueChange = { subCategory = it },
                            label = { Text("Subcategory") },
                            placeholder = { Text("e.g. Fuel, Lunch, Monthly Salary") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BudgetGreen, unfocusedBorderColor = BudgetGreen.copy(alpha = 0.55f), focusedLabelColor = BudgetGreen, cursorColor = BudgetGreen),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Appearance", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextBlack)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryVisualPreview(visualType = visualType, visualValue = visualValue, outlineColor = outlineColor, modifier = Modifier.size(62.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            CategoryVisualTypeSlider(selectedType = visualType, onSelected = { newType ->
                                visualType = newType
                                visualValue = if (newType == "emoji") "🍳" else "restaurant"
                            })
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(if (visualType == "emoji") "Choose Emoji" else "Choose Icon", fontWeight = FontWeight.SemiBold, color = TextBlack)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (visualType == "emoji") {
                            EmojiPicker(selectedEmoji = visualValue, onEmojiSelected = { visualValue = it })
                        } else {
                            IconPicker(selectedIconKey = visualValue, onIconSelected = { visualValue = it })
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Text("Outline Colour", fontWeight = FontWeight.SemiBold, color = TextBlack)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            SimpleColorWheel(selectedColor = selectedColor, onColorSelected = { selectedColor = it })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { saveCategory() },
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(containerColor = BudgetGreen, disabledContainerColor = Color.LightGray),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Add Category", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CategoryTypeSliderAdd(selectedCategoryType: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .padding(4.dp)
    ) {
        listOf("expense" to "Expense", "income" to "Income").forEach { (value, label) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (selectedCategoryType == value) BudgetGreen else Color.Transparent)
                    .clickable { onSelected(value) }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (selectedCategoryType == value) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun CategoryVisualTypeSlider(selectedType: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .width(190.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFFE0E0E0))
            .padding(4.dp)
    ) {
        listOf("emoji" to "Emoji", "icon" to "Icon").forEach { (value, label) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (selectedType == value) BudgetGreen else Color.Transparent)
                    .clickable { onSelected(value) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (selectedType == value) Color.White else Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}
