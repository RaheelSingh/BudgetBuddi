package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import group12.budgetbuddi.data.entity.AchievementEntity
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BrandGreenDark as BudgetGreen
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: UserProfileViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val uiState by profileViewModel.uiState.collectAsState()
    var selectedTier by remember { mutableStateOf(1) }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { profileViewModel.setUserId(it) }
    }

    val filteredAchievements = remember(uiState.allAchievements, selectedTier) {
        uiState.allAchievements.filter { it.tier == selectedTier }
    }

    AppScaffold(
        navController = navController,
        selectedRoute = "achievements",
        authViewModel = authViewModel
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E9))
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BudgetGreen,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Achievements", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Surface(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${uiState.unlockedAchievements.size}/${uiState.allAchievements.size}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            TabRow(selectedTabIndex = selectedTier - 1, containerColor = Color.White, contentColor = BudgetGreen) {
                listOf("Tier 1", "Tier 2", "Tier 3").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTier == index + 1,
                        onClick = { selectedTier = index + 1 },
                        text = { Text(title, fontSize = 14.sp, fontWeight = if (selectedTier == index + 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAchievements) { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        isUnlocked = uiState.unlockedAchievements.contains(achievement)
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: AchievementEntity, isUnlocked: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(if (isUnlocked) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = if (isUnlocked) BudgetGreen.copy(alpha = 0.1f) else Color(0xFFEEEEEE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = achievement.badgeUrl,
                        contentDescription = achievement.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .alpha(if (isUnlocked) 1f else 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color(0xFF1A1A1A) else Color(0xFF666666)
                )
                Text(
                    text = achievement.description,
                    fontSize = 13.sp,
                    color = if (isUnlocked) Color.Gray else Color(0xFF999999),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(5.dp))

                if (isUnlocked) {
                    Surface(shape = RoundedCornerShape(50.dp), color = BudgetGreen.copy(alpha = 0.1f)) {
                        Text(
                            text = "+${achievement.xpReward} XP",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = BudgetGreen
                        )
                    }
                } else {
                    Text(
                        text = getAchievementRequirementText(achievement),
                        fontSize = 11.sp,
                        color = BudgetGreen.copy(alpha = 0.75f)
                    )
                }
            }

            if (!isUnlocked) {
                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun getAchievementRequirementText(achievement: AchievementEntity): String {
    return when (achievement.criteriaType) {
        "expense_count" -> "Add ${achievement.criteriaValue} expense(s) to unlock"
        "income_count" -> "Add ${achievement.criteriaValue} income entrie(s) to unlock"
        "budget_goal_created" -> "Create ${achievement.criteriaValue} budget goal(s) to unlock"
        "category_created" -> "Create ${achievement.criteriaValue} category/categories to unlock"
        "transaction_with_photo" -> "Add ${achievement.criteriaValue} photo(s) to unlock"
        "streak" -> "Reach a ${achievement.criteriaValue}-day streak"
        "monthly_saved" -> "Save R${achievement.criteriaValue} in a month"
        else -> "Complete this achievement to unlock"
    }
}
