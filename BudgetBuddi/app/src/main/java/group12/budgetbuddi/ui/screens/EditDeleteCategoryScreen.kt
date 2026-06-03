package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.R
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.ui.components.*
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel

private val EditGreen = Color(0xFF139A43)
private val EditBg = Color(0xFFE8F5E9)
private val EditText = Color(0xFF17231D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeleteCategoryScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val categories by categoryViewModel.allCategories.collectAsState()

    var selectedCategoryType by remember { mutableStateOf("expense") }
    var selectedMainCategory by remember { mutableStateOf("") }
    var selectedSubCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    var editedMainCategory by remember { mutableStateOf("") }
    var editedSubCategory by remember { mutableStateOf("") }
    var editedVisualType by remember { mutableStateOf("emoji") }
    var editedVisualValue by remember { mutableStateOf("🍳") }
    var selectedColor by remember { mutableStateOf(colorFromHex("#139A43")) }

    var mainExpanded by remember { mutableStateOf(false) }
    var subExpanded by remember { mutableStateOf(false) }
    var showDeleteMainDialog by remember { mutableStateOf(false) }
    var showDeleteSubDialog by remember { mutableStateOf(false) }

    val editedOutlineColor = colorToHex(selectedColor)

    val categoriesForSelectedType = remember(categories, selectedCategoryType) {
        categories.filter { it.categoryType == selectedCategoryType }
    }

    val mainCategoriesForSelectedType = remember(categoriesForSelectedType) {
        categoriesForSelectedType.map { it.mainCategory }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredSubCategories = remember(categoriesForSelectedType, selectedMainCategory) {
        categoriesForSelectedType.filter { it.mainCategory == selectedMainCategory }.sortedBy { it.subCategory }
    }

    fun resetSelectionForTabChange(newType: String) {
        selectedCategoryType = newType
        selectedMainCategory = ""
        selectedSubCategory = null
        editedMainCategory = ""
        editedSubCategory = ""
        editedVisualType = "emoji"
        editedVisualValue = "🍳"
        selectedColor = colorFromHex("#139A43")
        mainExpanded = false
        subExpanded = false
    }

    fun selectMainCategory(mainCategory: String) {
        selectedMainCategory = mainCategory
        selectedSubCategory = null
        editedMainCategory = mainCategory
        editedSubCategory = ""
        val first = categoriesForSelectedType.firstOrNull { it.mainCategory == mainCategory }
        editedVisualType = first?.visualType ?: "emoji"
        editedVisualValue = first?.visualValue ?: "🍳"
        selectedColor = colorFromHex(first?.outlineColor ?: "#139A43")
        mainExpanded = false
        subExpanded = false
    }

    fun selectSubCategory(category: CategoryEntity) {
        selectedSubCategory = category
        editedMainCategory = category.mainCategory
        editedSubCategory = category.subCategory
        editedVisualType = category.visualType
        editedVisualValue = category.visualValue
        selectedColor = colorFromHex(category.outlineColor)
        subExpanded = false
    }

    fun saveChanges() {
        if (selectedMainCategory.isBlank() || editedMainCategory.isBlank()) return

        val selectedUpdatedCategory = selectedSubCategory?.copy(
            categoryType = selectedCategoryType,
            mainCategory = editedMainCategory.trim(),
            subCategory = editedSubCategory.trim(),
            visualType = editedVisualType,
            visualValue = editedVisualValue,
            outlineColor = editedOutlineColor
        )

        categoryViewModel.updateCategoryAndMaybeRenameMainCategory(
            oldMainCategory = selectedMainCategory,
            newMainCategory = editedMainCategory.trim(),
            selectedCategory = selectedUpdatedCategory
        )
        navController.popBackStack()
    }

    val canSave = selectedMainCategory.isNotBlank() && editedMainCategory.isNotBlank() &&
            (selectedSubCategory == null || editedSubCategory.isNotBlank())

    AppScaffold(
        navController = navController,
        selectedRoute = "edit_delete_category",
        authViewModel = authViewModel
    ) { modifier ->
        Column(
            modifier = modifier.fillMaxSize().background(EditBg)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = EditGreen,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Edit / Delete", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Update or remove categories", fontSize = 13.sp, color = Color.White.copy(alpha = 0.82f))
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
                Image(painter = painterResource(id = R.drawable.category_bubu), contentDescription = null, modifier = Modifier.size(92.dp))
                Spacer(modifier = Modifier.height(16.dp))

                EditTypeSlider(selectedCategoryType = selectedCategoryType, onSelected = { resetSelectionForTabChange(it) })
                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select category", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = EditText)
                        Spacer(modifier = Modifier.height(14.dp))

                        ExposedDropdownMenuBox(expanded = mainExpanded, onExpandedChange = { mainExpanded = !mainExpanded }) {
                            OutlinedTextField(
                                value = selectedMainCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Main Category") },
                                placeholder = { Text("Choose category") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EditGreen, unfocusedBorderColor = EditGreen.copy(alpha = 0.55f), focusedLabelColor = EditGreen),
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = mainExpanded, onDismissRequest = { mainExpanded = false }) {
                                mainCategoriesForSelectedType.forEach { main ->
                                    DropdownMenuItem(text = { Text(main) }, onClick = { selectMainCategory(main) })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ExposedDropdownMenuBox(expanded = subExpanded, onExpandedChange = { if (selectedMainCategory.isNotBlank()) subExpanded = !subExpanded }) {
                            OutlinedTextField(
                                value = selectedSubCategory?.subCategory ?: "Whole category / no specific subcategory",
                                onValueChange = {},
                                readOnly = true,
                                enabled = selectedMainCategory.isNotBlank(),
                                label = { Text("Subcategory") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EditGreen, unfocusedBorderColor = EditGreen.copy(alpha = 0.55f), focusedLabelColor = EditGreen),
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = subExpanded, onDismissRequest = { subExpanded = false }) {
                                filteredSubCategories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.subCategory.ifBlank { "Whole category" }) },
                                        onClick = { selectSubCategory(category) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Edit details", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = EditText)
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = editedMainCategory,
                            onValueChange = { editedMainCategory = it },
                            label = { Text("Main Category") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EditGreen, unfocusedBorderColor = EditGreen.copy(alpha = 0.55f), focusedLabelColor = EditGreen, cursorColor = EditGreen),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = editedSubCategory,
                            onValueChange = { editedSubCategory = it },
                            label = { Text("Subcategory") },
                            enabled = selectedSubCategory != null,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EditGreen, unfocusedBorderColor = EditGreen.copy(alpha = 0.55f), focusedLabelColor = EditGreen, cursorColor = EditGreen),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryVisualPreview(visualType = editedVisualType, visualValue = editedVisualValue, outlineColor = editedOutlineColor, modifier = Modifier.size(62.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            EditVisualTypeSlider(selectedType = editedVisualType, onSelected = { newType ->
                                editedVisualType = newType
                                editedVisualValue = if (newType == "emoji") "🍳" else "restaurant"
                            })
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        if (editedVisualType == "emoji") {
                            EmojiPicker(selectedEmoji = editedVisualValue, onEmojiSelected = { editedVisualValue = it })
                        } else {
                            IconPicker(selectedIconKey = editedVisualValue, onIconSelected = { editedVisualValue = it })
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Outline Colour", fontWeight = FontWeight.SemiBold, color = EditText)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            SimpleColorWheel(selectedColor = selectedColor, onColorSelected = { selectedColor = it })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showDeleteMainDialog = true },
                        enabled = selectedMainCategory.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Category", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = { showDeleteSubDialog = true },
                        enabled = selectedSubCategory != null,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Sub", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { saveChanges() },
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(containerColor = EditGreen, disabledContainerColor = Color.LightGray),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteMainDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteMainDialog = false },
            title = { Text("Delete Category") },
            text = { Text("Delete the whole '$selectedMainCategory' category and all its subcategories?") },
            confirmButton = {
                TextButton(onClick = {
                    categoryViewModel.deleteWholeMainCategory(selectedMainCategory, selectedCategoryType)
                    showDeleteMainDialog = false
                    navController.popBackStack()
                }) { Text("Delete", color = Color(0xFFE53935), fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteMainDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteSubDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSubDialog = false },
            title = { Text("Delete Subcategory") },
            text = { Text("Delete '${selectedSubCategory?.subCategory}'?") },
            confirmButton = {
                TextButton(onClick = {
                    selectedSubCategory?.let { categoryViewModel.deleteCategory(it) }
                    showDeleteSubDialog = false
                    navController.popBackStack()
                }) { Text("Delete", color = Color(0xFFE53935), fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteSubDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun EditTypeSlider(selectedCategoryType: String, onSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50.dp)).background(Color.White).padding(4.dp)) {
        listOf("expense" to "Expense", "income" to "Income").forEach { (value, label) ->
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(50.dp)).background(if (selectedCategoryType == value) EditGreen else Color.Transparent).clickable { onSelected(value) }.padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                Text(label, color = if (selectedCategoryType == value) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun EditVisualTypeSlider(selectedType: String, onSelected: (String) -> Unit) {
    Row(modifier = Modifier.width(190.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xFFE0E0E0)).padding(4.dp)) {
        listOf("emoji" to "Emoji", "icon" to "Icon").forEach { (value, label) ->
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(50.dp)).background(if (selectedType == value) EditGreen else Color.Transparent).clickable { onSelected(value) }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text(label, color = if (selectedType == value) Color.White else Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}
