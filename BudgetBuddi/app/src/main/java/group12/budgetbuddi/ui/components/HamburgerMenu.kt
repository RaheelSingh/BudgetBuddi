package group12.budgetbuddi.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*
Author: Android Developers
Description: Used as reference for creating a Material 3 drawer sheet with navigation drawer items.
Reference: Android Developers. 2024. Navigation drawer. [Online]. Available at: https://developer.android.com/develop/ui/compose/components/drawer [Accessed 29 April 2026].
*/

@Composable
fun HamburgerMenu(
    selectedRoute: String,
    onHomeClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Budget Buddi")

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            // The selected route is used to highlight the current menu item.
            NavigationDrawerItem(
                selected = selectedRoute == "home",
                onClick = onHomeClick,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home"
                    )
                },
                label = { Text("Home") }
            )

            NavigationDrawerItem(
                selected = selectedRoute == "categories",
                onClick = onCategoriesClick,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Categories"
                    )
                },
                label = { Text("Categories") }
            )

            // Logout is not linked to a selected screen, so selected remains false.
            NavigationDrawerItem(
                selected = false,
                onClick = onLogoutClick,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout"
                    )
                },
                label = { Text("Logout") }
            )
        }
    }
}