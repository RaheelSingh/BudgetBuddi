package group12.budgetbuddi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import group12.budgetbuddi.ui.theme.BrandGreenDark as BudgetGreen

@Composable
fun BottomNavBar(
    navController: NavController,
    selectedRoute: String,
    onMenuClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    color = BudgetGreen.copy(alpha = 0.85f)
                )
            ),
        color = BudgetGreen,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                selected = selectedRoute == "home",
                label = "Home",
                icon = Icons.Default.Home,
                contentDescription = "Home",
                onClick = {
                    if (selectedRoute != "home") {
                        navController.navigate("home") {
                            launchSingleTop = true
                        }
                    }
                }
            )

            BottomNavItem(
                selected = selectedRoute == "daily_report",
                label = "Daily",
                icon = Icons.Default.Assessment,
                contentDescription = "Daily Report",
                onClick = {
                    if (selectedRoute != "daily_report") {
                        navController.navigate("daily_report") {
                            launchSingleTop = true
                        }
                    }
                }
            )

            BottomNavItem(
                selected = selectedRoute == "add_transaction",
                label = "Add",
                icon = Icons.Default.AddCircle,
                contentDescription = "Add Transaction",
                onClick = {
                    if (selectedRoute != "add_transaction") {
                        navController.navigate("add_transaction") {
                            launchSingleTop = true
                        }
                    }
                }
            )

            BottomNavItem(
                selected = selectedRoute == "menu",
                label = "Menu",
                icon = Icons.Default.Menu,
                contentDescription = "Menu",
                onClick = onMenuClick
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(82.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(62.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(
                    if (selected) Color.White.copy(alpha = 0.22f) else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
