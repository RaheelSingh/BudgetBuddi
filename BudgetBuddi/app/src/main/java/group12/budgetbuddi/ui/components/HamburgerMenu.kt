package group12.budgetbuddi.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DrawerGreen = Color(0xFF139A43)
private val DrawerText = Color.White
private val DrawerDivider = Color.White.copy(alpha = 0.55f)

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
    ModalDrawerSheet(
        modifier = Modifier
            .width(292.dp)
            .drawLeftDrawerOutline(),
        drawerContainerColor = DrawerGreen
    ) {
        Column(modifier = Modifier.fillMaxHeight().padding(horizontal = 14.dp, vertical = 18.dp)) {
            Text(
                text = "Budget Buddi",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DrawerText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )

            HorizontalDivider(color = DrawerDivider)
            Spacer(modifier = Modifier.height(6.dp))

            DrawerTab(
                selected = selectedRoute == "home",
                onClick = onHomeClick,
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = "Home"
            )

            DrawerTab(
                selected = selectedRoute == "categories",
                onClick = onCategoriesClick,
                icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
                label = "Categories"
            )

            DrawerTab(
                selected = selectedRoute == "expense_list" || selectedRoute == "expenses",
                onClick = onExpensesClick,
                icon = { Icon(Icons.Default.ListAlt, contentDescription = "Transaction List") },
                label = "Transaction List"
            )

            DrawerTab(
                selected = selectedRoute == "category_summary",
                onClick = onCategorySummaryClick,
                icon = { Icon(Icons.Default.PieChart, contentDescription = "Category Summary") },
                label = "Category Summary"
            )

            DrawerTab(
                selected = selectedRoute == "category_totals",
                onClick = onBudgetGoalsClick,
                icon = { Icon(Icons.Default.TrackChanges, contentDescription = "Budget Goals") },
                label = "Budget Goals"
            )

            DrawerTab(
                selected = selectedRoute == "budget_report",
                onClick = onBudgetReportClick,
                icon = { Icon(Icons.Default.PieChart, contentDescription = "Budget Report") },
                label = "Budget Report"
            )

            DrawerTab(
                selected = selectedRoute == "achievements",
                onClick = onAchievementsClick,
                icon = { Icon(Icons.Default.Category, contentDescription = "Achievements") },
                label = "Achievements"
            )

            DrawerTab(
                selected = selectedRoute == "user_profile",
                onClick = onUserProfileClick,
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                label = "My Profile"
            )

            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider(color = DrawerDivider)
            Spacer(modifier = Modifier.height(6.dp))

            DrawerTab(
                selected = false,
                onClick = onLogoutClick,
                icon = { Icon(Icons.Default.Logout, contentDescription = "Logout") },
                label = "Logout"
            )
        }
    }
}

@Composable
private fun DrawerTab(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String
) {
    NavigationDrawerItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        label = {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DrawerText,
                maxLines = 1
            )
        },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color.White.copy(alpha = 0.18f),
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            unselectedIconColor = Color.White,
            unselectedTextColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    )
    HorizontalDivider(color = DrawerDivider.copy(alpha = 0.8f), thickness = 0.7.dp)
}

private fun Modifier.drawLeftDrawerOutline(): Modifier {
    return this.then(
        Modifier.background(DrawerGreen)
    ).then(
        Modifier.drawWithContent {
            drawContent()
            drawLine(
                color = Color.Black.copy(alpha = 0.45f),
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
    )
}
