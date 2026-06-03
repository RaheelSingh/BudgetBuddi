package group12.budgetbuddi.ui.screens

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import group12.budgetbuddi.data.entity.ExpenseEntity
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BrandGreenDark as BudgetGreen
import group12.budgetbuddi.ui.theme.TextBlack
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.io.File
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val LightGreenBg = Color(0xFFE8F5E9)
private val ExpenseRed = Color(0xFFE53935)
private val IncomeGreen = Color(0xFF2E7D32)
private val SoftBorder = Color(0xFFE0EDE8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val context = LocalContext.current
    val allTransactions by expenseViewModel.allExpenses.collectAsState()

    var startDateString by remember { mutableStateOf<String?>(null) }
    var endDateString by remember { mutableStateOf<String?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    val displayDateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val filteredTransactions = remember(allTransactions, startDateString, endDateString) {
        allTransactions
            .filter { transaction ->
                val date = transaction.date
                val afterStart = startDateString?.let { date >= it } ?: true
                val beforeEnd = endDateString?.let { date <= it } ?: true
                afterStart && beforeEnd
            }
            .sortedWith(
                compareByDescending<ExpenseEntity> { it.date }
                    .thenByDescending { it.createdAt }
            )
    }

    val groupedByDate = remember(filteredTransactions) {
        filteredTransactions
            .groupBy { it.date }
            .toSortedMap(compareByDescending { it })
            .map { it.key to it.value }
    }

    val totalExpenses = remember(filteredTransactions) {
        filteredTransactions
            .filter { it.type.equals("expense", ignoreCase = true) }
            .sumOf { it.amount }
    }

    val totalIncome = remember(filteredTransactions) {
        filteredTransactions
            .filter { it.type.equals("income", ignoreCase = true) }
            .sumOf { it.amount }
    }

    val netTotal = totalIncome - totalExpenses

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(350),
        label = "contentAlpha"
    )

    fun clearFilters() {
        startDateString = null
        endDateString = null
    }

    AppScaffold(
        navController = navController,
        selectedRoute = "expense_list",
        authViewModel = authViewModel
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(LightGreenBg)
                .graphicsLayer(alpha = contentAlpha)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                color = BudgetGreen
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Transaction List",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Expenses and income, grouped by date",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.82f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TransactionTotalCard(
                            label = "Income",
                            amount = totalIncome,
                            iconColor = IncomeGreen,
                            icon = Icons.Default.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                        TransactionTotalCard(
                            label = "Expenses",
                            amount = totalExpenses,
                            iconColor = ExpenseRed,
                            icon = Icons.Default.TrendingDown,
                            modifier = Modifier.weight(1f)
                        )
                        TransactionTotalCard(
                            label = "Net",
                            amount = netTotal,
                            iconColor = if (netTotal >= 0) IncomeGreen else ExpenseRed,
                            icon = Icons.Default.Wallet,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = startDateString?.let { formatTransactionDate(it) } ?: "Start date",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = endDateString?.let { formatTransactionDate(it) } ?: "End date",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (startDateString != null || endDateString != null) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = true,
                                onClick = {},
                                label = {
                                    Text(
                                        text = "${startDateString?.let { formatTransactionDate(it) } ?: "Any"} - ${endDateString?.let { formatTransactionDate(it) } ?: "Any"}",
                                        fontSize = 12.sp
                                    )
                                }
                            )

                            TextButton(onClick = { clearFilters() }) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear", color = BudgetGreen)
                            }
                        }
                    }
                }
            }

            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.45f),
                            modifier = Modifier.size(70.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No transactions found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )
                        Text(
                            text = "Try clearing the date filter or adding a transaction.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedByDate.forEach { (date, transactionsForDate) ->
                        item(key = "header_$date") {
                            TransactionDateHeader(
                                date = date,
                                transactions = transactionsForDate
                            )
                        }

                        items(
                            items = transactionsForDate,
                            key = { it.id.ifBlank { "${it.date}_${it.createdAt}_${it.amount}" } }
                        ) { transaction ->
                            TransactionListItem(
                                transaction = transaction,
                                onClick = {},
                                onPhotoClick = { photoPath ->
                                    val encodedPath = URLEncoder.encode(photoPath, "UTF-8")
                                    navController.navigate("photo_viewer/$encodedPath")
                                },
                                onDeleteClick = {
                                    transactionToDelete = transaction
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStartDatePicker) {
        TransactionDatePickerDialog(
            title = "Select Start Date",
            onDateSelected = { date ->
                startDateString = date
                if (endDateString != null && endDateString!! < date) {
                    endDateString = null
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        TransactionDatePickerDialog(
            title = "Select End Date",
            onDateSelected = { date ->
                if (startDateString != null && date < startDateString!!) {
                    startDateString = date
                    endDateString = date
                } else {
                    endDateString = date
                }
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    if (showDeleteDialog && transactionToDelete != null) {
        val transaction = transactionToDelete!!
        val typeLabel = if (transaction.type.equals("income", ignoreCase = true)) "income" else "expense"

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                transactionToDelete = null
            },
            title = { Text("Delete Transaction") },
            text = {
                Text("Are you sure you want to delete this $typeLabel transaction?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseViewModel.deleteExpense(transaction)
                        showDeleteDialog = false
                        transactionToDelete = null
                    }
                ) {
                    Text("Delete", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        transactionToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TransactionTotalCard(
    label: String,
    amount: Double,
    iconColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = iconColor.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                label,
                fontSize = 9.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (label == "Net" && amount < 0) "-R${String.format("%.0f", kotlin.math.abs(amount))}" else "R${String.format("%.0f", kotlin.math.abs(amount))}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TransactionDateHeader(
    date: String,
    transactions: List<ExpenseEntity>
) {
    val expensesTotal = transactions
        .filter { it.type.equals("expense", ignoreCase = true) }
        .sumOf { it.amount }

    val incomeTotal = transactions
        .filter { it.type.equals("income", ignoreCase = true) }
        .sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = formatTransactionDateLong(date),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
                Text(
                    text = "${transactions.size} transaction${if (transactions.size == 1) "" else "s"}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (incomeTotal > 0) {
                    Text(
                        text = "+R${String.format("%.2f", incomeTotal)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IncomeGreen
                    )
                }
                if (expensesTotal > 0) {
                    Text(
                        text = "-R${String.format("%.2f", expensesTotal)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ExpenseRed
                    )
                }
            }
        }

        HorizontalDivider(color = BudgetGreen.copy(alpha = 0.18f))
    }
}

@Composable
private fun TransactionListItem(
    transaction: ExpenseEntity,
    onClick: () -> Unit,
    onPhotoClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val isIncome = transaction.type.equals("income", ignoreCase = true)
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed
    val amountPrefix = if (isIncome) "+R" else "-R"
    val typeLabel = if (isIncome) "Income" else "Expense"
    val categoryColor = getTransactionCategoryColor(transaction.category, isIncome)
    val hasPhoto = !transaction.photoPath.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SoftBorder),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ExpenseRed,
                    modifier = Modifier.size(19.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 34.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = categoryColor.copy(alpha = 0.13f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isIncome) "💰" else getTransactionCategoryEmoji(transaction.category),
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = transaction.category.ifBlank { typeLabel },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = CircleShape,
                            color = amountColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = typeLabel,
                                fontSize = 10.sp,
                                color = amountColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (transaction.subCategory.isNotBlank()) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = transaction.subCategory,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = transaction.paymentMethod.ifBlank { "No payment method" },
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (transaction.note.isNotBlank()) {
                        Text(
                            text = transaction.note,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (transaction.isRecurring) {
                        Text(
                            text = "Recurring transaction",
                            fontSize = 11.sp,
                            color = BudgetGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (hasPhoto) {
                        TransactionPhotoThumb(
                            photoPath = transaction.photoPath!!,
                            onClick = { onPhotoClick(transaction.photoPath!!) }
                        )
                    }

                    Text(
                        text = "$amountPrefix${String.format("%.2f", transaction.amount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = amountColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}


@Composable
private fun TransactionPhotoThumb(
    photoPath: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val imageModel: Any? = remember(photoPath) {
        when {
            photoPath.startsWith("https://") || photoPath.startsWith("http://") -> photoPath
            photoPath.startsWith("content://") -> Uri.parse(photoPath)
            else -> {
                val file = File(photoPath)
                if (file.exists()) Uri.fromFile(file) else null
            }
        }
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, SoftBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageModel != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageModel)
                    .crossfade(true)
                    .build(),
                contentDescription = "Transaction photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Photo missing",
                tint = Color.Gray.copy(alpha = 0.45f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun TransactionDatePickerDialog(
    title: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val outputFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val monthTitleFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val today = remember { Calendar.getInstance() }
    var visibleMonth by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
            }
        )
    }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }

    val daysInMonth = visibleMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOffset = remember(visibleMonth.timeInMillis) {
        val first = visibleMonth.clone() as Calendar
        first.set(Calendar.DAY_OF_MONTH, 1)
        first.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthTitleFormat.format(visibleMonth.time),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextBlack
                    )
                    Row {
                        IconButton(
                            onClick = {
                                visibleMonth = (visibleMonth.clone() as Calendar).apply {
                                    add(Calendar.MONTH, -1)
                                    set(Calendar.DAY_OF_MONTH, 1)
                                }
                            }
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = BudgetGreen)
                        }
                        IconButton(
                            onClick = {
                                visibleMonth = (visibleMonth.clone() as Calendar).apply {
                                    add(Calendar.MONTH, 1)
                                    set(Calendar.DAY_OF_MONTH, 1)
                                }
                            }
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = BudgetGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val totalCells = firstDayOffset + daysInMonth
                val rowCount = ((totalCells + 6) / 7)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0 until rowCount) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0..6) {
                                val cellIndex = row * 7 + col
                                val dayNumber = cellIndex - firstDayOffset + 1

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dayNumber in 1..daysInMonth) {
                                        val cellDate = (visibleMonth.clone() as Calendar).apply {
                                            set(Calendar.DAY_OF_MONTH, dayNumber)
                                        }
                                        val isSelected = selectedDate?.let {
                                            it.get(Calendar.YEAR) == cellDate.get(Calendar.YEAR) &&
                                                    it.get(Calendar.MONTH) == cellDate.get(Calendar.MONTH) &&
                                                    it.get(Calendar.DAY_OF_MONTH) == cellDate.get(Calendar.DAY_OF_MONTH)
                                        } ?: false
                                        val isToday = today.get(Calendar.YEAR) == cellDate.get(Calendar.YEAR) &&
                                                today.get(Calendar.MONTH) == cellDate.get(Calendar.MONTH) &&
                                                today.get(Calendar.DAY_OF_MONTH) == cellDate.get(Calendar.DAY_OF_MONTH)

                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isSelected -> BudgetGreen
                                                        isToday -> BudgetGreen.copy(alpha = 0.12f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .clickable { selectedDate = cellDate },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNumber.toString(),
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else TextBlack
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            selectedDate?.let { onDateSelected(outputFormat.format(it.time)) }
                        },
                        enabled = selectedDate != null,
                        colors = ButtonDefaults.buttonColors(containerColor = BudgetGreen),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

private fun formatTransactionDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

private fun formatTransactionDateLong(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

private fun getTransactionCategoryColor(category: String, isIncome: Boolean): Color {
    if (isIncome) return IncomeGreen

    return when (category.lowercase()) {
        "food", "food & drink", "groceries", "restaurant" -> Color(0xFFFF9800)
        "transport", "car", "fuel" -> Color(0xFF2196F3)
        "shopping", "clothing" -> Color(0xFF9C27B0)
        "entertainment", "movie", "movies" -> Color(0xFFE91E63)
        "housing", "rent" -> Color(0xFF795548)
        "health", "healthcare", "medical" -> Color(0xFF4CAF50)
        "education" -> Color(0xFF009688)
        else -> BudgetGreen
    }
}

private fun getTransactionCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "food", "food & drink", "groceries", "restaurant" -> "🍔"
        "transport", "car", "fuel" -> "🚗"
        "shopping", "clothing" -> "🛍️"
        "entertainment", "movie", "movies" -> "🎬"
        "bills", "utilities" -> "📄"
        "health", "healthcare", "medical" -> "🏥"
        "education" -> "📚"
        "housing", "rent" -> "🏠"
        "travel" -> "✈️"
        else -> "💸"
    }
}
