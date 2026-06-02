package group12.budgetbuddi.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrackChanges
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
    onExpensesClick: () -> Unit,
    onCategorySummaryClick: () -> Unit,
    onBudgetGoalsClick: () -> Unit,
    onCategoryTotalsClick: () -> Unit,
    onBudgetReportClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onUserProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Budget Buddi")

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))


            NavigationDrawerItem(
                selected = selectedRoute == "home",
                onClick = onHomeClick,
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") }
            )


            NavigationDrawerItem(
                selected = selectedRoute == "categories",
                onClick = onCategoriesClick,
                icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
                label = { Text("Categories") }
            )


            NavigationDrawerItem(
                selected = selectedRoute == "expenses",
                onClick = onExpensesClick,
                icon = { Icon(Icons.Default.ListAlt, contentDescription = "Transactions") },
                label = { Text("Transactions") }
            )


            NavigationDrawerItem(
                selected = selectedRoute == "category_summary",
                onClick = onCategorySummaryClick,
                icon = { Icon(Icons.Default.PieChart, contentDescription = "Category Summary") },
                label = { Text("Category Summary") }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))


            NavigationDrawerItem(
                selected = selectedRoute == "budget_goals",
                onClick = onBudgetGoalsClick,
                icon = { Icon(Icons.Default.TrackChanges, contentDescription = "Budget Goals") },
                label = { Text("Budget Goals") }
            )


            NavigationDrawerItem(
                selected = selectedRoute == "category_totals",
                onClick = onCategoryTotalsClick,
                icon = { Icon(Icons.Default.PieChart, contentDescription = "Category Totals") },
                label = { Text("Category Goals Report") }
            )


            NavigationDrawerItem(
                selected = selectedRoute == "budget_report",
                onClick = onBudgetReportClick,
                icon = { Icon(Icons.Default.PieChart, contentDescription = "Budget Report") },
                label = { Text("Budget Report") }
            )

            NavigationDrawerItem(
                selected = selectedRoute == "achievements",
                onClick = onAchievementsClick,
                icon = { Icon(imageVector = Icons.Default.Category, contentDescription = "Achievements") },
                label = { Text("Achievements") }
                    )

            NavigationDrawerItem(
                selected = selectedRoute == "user_profile",
                onClick = onUserProfileClick,
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                label = { Text("My Profile") }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))


            NavigationDrawerItem(
                selected = false,
                onClick = onLogoutClick,
                icon = { Icon(Icons.Default.Logout, contentDescription = "Logout") },
                label = { Text("Logout") }
            )
            // Add to function parameters


// Add this menu item in the Column (put it after Categories or wherever you want)


        }
    }
}