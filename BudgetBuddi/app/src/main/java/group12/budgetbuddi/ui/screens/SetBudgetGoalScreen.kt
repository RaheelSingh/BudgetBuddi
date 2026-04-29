package group12.budgetbuddi.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.R
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryTotalsViewModel
import java.util.Calendar

private val BrandGreen = Color(0xFF4CAF50)
private val LightGreen = Color(0xFFE8F5E9)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetGoalScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    categoryTotalsViewModel: CategoryTotalsViewModel = viewModel()
) {
    val goalState by categoryTotalsViewModel.goalUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Local state for text fields to enable smooth typing
    var minGoalInput by remember { mutableStateOf(goalState.minGoal) }
    var maxGoalInput by remember { mutableStateOf(goalState.maxGoal) }

    // Update local inputs when goalState changes from database
    LaunchedEffect(goalState.minGoal) {
        minGoalInput = goalState.minGoal
    }
    LaunchedEffect(goalState.maxGoal) {
        maxGoalInput = goalState.maxGoal
    }

    // Show snackbar when saved
    LaunchedEffect(goalState.isSaved) {
        if (goalState.isSaved) {
            snackbarHostState.showSnackbar("Budget goal saved!")
            categoryTotalsViewModel.resetSavedFlag()
        }
    }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthName = monthNames.getOrElse(goalState.month - 1) { "Unknown" }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Box {
        AppScaffold(
            navController = navController,
            selectedRoute = "budget_goals",
            authViewModel = authViewModel
        ) { modifier ->

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
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
                        text = "Set Monthly Budget",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Logo Image
                Image(
                    painter = painterResource(id = R.drawable.bb_og),
                    contentDescription = "Budget Buddi Logo",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Month/Year Display Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = BrandGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$monthName $currentYear",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Minimum Budget Goal
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Minimum Monthly Spending",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "R",
                            fontSize = 28.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Light
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        OutlinedTextField(
                            value = minGoalInput,
                            onValueChange = { newValue ->
                                minGoalInput = newValue
                                categoryTotalsViewModel.updateMinGoal(newValue)
                            },
                            placeholder = {
                                Text("0", fontSize = 28.sp, color = Color.LightGray)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                errorBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.width(200.dp),
                            isError = goalState.error != null && goalState.error!!.contains("Minimum")
                        )
                    }

                    Divider(
                        color = BrandGreen.copy(alpha = 0.4f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(25.dp))

                // Maximum Budget Goal
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Maximum Monthly Spending",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "R",
                            fontSize = 28.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Light
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        OutlinedTextField(
                            value = maxGoalInput,
                            onValueChange = { newValue ->
                                maxGoalInput = newValue
                                categoryTotalsViewModel.updateMaxGoal(newValue)
                            },
                            placeholder = {
                                Text("0", fontSize = 28.sp, color = Color.LightGray)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                errorBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.width(200.dp),
                            isError = goalState.error != null && goalState.error!!.contains("Maximum")
                        )
                    }

                    Divider(
                        color = BrandGreen.copy(alpha = 0.4f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Error message
                if (goalState.error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = goalState.error!!,
                                color = Color(0xFFD32F2F),
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = { categoryTotalsViewModel.saveBudgetGoal() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Budget Goal",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // View Category Totals Button (Secondary)
                OutlinedButton(
                    onClick = { navController.navigate("category_totals") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrandGreen
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "View Category Totals",
                        color = BrandGreen,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}