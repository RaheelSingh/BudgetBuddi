package group12.budgetbuddi.ui.screens

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.data.entity.ExpenseEntity
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BrandGreenDark as BudgetGreen
import group12.budgetbuddi.ui.theme.TextBlack
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyReportScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val allTransactions by expenseViewModel.allExpenses.collectAsState()

    // Get today's date in yyyy-MM-dd format
    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Filter transactions for today only
    val todayTransactions = remember(allTransactions, today) {
        allTransactions.filter { it.date == today }
    }

    val todayExpenses = remember(todayTransactions) {
        todayTransactions.filter { it.type == "expense" }
    }

    val todayIncome = remember(todayTransactions) {
        todayTransactions.filter { it.type == "income" }
    }

    val totalExpenses = remember(todayExpenses) {
        todayExpenses.sumOf { it.amount }
    }

    val totalIncome = remember(todayIncome) {
        todayIncome.sumOf { it.amount }
    }

    val netTotal = totalIncome - totalExpenses

    val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val formattedDate = dateFormatter.format(Date())

    // Animation
    var contentVisible by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
    LaunchedEffect(Unit) { contentVisible = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "contentAlpha"
    )

    AppScaffold(
        navController = navController,
        selectedRoute = "daily_report",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E9))
                .graphicsLayer(alpha = contentAlpha)
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BudgetGreen,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Daily Report",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedDate,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (todayTransactions.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = CircleShape,
                            color = BudgetGreen.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = BudgetGreen,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }

                        Text(
                            text = "No Transactions Today",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )

                        Text(
                            text = "You haven't added any expenses or income for today.\nTap the button below to get started.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )

                        Button(
                            onClick = {
                                navController.navigate("add_transaction") {
                                    launchSingleTop = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BudgetGreen),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .width(200.dp)
                                .height(52.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Transaction", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Summary cards
                    item {
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DailySummaryCard(
                                title = "Expenses",
                                amount = totalExpenses,
                                icon = Icons.Default.TrendingDown,
                                iconColor = Color(0xFFE53935),
                                modifier = Modifier.weight(1f)
                            )

                            DailySummaryCard(
                                title = "Income",
                                amount = totalIncome,
                                icon = Icons.Default.TrendingUp,
                                iconColor = Color(0xFF43A047),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Net total card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = if (netTotal >= 0) Color(0xFF43A047) else Color(0xFFE53935),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Net Total",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextBlack
                                    )
                                }
                                Text(
                                    text = if (netTotal < 0) "-R${String.format("%.2f", kotlin.math.abs(netTotal))}" else "R${String.format("%.2f", netTotal)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netTotal >= 0) Color(0xFF43A047) else Color(0xFFE53935)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Section title for transactions
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Transactions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextBlack
                            )
                            TextButton(
                                onClick = { navController.navigate("expense_list") }
                            ) {
                                Text("View All", color = BudgetGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Transactions list
                    items(todayTransactions.sortedByDescending { it.createdAt }) { transaction ->
                        DailyTransactionItem(
                            transaction = transaction,
                            onPhotoClick = { photoPath ->
                                val encodedPath = URLEncoder.encode(photoPath, "UTF-8")
                                navController.navigate("photo_viewer/$encodedPath")
                            },
                            onClick = { },
                            onDeleteClick = {
                                transactionToDelete = transaction
                                showDeleteDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
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
                    Text("Delete", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
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
fun DailySummaryCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "R${String.format("%.2f", amount)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
        }
    }
}

@Composable
fun DailyBarChartRow(
    category: String,
    amount: Double,
    percentage: Float,
    maxAmount: Double
) {
    // Animated bar width
    val animatedWidth by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "barWidth"
    )

    val barColor = when (category.lowercase()) {
        "food", "groceries", "restaurant" -> Color(0xFFFF9800)
        "transport", "car", "fuel" -> Color(0xFF2196F3)
        "shopping", "clothing" -> Color(0xFF9C27B0)
        "entertainment", "movies" -> Color(0xFFE91E63)
        "bills", "utilities" -> Color(0xFF607D8B)
        "health", "medical" -> Color(0xFF4CAF50)
        "education" -> Color(0xFF3F51B5)
        else -> BudgetGreen
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = getDailyCategoryEmoji(category),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextBlack
                )
            }
            Text(
                text = "R${String.format("%.2f", amount)}",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bar background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF0F0F0))
        ) {
            // Animated bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(barColor)
            )

            // Percentage text overlay
            if (percentage > 0.15f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else if (percentage > 0.05f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun DailyTransactionItem(
    transaction: ExpenseEntity,
    onPhotoClick: (String) -> Unit,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isIncome = transaction.type.equals("income", ignoreCase = true)
    val amountColor = if (isIncome) Color(0xFF43A047) else Color(0xFFE53935)
    val amountPrefix = if (isIncome) "+R" else "-R"
    val typeLabel = if (isIncome) "Income" else "Expense"
    val hasPhoto = !transaction.photoPath.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    contentDescription = "Delete transaction",
                    tint = Color(0xFFE53935),
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
                    color = BudgetGreen.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isIncome) "💰" else getDailyCategoryEmoji(transaction.category),
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
                        DailyTransactionPhotoThumb(
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
private fun DailyTransactionPhotoThumb(
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
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, BudgetGreen.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
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


fun getDailyCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "food", "groceries", "restaurant" -> "🍔"
        "transport", "car", "fuel" -> "🚗"
        "shopping", "clothing" -> "🛍️"
        "entertainment", "movies" -> "🎬"
        "bills", "utilities" -> "📄"
        "health", "medical" -> "🏥"
        "education" -> "📚"
        "housing", "rent" -> "🏠"
        "travel" -> "✈️"
        "salary", "income" -> "💰"
        else -> if (category.lowercase() == "income") "💰" else "💸"
    }
}