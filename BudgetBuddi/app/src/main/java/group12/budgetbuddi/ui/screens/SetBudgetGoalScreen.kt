package group12.budgetbuddi.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryTotalsViewModel
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetBudgetGoalScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    categoryTotalsViewModel: CategoryTotalsViewModel = viewModel()
) {
    val goalState by categoryTotalsViewModel.goalUiState.collectAsState()

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val monthName = monthNames.getOrElse(goalState.month - 1) { "Unknown" }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(goalState.isSaved) {
        if (goalState.isSaved) {
            snackbarHostState.showSnackbar("Budget goal saved!")
            categoryTotalsViewModel.resetSavedFlag()
        }
    }

    Box {
        AppScaffold(
            navController = navController,
            selectedRoute = "budget_goals",
            authViewModel = authViewModel
        ) { modifier ->

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                Text(
                    text = "Set Budget Goals",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Month: $monthName $currentYear",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = goalState.minGoal,
                    onValueChange = { categoryTotalsViewModel.updateMinGoal(it) },
                    label = { Text("Minimum Monthly Spending (R)") },
                    placeholder = { Text("e.g. 1000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = goalState.error != null
                )

                OutlinedTextField(
                    value = goalState.maxGoal,
                    onValueChange = { categoryTotalsViewModel.updateMaxGoal(it) },
                    label = { Text("Maximum Monthly Spending (R)") },
                    placeholder = { Text("e.g. 5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = goalState.error != null
                )

                if (goalState.error != null) {
                    Text(
                        text = goalState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = { categoryTotalsViewModel.saveBudgetGoal() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Goal")
                }

                OutlinedButton(
                    onClick = { navController.navigate("category_totals") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Category Totals →")
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}