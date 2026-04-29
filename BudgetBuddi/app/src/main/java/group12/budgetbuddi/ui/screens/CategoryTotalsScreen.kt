package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.CategoryTotalsViewModel

@Composable
fun CategoryTotalsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    categoryTotalsViewModel: CategoryTotalsViewModel = viewModel()
) {
    val state by categoryTotalsViewModel.totalsUiState.collectAsState()

    AppScaffold(
        navController = navController,
        selectedRoute = "category_totals",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Category Totals",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}