package group12.budgetbuddi.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val BrandGreen = Color(0xFF4CAF50)
private val LightGreen = Color(0xFFE8F5E9)

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
    ) {

        val context = LocalContext.current


        var selectedType by remember { mutableStateOf("expense") }
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
        var showPaymentDialog by remember { mutableStateOf(false) }


        val allCategories by categoryViewModel.allCategories.collectAsState()
        val mainCategories by categoryViewModel.mainCategories.collectAsState()

        val saveSuccess by expenseViewModel.saveSuccess.collectAsState()
        val uiMessage by expenseViewModel.uiMessage.collectAsState()

        LaunchedEffect(saveSuccess) {
            if (saveSuccess) {
                expenseViewModel.clearSaveSuccess()
                navController.popBackStack()
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            Text("New Transaction", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row {
                Button(onClick = { selectedType = "expense" }) {
                    Text("Expense")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { selectedType = "income" }) {
                    Text("Income")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FormRow(Icons.Default.Category, "Category", selectedMainCategory, "Select") {
                // category dialog (not included in this chunk)
            }

            FormRow(Icons.Default.CalendarMonth, "Date", selectedDate, "Pick") {
                showDatePicker = true
            }

            FormRow(Icons.Default.AccessTime, "Start Time", startTime, "Pick") {
                showStartTimePicker = true
            }

            FormRow(Icons.Default.AccessTime, "End Time", endTime, "Pick") {
                showEndTimePicker = true
            }

            FormRow(Icons.Default.CreditCard, "Payment", paymentMethod, "Select") {
                showPaymentDialog = true
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                expenseViewModel.saveExpense(
                    amount = amountText.toDoubleOrNull() ?: 0.0,
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
            }) {
                Text("Save")
            }
        }


        if (showDatePicker) {
            val state = rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let {
                            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(Date(it))
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                }
            ) {
                DatePicker(state = state)
            }
        }


        if (showStartTimePicker) {
            AlertDialog(
                onDismissRequest = { showStartTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        startTime = "Selected Time"
                        showStartTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                text = { Text("Start Time Picker") }
            )
        }


        if (showEndTimePicker) {
            AlertDialog(
                onDismissRequest = { showEndTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        endTime = "Selected Time"
                        showEndTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                text = { Text("End Time Picker") }
            )
        }

        if (showPaymentDialog) {
            AlertDialog(
                onDismissRequest = { showPaymentDialog = false },
                confirmButton = {},
                text = { Text("Payment options coming soon") }
            )
        }
    }
}


@Composable
fun FormRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Icon(icon, contentDescription = null, tint = BrandGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label)
        }

        Text(
            text = if (value.isBlank()) placeholder else value,
            color = if (value.isBlank()) Color.Gray else Color.Black
        )
    }
}