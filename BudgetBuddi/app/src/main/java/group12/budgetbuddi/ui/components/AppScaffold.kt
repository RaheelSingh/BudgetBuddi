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
    selectedRoute: String, // used to highlight current tab/menu item
    authViewModel: AuthViewModel,
    content: @Composable (Modifier) -> Unit // screen content injected here
) {

    // Controls whether the drawer is open or closed
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // For drawer to open/close asynchronously
    val scope = rememberCoroutineScope()

    // Changes drawer to open from right to left
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

        ModalNavigationDrawer(
            drawerState = drawerState,

            // Drawer content = hamburger menu UI
            drawerContent = {

                // Set the drawer content itself back to normal left-to-right text/layout
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    HamburgerMenu(
                        selectedRoute = selectedRoute,

                        // Navigate to Home
                        onHomeClick = {
                            scope.launch { drawerState.close() } // close drawer first
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true } // avoid stacking duplicates
                                launchSingleTop = true
                            }
                        },

                        // Navigate to Categories screen
                        onCategoriesClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("categories") {
                                launchSingleTop = true
                            }
                        },

                        // Logout user and return to welcome screen
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

            // Set the main app content back to normal left-to-right layout
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {

                // Main layout structure (bottom nav and screen content)
                Scaffold(

                    // Bottom navigation bar always visible
                    bottomBar = {
                        BottomNavBar(
                            navController = navController,
                            selectedRoute = selectedRoute,

                            // When hamburger icon is clicked then drawer opens
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }

                ) { padding ->

                    // Inject actual screen UI here with proper padding
                    content(Modifier.padding(padding))
                }
            }
        }
    }
}