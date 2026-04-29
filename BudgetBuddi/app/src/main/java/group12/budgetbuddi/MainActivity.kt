package group12.budgetbuddi

import group12.budgetbuddi.ui.screens.ExpenseListScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import group12.budgetbuddi.ui.screens.*
import group12.budgetbuddi.ui.theme.BudgetBuddiTheme
import java.net.URLDecoder

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

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        composable("forgot") {
            ForgotPasswordScreen(navController = navController)
        }

        composable("reset/{token}") { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            ResetPasswordScreen(navController = navController, token = token)
        }

        composable("home") {
            HomeScreen(navController = navController)
        }

        composable("categories") {
            CategoriesScreen(navController = navController)
        }

        composable("add_category") {
            AddCategoryScreen(navController = navController)
        }

        composable("edit_delete_category") {
            EditDeleteCategoryScreen(navController = navController)
        }

        composable("daily_report") {
            DailyReportScreen(navController = navController)
        }

        composable("expenses") {
            ExpensesScreen(navController = navController)
        }

        composable("add_transaction") {
            AddTransactionScreen(navController = navController)
        }

        composable("expense_list") {
            ExpenseListScreen(navController = navController)
        }


        composable("photo_viewer/{encodedPath}") { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("encodedPath") ?: ""
            val photoPath = URLDecoder.decode(encodedPath, "UTF-8")
            PhotoViewerScreen(
                photoPath = photoPath,
                onBack = { navController.popBackStack() }
            )
        }
    }
}