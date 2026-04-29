package group12.budgetbuddi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import group12.budgetbuddi.ui.screens.*
import group12.budgetbuddi.ui.theme.BudgetBuddiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BudgetBuddiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    // TEMP: replace later with real logged-in user
    val userEmail = "test@budgetbuddi.com"

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {

        composable("welcome") {
            WelcomeScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("register") {
            RegisterScreen(navController)
        }

        composable("forgot") {
            ForgotPasswordScreen(navController)
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable("categories") {
            CategoriesScreen(navController)
        }

        composable("add_category") {
            AddCategoryScreen(navController)
        }

        composable("edit_delete_category") {
            EditDeleteCategoryScreen(navController)
        }

        composable("daily_report") {
            DailyReportScreen(navController)
        }

        composable("expenses") {
            ExpensesScreen(navController)
        }

        composable("add_transaction") {
            AddTransactionScreen(navController)
        }

    }
}