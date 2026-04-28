package group12.budgetbuddi.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions

private val BrandGreen = Color(0xFF4CAF50)
private val LightGreen = Color(0xFFE8F5E9)

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

        var selectedType by remember { mutableStateOf("expense") } // "expense" | "income"
        var amountText by remember { mutableStateOf("") }
        var selectedDate by remember { mutableStateOf("") }
        var startTime by remember { mutableStateOf("") }
        var endTime by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        var selectedMainCategory by remember { mutableStateOf("") }
        var selectedSubCategory by remember { mutableStateOf("") }
        var paymentMethod by remember { mutableStateOf("Cash") }
        var isRecurring by remember { mutableStateOf(false) }
        var photoPath by remember { mutableStateOf<String?>(null) }

        var showDatePicker by remember { mutableStateOf(false) }
        var showStartTimePicker by remember { mutableStateOf(false) }
        var showEndTimePicker by remember { mutableStateOf(false) }
        var showCategoryDialog by remember { mutableStateOf(false) }
        var showPaymentDialog by remember { mutableStateOf(false) }
        var showPhotoDialog by remember { mutableStateOf(false) }

        val allCategories by categoryViewModel.allCategories.collectAsState()
        val mainCategories by categoryViewModel.mainCategories.collectAsState()

        var cameraUri by remember { mutableStateOf<Uri?>(null) }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                photoPath = cameraUri?.path
            }
        }

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                photoPath = getRealPathFromUri(context, it)
            }
        }

        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                val file = createImageFile(context)
                cameraUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                cameraLauncher.launch(cameraUri!!)
            }
        }

        val saveSuccess by expenseViewModel.saveSuccess.collectAsState()
        val uiMessage by expenseViewModel.uiMessage.collectAsState()

        LaunchedEffect(saveSuccess) {
            if (saveSuccess) {
                expenseViewModel.clearSaveSuccess()
                navController.popBackStack()
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

            // Close button row
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

            // Amount display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "R",
                    fontSize = 28.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    placeholder = { Text("0", fontSize = 28.sp, color = Color.LightGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.width(180.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expense / Income toggle
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
                            .background(
                                if (selectedType == value) BrandGreen else Color.Transparent
                            )
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
                value = if (selectedSubCategory.isNotBlank())
                    "$selectedMainCategory / $selectedSubCategory"
                else selectedMainCategory,
                placeholder = "Select category",
                onClick = { showCategoryDialog = true },
                trailingIcon = Icons.Default.List
            )

            Divider(color = BrandGreen.copy(alpha = 0.4f), thickness = 1.dp)

            FormRow(
                icon = Icons.Default.CalendarMonth,
                label = "Date :",
                value = selectedDate,
                placeholder = "Pick a date",
                onClick = { showDatePicker = true }
            )

            Divider(color = BrandGreen.copy(alpha = 0.4f), thickness = 1.dp)

            FormRow(
                icon = Icons.Default.AccessTime,
                label = "Start Time :",
                value = startTime,
                placeholder = "Pick start time",
                onClick = { showStartTimePicker = true }
            )

            Divider(color = BrandGreen.copy(alpha = 0.4f), thickness = 1.dp)

            FormRow(
                icon = Icons.Default.AccessTime,
                label = "End Time :",
                value = endTime,
                placeholder = "Pick end time",
                onClick = { showEndTimePicker = true }
            )

            Divider(color = BrandGreen.copy(alpha = 0.4f), thickness = 1.dp)

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
                    modifier = Modifier.padding(top = 14.dp, end = 8.dp).size(20.dp)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    placeholder = { Text("Add a note...") },
                    minLines = 2,
                    maxLines = 4,
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
                    Text(
                        text = if (photoPath != null) "Photo attached ✓" else "Add Photo :",
                        color = if (photoPath != null) BrandGreen else Color.Gray
                    )
                }
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = BrandGreen,
                    modifier = Modifier
                        .size(28.dp)
                        .background(LightGreen, RoundedCornerShape(6.dp))
                        .padding(4.dp)
                )
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
                                .background(
                                    if (isRecurring == value) BrandGreen else Color.Transparent
                                )
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

            // Error message
            uiMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                LaunchedEffect(it) { expenseViewModel.clearMessage() }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    expenseViewModel.saveExpense(
                        amount = amount,
                        type = selectedType,
                        category = selectedMainCategory,
                        subCategory = selectedSubCategory,
                        date = selectedDate,
                        startTime = startTime,
                        endTime = endTime,
                        note = note,
                        photoPath = photoPath,
                        paymentMethod = paymentMethod,
                        isRecurring = isRecurring
                    )
                },
                modifier = Modifier
                    .width(160.dp)
                    .height(46.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            selectedDate = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }) { Text("OK", color = BrandGreen) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showStartTimePicker) {
            val timeState = rememberTimePickerState()
            AlertDialog(
                onDismissRequest = { showStartTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        startTime = "%02d:%02d".format(timeState.hour, timeState.minute)
                        showStartTimePicker = false
                    }) { Text("OK", color = BrandGreen) }
                },
                dismissButton = {
                    TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
                },
                title = { Text("Start Time") },
                text = { TimePicker(state = timeState) }
            )
        }

        if (showEndTimePicker) {
            val timeState = rememberTimePickerState()
            AlertDialog(
                onDismissRequest = { showEndTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        endTime = "%02d:%02d".format(timeState.hour, timeState.minute)
                        showEndTimePicker = false
                    }) { Text("OK", color = BrandGreen) }
                },
                dismissButton = {
                    TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
                },
                title = { Text("End Time") },
                text = { TimePicker(state = timeState) }
            )
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
                    Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
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
                                    imageVector = if (expandedMain == main)
                                        Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = BrandGreen
                                )
                            }
                            if (expandedMain == main) {
                                val subs = allCategories.filter { it.mainCategory == main }
                                subs.forEach { cat ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedMainCategory = main
                                                selectedSubCategory = cat.subCategory
                                                showCategoryDialog = false
                                            }
                                            .padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(cat.visualValue, modifier = Modifier.width(28.dp))
                                        Text(cat.subCategory)
                                        if (selectedSubCategory == cat.subCategory && selectedMainCategory == main) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(Icons.Default.Check, contentDescription = null, tint = BrandGreen)
                                        }
                                    }
                                }
                            }
                            Divider()
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
                                val hasCameraPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasCameraPermission) {
                                    val file = createImageFile(context)
                                    cameraUri = FileProvider.getUriForFile(
                                        context, "${context.packageName}.provider", file
                                    )
                                    cameraLauncher.launch(cameraUri!!)
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
                        TextButton(
                            onClick = {
                                showPhotoDialog = false
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null, tint = BrandGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose from Gallery", color = Color.Black)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun FormRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
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
                tint = BrandGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = Color.DarkGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value.ifBlank { placeholder },
                color = if (value.isBlank()) Color.LightGray else Color.Black,
                fontSize = 14.sp
            )
        }
        trailingIcon?.let {
            Icon(imageVector = it, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(18.dp))
        }
    }
}

private fun createImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("EXPENSE_${timestamp}_", ".jpg", storageDir)
}

private fun getRealPathFromUri(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(storageDir, "GALLERY_${timestamp}.jpg")
        file.outputStream().use { inputStream.copyTo(it) }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}