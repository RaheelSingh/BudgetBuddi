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

@Composable
fun HamburgerMenu(
    selectedRoute: String,
    onHomeClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onBudgetGoalsClick: () -> Unit,
    onCategoryTotalsClick: () -> Unit,
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
                selected = selectedRoute == "budget_goals",
                onClick = onBudgetGoalsClick,
                icon = { Icon(Icons.Default.TrackChanges, contentDescription = "Budget Goals") },
                label = { Text("Budget Goals") }
            )

            NavigationDrawerItem(
                selected = selectedRoute == "category_totals",
                onClick = onCategoryTotalsClick,
                icon = { Icon(Icons.Default.PieChart, contentDescription = "Category Totals") },
                label = { Text("Category Totals") }
            )

            NavigationDrawerItem(
                selected = false,
                onClick = onLogoutClick,
                icon = { Icon(Icons.Default.Logout, contentDescription = "Logout") },
                label = { Text("Logout") }
            )
        }
    }
}