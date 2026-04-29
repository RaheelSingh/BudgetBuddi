/*

2024 Coil Contributors. “Compose - Coil.” Github.io, 2024, coil-kt.github.io/coil/compose/. Accessed 29 Apr. 2026.

“Access App-Specific Files.” Android Developers, 2026, developer.android.com/training/data-storage/app-specific. Accessed 29 Apr. 2026.

“Getting a Result from an Activity.” Android Developers, 1 Jan. 2026, developer.android.com/training/basics/intents/result. Accessed 29 Apr. 2026.

Google. “ViewModel Overview  |  Android Developers.” Android Developers, 2019, developer.android.com/topic/libraries/architecture/viewmodel. Accessed 29 Apr. 2026.

“Navigation with Compose | Jetpack Compose.” Android Developers, developer.android.com/develop/ui/compose/navigation. Accessed 29 Apr. 2026.

“Photo Picker.” Android Developers, 2025, developer.android.com/training/data-storage/shared/photo-picker. Accessed 29 Apr. 2026.

“Request App Permissions.” Android Developers, 2026, developer.android.com/training/permissions/requesting. Accessed 29 Apr. 2026.

“SimpleDateFormat  |  API Reference  |  Android Developers.” Android Developers, 2026, developer.android.com/reference/java/text/SimpleDateFormat. Accessed 29 Apr. 2026.

“State and Jetpack Compose.” Android Developers, 2024, developer.android.com/develop/ui/compose/state. Accessed 29 Apr. 2026.
 */



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
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        // Validation error states
        var amountError by remember { mutableStateOf<String?>(null) }
        var categoryError by remember { mutableStateOf<String?>(null) }
        var dateError by remember { mutableStateOf<String?>(null) }

        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var photoPath by remember { mutableStateOf<String?>(null) }
        var cameraUri by remember { mutableStateOf<Uri?>(null) }
        var pendingCameraPath by remember { mutableStateOf<String?>(null) }

        var showDatePicker by remember { mutableStateOf(false) }
        var showCategoryDialog by remember { mutableStateOf(false) }
        var showPaymentDialog by remember { mutableStateOf(false) }
        var showPhotoDialog by remember { mutableStateOf(false) }

        val allCategories by categoryViewModel.allCategories.collectAsState()
        val mainCategories by categoryViewModel.mainCategories.collectAsState()

        val saveSuccess by expenseViewModel.saveSuccess.collectAsState()
        val uiMessage by expenseViewModel.uiMessage.collectAsState()

        // Clear field errors as user corrects them
        LaunchedEffect(amountText) {
            if (amountText.isNotBlank()) amountError = null
        }
        LaunchedEffect(selectedMainCategory) {
            if (selectedMainCategory.isNotBlank()) categoryError = null
        }
        LaunchedEffect(selectedDate) {
            if (selectedDate.isNotBlank()) dateError = null
        }

        val photoPickerLauncher = rememberLauncherForActivityResult(  //Handles external app results safely (camera, gallery, permissions) without deprecated APIs.
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                photoPath = uri.toString()
            }
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                selectedImageUri = cameraUri
                photoPath = pendingCameraPath
            }
        }

        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                val file = createImageFile(context)
                pendingCameraPath = file.absolutePath
                cameraUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                cameraUri?.let { cameraLauncher.launch(it) }
            } else {
                expenseViewModel.setMessage("Camera permission denied. Please enable it in Settings.")
            }
        }

        LaunchedEffect(saveSuccess) {
            if (saveSuccess) {
                expenseViewModel.clearSaveSuccess()
                navController.popBackStack() //Handles screen navigation in the app.
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
                IconButton(onClick = { navController.popBackStack() }) {
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
                        // Allow only valid decimal input; prevent multiple dots
                        val filtered = input.filter { c -> c.isDigit() || c == '.' }
                        val dotCount = filtered.count { it == '.' }
                        if (dotCount <= 1) amountText = filtered
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
                            .clickable { selectedType = value }
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
                placeholder = "Select category",
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

            Divider(color = if (categoryError != null) ErrorRed else BrandGreen.copy(alpha = 0.4f), thickness = 1.dp)

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

            Divider(color = if (dateError != null) ErrorRed else BrandGreen.copy(alpha = 0.4f), thickness = 1.dp)

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
                    onValueChange = { if (it.length <= 300) note = it },
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
                        Text(text = "Optional", color = Color.LightGray, fontSize = 11.sp)
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
                    painter = rememberAsyncImagePainter(uri),  //Loads and displays images efficiently from a URI.
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
                        photoPath = null
                        cameraUri = null
                        pendingCameraPath = null
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Remove photo", color = MaterialTheme.colorScheme.error)
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
                    expenseViewModel.clearMessage()
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    var isValid = true

                    val parsedAmount = amountText.toDoubleOrNull()  //Ensures user input is: Numeric Valid
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
                            startTime = "",
                            endTime = "",
                            note = note.trim(),
                            photoPath = photoPath,
                            paymentMethod = paymentMethod,
                            isRecurring = isRecurring
                        )
                    }
                },
                modifier = Modifier
                    .width(160.dp)
                    .height(46.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                Text(text = "Save", color = Color.Black, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) //Formats the selected date into a readable string format.
                            selectedDate = sdf.format(Date(millis))
                            dateError = null
                        } else {
                            dateError = "Please select a date"
                        }
                        showDatePicker = false
                    }) {
                        Text("OK", color = BrandGreen)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showCategoryDialog) {
            var expandedMain by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCategoryDialog = false },
                confirmButton = {
                    TextButton(onClick = { showCategoryDialog = false }) {
                        Text("Done", color = BrandGreen)
                    }
                },
                title = { Text("Select Category") },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (mainCategories.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No categories available", color = Color.Gray)
                            }
                        } else {
                            mainCategories.forEach { main ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedMain = if (expandedMain == main) "" else main
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(main, fontWeight = FontWeight.SemiBold)
                                    Icon(
                                        imageVector = if (expandedMain == main) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = BrandGreen
                                    )
                                }
                                if (expandedMain == main) {
                                    val subs = allCategories.filter { it.mainCategory == main }
                                    if (subs.isEmpty()) {
                                        Text(
                                            text = "No subcategories",
                                            color = Color.Gray,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
                                        )
                                    } else {
                                        subs.forEach { cat ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedMainCategory = main
                                                        selectedSubCategory = cat.subCategory
                                                        categoryError = null
                                                        showCategoryDialog = false
                                                    }
                                                    .padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = cat.visualValue, modifier = Modifier.width(28.dp))
                                                Text(cat.subCategory)
                                                if (selectedSubCategory == cat.subCategory && selectedMainCategory == main) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = BrandGreen)
                                                }
                                            }
                                        }
                                    }
                                }
                                Divider()
                            }
                        }
                    }
                }
            )
        }

        if (showPaymentDialog) {
            val methods = listOf("Cash", "Card", "Bank Transfer", "Other")
            AlertDialog(
                onDismissRequest = { showPaymentDialog = false },
                confirmButton = {
                    TextButton(onClick = { showPaymentDialog = false }) {
                        Text("Done", color = BrandGreen)
                    }
                },
                title = { Text("Payment Method") },
                text = {
                    Column {
                        methods.forEach { method ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        paymentMethod = method
                                        showPaymentDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(method)
                                if (paymentMethod == method) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = BrandGreen)
                                }
                            }
                            Divider()
                        }
                    }
                }
            )
        }

        if (showPhotoDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoDialog = false },
                confirmButton = {},
                title = { Text("Add Photo") },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                showPhotoDialog = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)  //Allows users to select images from their device securely.
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null, tint = BrandGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose from Gallery", color = Color.Black)
                        }
                        TextButton(
                            onClick = {
                                showPhotoDialog = false
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    val file = createImageFile(context)
                                    pendingCameraPath = file.absolutePath
                                    cameraUri = FileProvider.getUriForFile(  //Generates a secure URI to share a file with the camera app.
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    cameraUri?.let { cameraLauncher.launch(it) }
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrandGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take Photo", color = Color.Black)
                        }
                    }
                }
            )
        }
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
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (hasError) ErrorRed else BrandGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = if (hasError) ErrorRed else Color.DarkGray, fontSize = 14.sp)
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
            Icon(imageVector = it, contentDescription = null, tint = if (hasError) ErrorRed else BrandGreen, modifier = Modifier.size(18.dp))
        }
    }
}

private fun createImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) //Stores images in app-specific external storage.
    return File.createTempFile("EXPENSE_${timestamp}_", ".jpg", storageDir)
}