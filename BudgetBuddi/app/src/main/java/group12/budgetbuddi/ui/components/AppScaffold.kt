// AppScaffold.kt - Add achievement listening here
package group12.budgetbuddi.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavController
import group12.budgetbuddi.data.entity.AchievementEntity
import group12.budgetbuddi.ui.components.AchievementUnlockDialog
import group12.budgetbuddi.utils.AchievementManager
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.UserProfileEvent
import group12.budgetbuddi.viewmodel.UserProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun AppScaffold(
    navController: NavController,
    selectedRoute: String,
    authViewModel: AuthViewModel,
    content: @Composable (Modifier) -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // State for achievement popup
    var showAchievementDialog by remember { mutableStateOf<AchievementEntity?>(null) }
    var dialogOldLevel by remember { mutableStateOf(1) }
    var dialogNewLevel by remember { mutableStateOf(1) }

    // Get UserProfileViewModel from AchievementManager
    val userProfileViewModel = if (AchievementManager.isInitialized()) {
        AchievementManager.userProfileViewModel
    } else null

    // Listen for achievement events globally
    LaunchedEffect(userProfileViewModel) {
        userProfileViewModel?.events?.collect { event ->
            when (event) {
                is UserProfileEvent.AchievementUnlocked -> {
                    showAchievementDialog = event.achievement
                    dialogOldLevel = event.oldLevel
                    dialogNewLevel = event.newLevel
                    // Also show toast as backup
                    android.widget.Toast.makeText(
                        context,
                        "🎉 Achievement Unlocked: ${event.achievement.name}! +${event.achievement.xpReward} XP",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    HamburgerMenu(
                        selectedRoute = selectedRoute,
                        onHomeClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onCategoriesClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("categories") {
                                launchSingleTop = true
                            }
                        },
                        onExpensesClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("expense_list") {
                                launchSingleTop = true
                            }
                        },
                        onCategorySummaryClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("category_summary") {
                                launchSingleTop = true
                            }
                        },
                        onBudgetGoalsClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("budget_goals") {
                                launchSingleTop = true
                            }
                        },
                        onCategoryTotalsClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("category_totals") {
                                launchSingleTop = true
                            }
                        },
                        onBudgetReportClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("budget_report") {
                                launchSingleTop = true
                            }
                        },
                        onAchievementsClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("achievements") {
                                launchSingleTop = true
                            }
                        },
                        onUserProfileClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("user_profile") {
                                launchSingleTop = true
                            }
                        },
                        onLogoutClick = {
                            scope.launch { drawerState.close() }
                            authViewModel.logout()
                            navController.navigate("welcome") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    bottomBar = {
                        BottomNavBar(
                            navController = navController,
                            selectedRoute = selectedRoute,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }
                ) { padding ->
                    content(Modifier.padding(padding))
                }
            }
        }
    }

    // Global Achievement Dialog - shows on top of any screen
    showAchievementDialog?.let { achievement ->
        AchievementUnlockDialog(
            achievement = achievement,
            oldLevel = dialogOldLevel,
            newLevel = dialogNewLevel,
            onDismiss = { showAchievementDialog = null }
        )
    }
}