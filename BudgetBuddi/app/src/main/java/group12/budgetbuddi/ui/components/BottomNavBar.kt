package group12.budgetbuddi.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomNavBar(
    navController: NavController,
    selectedRoute: String,
    onMenuClick: () -> Unit
) {
    NavigationBar {

        NavigationBarItem(
            selected = selectedRoute == "home",
            onClick = {
                navController.navigate("home") {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = selectedRoute == "daily_report",
            onClick = {
                navController.navigate("daily_report") {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = "Daily Report"
                )
            },
            label = { Text("Daily") }
        )

        NavigationBarItem(
            selected = selectedRoute == "add_transaction",
            onClick = {
                navController.navigate("add_transaction") {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Add Transaction"
                )
            },
            label = { Text("Add") }
        )

        NavigationBarItem(
            selected = selectedRoute == "menu",
            onClick = onMenuClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            },
            label = { Text("Menu") }
        )
    }
}