package group12.budgetbuddi

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import group12.budgetbuddi.ui.screens.*
import group12.budgetbuddi.ui.theme.BudgetBuddiTheme
import group12.budgetbuddi.utils.AchievementManager
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.BudgetGoalViewModel
import group12.budgetbuddi.viewmodel.CategoryViewModel
import group12.budgetbuddi.viewmodel.ExpenseViewModel
import group12.budgetbuddi.viewmodel.UserProfileViewModel
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val budgetGoalViewModel: BudgetGoalViewModel = viewModel()


    val userProfileViewModel: UserProfileViewModel = viewModel()
    val expenseViewModel: ExpenseViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel()


    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            android.util.Log.d("MainActivity", "User logged in: $userId")


            userProfileViewModel.setUserId(userId)


            AchievementManager.init(userProfileViewModel)

            userProfileViewModel.updateLoginStreak()

            android.util.Log.d("MainActivity", "UserProfileViewModel initialized with user: $userId")
            android.util.Log.d("MainActivity", "AchievementManager initialized: ${AchievementManager.isInitialized()}")
        } ?: run {
            android.util.Log.d("MainActivity", "No user logged in, clearing state")

        }
    }

    val startDestination = if (currentUser != null) "home" else "welcome"

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate("home") {
                popUpTo("welcome") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
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
        composable("achievements") {
            AchievementsScreen(navController)
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("categories") {
            CategoriesScreen(navController)
        }

        composable("add_category/{categoryType}") { backStackEntry ->
            val categoryType = backStackEntry.arguments?.getString("categoryType") ?: "expense"
            AddCategoryScreen(
                navController = navController,
                initialCategoryType = categoryType
            )
        }

        composable("add_category") {
            AddCategoryScreen(
                navController = navController,
                initialCategoryType = "expense"
            )
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
        composable("expense_list") {
            ExpenseListScreen(navController = navController)
        }
        composable("category_summary") {
            CategorySummaryScreen(navController = navController,)
        }
        composable("photo_viewer/{encodedPath}") { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("encodedPath") ?: ""
            val photoPath = URLDecoder.decode(encodedPath, "UTF-8")
            PhotoViewerScreen(
                photoPath = photoPath,
                onBack = { navController.popBackStack() }
            )
        }
        composable("budget_goals") {
            SetBudgetGoalScreen(
                navController = navController,
                budgetGoalViewModel = budgetGoalViewModel
            )
        }
        composable("category_totals") {
            CategoryTotalsScreen(
                navController = navController,
                budgetGoalViewModel = budgetGoalViewModel
            )
        }
        composable("user_profile") {
            UserProfileScreen(navController)
        }
        composable("budget_report") {
            BudgetReportScreen(navController)
        }
    }
}