package group12.budgetbuddi.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import group12.budgetbuddi.R
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.components.CategoryVisualPreview
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

private val BrandGreen = Color(0xFF4CAF50)
private val LightGreen = Color(0xFFE8F5E9)
private val ErrorRed = Color(0xFFD32F2F)
private val ErrorBackground = Color(0xFFFFEBEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    AppScaffold(
        navController = navController,
        selectedRoute = "add_transaction",
        authViewModel = authViewModel
    ) { modifier ->

        val context = LocalContext.current

        var selectedType by remember { mutableStateOf("expense") }
        var amountText by remember { mutableStateOf("") }
        var selectedDate by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        var selectedMainCategory by remember { mutableStateOf("") }
        var selectedSubCategory by remember { mutableStateOf("") }
        var paymentMethod by remember { mutableStateOf("Cash") }
        var isRecurring by remember { mutableStateOf(false) }

        var amountError by remember { mutableStateOf<String?>(null) }
        var categoryError by remember { mutableStateOf<String?>(null) }
        var dateError by remember { mutableStateOf<String?>(null) }

        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var photoUriString by remember { mutableStateOf<String?>(null) }
        var cameraUri by remember { mutableStateOf<Uri?>(null) }

        var showDatePicker by remember { mutableStateOf(false) }
        var showCategoryDialog by remember { mutableStateOf(false) }
        var showPaymentDialog by remember { mutableStateOf(false) }
        var showPhotoDialog by remember { mutableStateOf(false) }

        var expandedMainCategoryInDialog by remember { mutableStateOf<String?>(null) }

        // This prevents the first click from closing the dialog and immediately returning "denied"
        // before Android has a clean chance to show the permission popup.
        var pendingCameraPermissionRequest by remember { mutableStateOf(false) }

        val allCategories by categoryViewModel.allCategories.collectAsState()

        val categoriesForSelectedType = remember(allCategories, selectedType) {
            allCategories.filter { it.categoryType == selectedType }
        }

        val mainCategoriesForSelectedType = remember(categoriesForSelectedType) {
            categoriesForSelectedType
                .map { it.mainCategory }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }

        val saveSuccess by expenseViewModel.saveSuccess.collectAsState()
        val uiMessage by expenseViewModel.uiMessage.collectAsState()
        val isUploadingImage by expenseViewModel.isUploadingImage.collectAsState()

        LaunchedEffect(amountText) {
            if (amountText.isNotBlank()) amountError = null
        }

        LaunchedEffect(selectedMainCategory) {
            if (selectedMainCategory.isNotBlank()) categoryError = null
        }

        LaunchedEffect(selectedDate) {
            if (selectedDate.isNotBlank()) dateError = null
        }

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                photoUriString = uri.toString()
            }
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && cameraUri != null) {
                selectedImageUri = cameraUri
                photoUriString = cameraUri.toString()
            }
        }

        fun launchCameraCapture() {
            try {
                val file = createImageFile(context)

                cameraUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                cameraUri?.let { uri ->
                    cameraLauncher.launch(uri)
                }
            } catch (e: Exception) {
                expenseViewModel.setMessage("Unable to open camera: ${e.message}")
            }
        }

        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                launchCameraCapture()
            } else {
                expenseViewModel.setMessage("Camera permission denied. Please enable it in Settings.")
            }
        }

        LaunchedEffect(pendingCameraPermissionRequest) {
            if (pendingCameraPermissionRequest) {
                pendingCameraPermissionRequest = false

                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    launchCameraCapture()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        LaunchedEffect(saveSuccess) {
            if (saveSuccess) {
                expenseViewModel.clearSaveSuccess()

                navController.navigate("daily_report") {
                    popUpTo("add_transaction") { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BrandGreen
                    )
                }

                Text(
                    text = "New Transaction",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = painterResource(id = R.drawable.bb_og),
                contentDescription = "BudgetBuddi logo",
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "R",
                    fontSize = 28.sp,
                    color = if (amountError != null) ErrorRed else Color.Gray,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val filtered = input.filter { c -> c.isDigit() || c == '.' }
                        val dotCount = filtered.count { it == '.' }

                        if (dotCount <= 1) {
                            amountText = filtered
                        }
                    },
                    placeholder = {
                        Text("0", fontSize = 28.sp, color = Color.LightGray)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    isError = amountError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.width(180.dp)
                )
            }

            if (amountError != null) {
                Text(
                    text = amountError!!,
                    color = ErrorRed,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE0E0E0))
                    .padding(3.dp)
            ) {
                listOf("expense" to "Expense", "income" to "Income").forEach { (value, label) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selectedType == value) BrandGreen else Color.Transparent)
                            .clickable {
                                selectedType = value
                                selectedMainCategory = ""
                                selectedSubCategory = ""
                                categoryError = null
                                expandedMainCategoryInDialog = null
                            }
                            .padding(horizontal = 28.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selectedType == value) Color.White else Color.Gray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            FormRow(
                icon = Icons.Default.Category,
                label = "Category :",
                value = if (selectedSubCategory.isNotBlank()) {
                    "$selectedMainCategory / $selectedSubCategory"
                } else {
                    selectedMainCategory
                },
                placeholder = if (selectedType == "income") {
                    "Select income category"
                } else {
                    "Select expense category"
                },
                onClick = { showCategoryDialog = true },
                trailingIcon = Icons.Default.List,
                hasError = categoryError != null
            )

            if (categoryError != null) {
                Text(
                    text = categoryError!!,
                    color = ErrorRed,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 28.dp, bottom = 4.dp)
                )
            }

            Divider(
                color = if (categoryError != null) ErrorRed else BrandGreen.copy(alpha = 0.4f),
                thickness = 1.dp
            )

            FormRow(
                icon = Icons.Default.CalendarMonth,
                label = "Date :",
                value = selectedDate,
                placeholder = "Pick a date",
                onClick = { showDatePicker = true },
                hasError = dateError != null
            )

            if (dateError != null) {
                Text(
                    text = dateError!!,
                    color = ErrorRed,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 28.dp, bottom = 4.dp)
                )
            }

            Divider(
                color = if (dateError != null) ErrorRed else BrandGreen.copy(alpha = 0.4f),
                thickness = 1.dp
            )

            FormRow(
                icon = Icons.Default.CreditCard,
                label = "Payment Method :",
                value = paymentMethod,
                placeholder = "Select payment",
                onClick = { showPaymentDialog = true }
            )

            Divider(color = BrandGreen.copy(alpha = 0.4f), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Note,
                    contentDescription = null,
                    tint = BrandGreen,
                    modifier = Modifier
                        .padding(top = 14.dp, end = 8.dp)
                        .size(20.dp)
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        if (it.length <= 300) {
                            note = it
                        }
                    },
                    label = { Text("Note") },
                    placeholder = { Text("Add a note...") },
                    minLines = 2,
                    maxLines = 4,
                    supportingText = {
                        Text(
                            text = "${note.length}/300",
                            modifier = Modifier.fillMaxWidth(),
                            color = if (note.length >= 280) Color(0xFFF57C00) else Color.Gray,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandGreen,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            Divider(color = BrandGreen.copy(alpha = 0.4f), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable { showPhotoDialog = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = if (selectedImageUri != null) "Photo attached ✓" else "Add Photo :",
                            color = if (selectedImageUri != null) BrandGreen else Color.Gray
                        )

                        Text(
                            text = "Optional",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Add photo",
                    tint = BrandGreen,
                    modifier = Modifier
                        .size(28.dp)
                        .background(LightGreen, RoundedCornerShape(6.dp))
                        .padding(4.dp)
                )
            }

            selectedImageUri?.let { uri ->
                Spacer(modifier = Modifier.height(8.dp))

                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Selected transaction photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BrandGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        selectedImageUri = null
                        photoUriString = null
                        cameraUri = null
                    }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        "Remove photo",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Divider(color = BrandGreen.copy(alpha = 0.4f), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Recurring :")
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE0E0E0))
                        .padding(2.dp)
                ) {
                    listOf(false to "Off", true to "On").forEach { (value, label) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isRecurring == value) BrandGreen else Color.Transparent)
                                .clickable { isRecurring = value }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isRecurring == value) Color.White else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            uiMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))

                val isSuccess = message.contains("success", ignoreCase = true)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSuccess) LightGreen else ErrorBackground)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isSuccess) BrandGreen else ErrorRed,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = message,
                        color = if (isSuccess) BrandGreen else ErrorRed,
                        fontSize = 13.sp
                    )
                }

                LaunchedEffect(message) {
                    delay(3500)
                    expenseViewModel.clearMessage()
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isUploadingImage) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = BrandGreen,
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Uploading photo...",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }

            Button(
                enabled = !isUploadingImage,
                onClick = {
                    var isValid = true

                    val parsedAmount = amountText.toDoubleOrNull()

                    when {
                        amountText.isBlank() -> {
                            amountError = "Amount is required"
                            isValid = false
                        }

                        parsedAmount == null -> {
                            amountError = "Enter a valid number"
                            isValid = false
                        }

                        parsedAmount <= 0.0 -> {
                            amountError = "Amount must be greater than zero"
                            isValid = false
                        }

                        parsedAmount > 9_999_999.99 -> {
                            amountError = "Amount is too large"
                            isValid = false
                        }

                        else -> amountError = null
                    }

                    if (selectedMainCategory.isBlank()) {
                        categoryError = "Please select a category"
                        isValid = false
                    } else {
                        categoryError = null
                    }

                    if (selectedDate.isBlank()) {
                        dateError = "Please select a date"
                        isValid = false
                    } else {
                        dateError = null
                    }

                    if (isValid) {
                        expenseViewModel.saveExpense(
                            amount = parsedAmount!!,
                            type = selectedType,
                            category = selectedMainCategory,
                            subCategory = selectedSubCategory,
                            date = selectedDate,
                            note = note.trim(),
                            photoPath = photoUriString,
                            paymentMethod = paymentMethod,
                            isRecurring = isRecurring
                        )
                    }
                },
                modifier = Modifier
                    .width(160.dp)
                    .height(46.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showCategoryDialog) {
            AlertDialog(
                onDismissRequest = {
                    showCategoryDialog = false
                    expandedMainCategoryInDialog = null
                },
                title = {
                    Text(
                        text = if (selectedType == "income") {
                            "Select Income Category"
                        } else {
                            "Select Expense Category"
                        }
                    )
                },
                text = {
                    if (mainCategoriesForSelectedType.isEmpty()) {
                        Text(
                            text = if (selectedType == "income") {
                                "No income categories found. Add income categories first."
                            } else {
                                "No expense categories found. Add expense categories first."
                            }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            mainCategoriesForSelectedType.forEach { main ->
                                val isExpanded = expandedMainCategoryInDialog == main

                                val subs = categoriesForSelectedType
                                    .filter { it.mainCategory == main }
                                    .sortedBy { it.subCategory }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFE8F5E9))
                                            .clickable {
                                                expandedMainCategoryInDialog =
                                                    if (isExpanded) null else main
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = main,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandGreen,
                                            fontSize = 15.sp,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Icon(
                                            imageVector = if (isExpanded) {
                                                Icons.Default.KeyboardArrowUp
                                            } else {
                                                Icons.Default.KeyboardArrowDown
                                            },
                                            contentDescription = if (isExpanded) {
                                                "Collapse category"
                                            } else {
                                                "Expand category"
                                            },
                                            tint = BrandGreen
                                        )
                                    }

                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(6.dp))

                                        subs.forEach { category ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        selectedMainCategory = category.mainCategory
                                                        selectedSubCategory = category.subCategory
                                                        showCategoryDialog = false
                                                        categoryError = null
                                                        expandedMainCategoryInDialog = null
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CategoryVisualPreview(
                                                    visualType = category.visualType,
                                                    visualValue = category.visualValue,
                                                    outlineColor = category.outlineColor,
                                                    modifier = Modifier.size(42.dp)
                                                )

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column(
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = category.subCategory,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.DarkGray,
                                                        fontSize = 14.sp
                                                    )

                                                    Text(
                                                        text = category.mainCategory,
                                                        color = Color.Gray,
                                                        fontSize = 11.sp
                                                    )
                                                }

                                                if (
                                                    selectedMainCategory == category.mainCategory &&
                                                    selectedSubCategory == category.subCategory
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Selected",
                                                        tint = BrandGreen,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCategoryDialog = false
                            expandedMainCategoryInDialog = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showPaymentDialog) {
            AlertDialog(
                onDismissRequest = { showPaymentDialog = false },
                title = { Text("Select Payment Method") },
                text = {
                    Column {
                        listOf("Cash", "Card", "Bank Transfer", "E-Wallet", "Other").forEach { method ->
                            Text(
                                text = method,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        paymentMethod = method
                                        showPaymentDialog = false
                                    }
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showPaymentDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showPhotoDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoDialog = false },
                title = { Text("Add Photo") },
                text = {
                    Column {
                        Text(
                            text = "Choose how you want to attach a photo.",
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                showPhotoDialog = false

                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose from gallery")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                showPhotoDialog = false
                                pendingCameraPermissionRequest = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take photo")
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showPhotoDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDatePicker) {
            TransactionDatePickerDialog(
                onDateSelected = { date ->
                    selectedDate = date
                    showDatePicker = false
                    dateError = null
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis

                    if (millis != null) {
                        val formattedDate = SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).format(Date(millis))

                        onDateSelected(formattedDate)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun FormRow(
    icon: ImageVector,
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null,
    hasError: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (hasError) ErrorRed else BrandGreen,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = label,
                color = if (hasError) ErrorRed else Color.DarkGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = value.ifBlank { placeholder },
                color = when {
                    hasError -> ErrorRed
                    value.isBlank() -> Color.LightGray
                    else -> Color.Black
                },
                fontSize = 14.sp
            )
        }

        trailingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = if (hasError) ErrorRed else BrandGreen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun createImageFile(context: Context): File {
    val timestamp = SimpleDateFormat(
        "yyyyMMdd_HHmmss",
        Locale.getDefault()
    ).format(Date())

    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    return File.createTempFile(
        "EXPENSE_${timestamp}_",
        ".jpg",
        storageDir
    )
}