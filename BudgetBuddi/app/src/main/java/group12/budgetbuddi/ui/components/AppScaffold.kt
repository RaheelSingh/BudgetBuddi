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

/*
Author: Android Developers
Description: Used as reference for Compose navigation actions such as navigate, popUpTo, and launchSingleTop.
Reference: Android Developers. n.d. Navigation with Compose | Jetpack Compose. [Online]. Available at: https://developer.android.com/develop/ui/compose/navigation [Accessed 29 April 2026].
*/
@Composable
fun AppScaffold(
    navController: NavController,
    selectedRoute: String,
    authViewModel: AuthViewModel,
    content: @Composable (Modifier) -> Unit
) {

    // This controls whether the side drawer is currently open or closed.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Drawer open and close functions are suspend functions, so they are launched in a coroutine.
    val scope = rememberCoroutineScope()

    // This makes the drawer open from the right side.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

        ModalNavigationDrawer(
            drawerState = drawerState,

            drawerContent = {

                // The drawer opens from the right, but the menu text itself stays left-to-right.
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

            // The main app content also stays left-to-right.
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

                    // The screen content is injected here so all screens can reuse the same scaffold.
                    content(Modifier.padding(padding))
                }
            }
        }
    }
}