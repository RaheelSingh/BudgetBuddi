package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel

@Composable
fun AddTransactionScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    AppScaffold(
        navController = navController,
        selectedRoute = "add_transaction",
        authViewModel = authViewModel
    ) { modifier ->

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Add Transaction")
        }
    }
}