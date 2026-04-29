package group12.budgetbuddi.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavController
import group12.budgetbuddi.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun AppScaffold(
    navController: NavController,
    selectedRoute: String,
    authViewModel: AuthViewModel,
    content: @Composable (Modifier) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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

                        onLogoutClick = {
                            scope.launch { drawerState.close() }
                            authViewModel.logout()
                            navController.navigate("welcome") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
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
}