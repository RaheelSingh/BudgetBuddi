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
import group12.budgetbuddi.utils.AchievementManager
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.UserProfileEvent
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

    var showAchievementDialog by remember { mutableStateOf<AchievementEntity?>(null) }
    var dialogOldLevel by remember { mutableStateOf(1) }
    var dialogNewLevel by remember { mutableStateOf(1) }

    val userProfileViewModel = if (AchievementManager.isInitialized()) {
        AchievementManager.userProfileViewModel
    } else null

    fun navigateFromDrawer(route: String) {
        scope.launch { drawerState.close() }
        if (selectedRoute != route) {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(userProfileViewModel) {
        userProfileViewModel?.events?.collect { event ->
            when (event) {
                is UserProfileEvent.AchievementUnlocked -> {
                    showAchievementDialog = event.achievement
                    dialogOldLevel = event.oldLevel
                    dialogNewLevel = event.newLevel
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
                        onHomeClick = { navigateFromDrawer("home") },
                        onCategoriesClick = { navigateFromDrawer("categories") },
                        onExpensesClick = { navigateFromDrawer("expense_list") },
                        onCategorySummaryClick = { navigateFromDrawer("category_summary") },
                        onBudgetGoalsClick = { navigateFromDrawer("category_totals") },
                        onCategoryTotalsClick = { navigateFromDrawer("category_totals") },
                        onBudgetReportClick = { navigateFromDrawer("budget_report") },
                        onAchievementsClick = { navigateFromDrawer("achievements") },
                        onUserProfileClick = { navigateFromDrawer("user_profile") },
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
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }
                ) { padding ->
                    content(Modifier.padding(padding))
                }
            }
        }
    }

    showAchievementDialog?.let { achievement ->
        AchievementUnlockDialog(
            achievement = achievement,
            oldLevel = dialogOldLevel,
            newLevel = dialogNewLevel,
            onDismiss = { showAchievementDialog = null }
        )
    }
}
