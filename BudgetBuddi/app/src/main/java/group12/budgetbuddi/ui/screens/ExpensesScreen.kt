package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.data.entity.ExpenseEntity
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BrandGreen
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    AppScaffold(
        navController = navController,
        selectedRoute = "expenses",
        authViewModel = authViewModel
    ) { modifier ->
        val allTransactions by expenseViewModel.allExpenses.collectAsState()
        var showDeleteDialog by remember { mutableStateOf(false) }
        var transactionToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Saved Transactions", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { navController.navigate("add_transaction") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add transaction", tint = BrandGreen)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (allTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No saved transactions yet", color = Color.Gray, fontSize = 15.sp)
                        Text("Add an expense or income to test database saving.", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(allTransactions) { transaction ->
                        SimpleTransactionCard(
                            transaction = transaction,
                            onDeleteClick = {
                                transactionToDelete = transaction
                                showDeleteDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        if (showDeleteDialog && transactionToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Transaction") },
                text = { Text("Are you sure you want to delete this transaction?") },
                confirmButton = {
                    TextButton(onClick = {
                        transactionToDelete?.let { expenseViewModel.deleteExpense(it) }
                        showDeleteDialog = false
                        transactionToDelete = null
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
            )
        }
    }
}

@Composable
private fun SimpleTransactionCard(
    transaction: ExpenseEntity,
    onDeleteClick: () -> Unit
) {
    val isIncome = transaction.type == "income"
    val amountColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
    val amountPrefix = if (isIncome) "+R" else "-R"

    val displayDate = remember(transaction.date) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val parsedDate = inputFormat.parse(transaction.date)
            if (parsedDate != null) outputFormat.format(parsedDate) else transaction.date
        } catch (e: Exception) { transaction.date }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, BrandGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = transaction.category, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (transaction.subCategory.isNotBlank()) {
                            Text(text = transaction.subCategory, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(text = "$amountPrefix%.2f".format(transaction.amount), color = amountColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = transaction.type.replaceFirstChar { it.uppercase() }, color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(text = displayDate, color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Payment: ${transaction.paymentMethod}", color = Color.DarkGray, fontSize = 12.sp)
            if (transaction.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = transaction.note, color = Color.Gray, fontSize = 12.sp)
            }
            if (!transaction.photoPath.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Photo attached", color = BrandGreen, fontSize = 12.sp)
                }
            }
            if (transaction.isRecurring) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Recurring transaction", color = BrandGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}