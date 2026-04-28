package group12.budgetbuddi.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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

        // Form state
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

        // Dialog flags
        var showDatePicker by remember { mutableStateOf(false) }
        var showStartTimePicker by remember { mutableStateOf(false) }
        var showEndTimePicker by remember { mutableStateOf(false) }
        var showCategoryDialog by remember { mutableStateOf(false) }
        var showPaymentDialog by remember { mutableStateOf(false) }
        var showPhotoDialog by remember { mutableStateOf(false) }

        // Data
        val allCategories by categoryViewModel.allCategories.collectAsState()
        val mainCategories by categoryViewModel.mainCategories.collectAsState()

        // Save state
        val saveSuccess by expenseViewModel.saveSuccess.collectAsState()
        val uiMessage by expenseViewModel.uiMessage.collectAsState()

        LaunchedEffect(saveSuccess) {
            if (saveSuccess) {
                expenseViewModel.clearSaveSuccess()
                navController.popBackStack()
            }
        }

        // UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            Text("New Transaction", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount") }
            )

            Row {
                Button(onClick = { selectedType = "expense" }) {
                    Text("Expense")
                }
                Button(onClick = { selectedType = "income" }) {
                    Text("Income")
                }
            }

            FormRow(Icons.Default.Category, "Category", selectedMainCategory, "Select") {
                showCategoryDialog = true
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

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") }
            )

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
    }
}