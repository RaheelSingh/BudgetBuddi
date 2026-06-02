package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import group12.budgetbuddi.ui.theme.BrandGreen
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
                .background(Color(0xFFF5F5F5))
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BrandGreen,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Achievements",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Stats summary
                    Surface(
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

            // Tier Tabs
            TabRow(
                selectedTabIndex = selectedTier - 1,
                containerColor = Color.White,
                contentColor = BrandGreen
            ) {
                listOf("Tier 1", "Tier 2", "Tier 3").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTier == index + 1,
                        onClick = { selectedTier = index + 1 },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTier == index + 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Achievements List
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
fun AchievementCard(
    achievement: AchievementEntity,
    isUnlocked: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color.White else Color(0xFFFAFAFA)
        ),
        elevation = CardDefaults.cardElevation(if (isUnlocked) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge Image or Locked Icon
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = if (isUnlocked) BrandGreen.copy(alpha = 0.1f) else Color(0xFFEEEEEE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isUnlocked) {
                        AsyncImage(
                            model = achievement.badgeUrl,
                            contentDescription = achievement.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        // Show grayed out version of the badge for locked achievements
                        AsyncImage(
                            model = achievement.badgeUrl,
                            contentDescription = achievement.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .alpha(0.3f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Achievement Name - Always show the name
                Text(
                    text = achievement.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color(0xFF1A1A1A) else Color(0xFF666666)
                )

                // Achievement Description - Always show the description
                Text(
                    text = achievement.description,
                    fontSize = 13.sp,
                    color = if (isUnlocked) Color.Gray else Color(0xFF999999),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Show unlock requirement for locked achievements
                if (!isUnlocked) {
                    val requirementText = when (achievement.criteriaType) {
                        "expense_count" -> "🔓 Add ${achievement.criteriaValue} expense(s) to unlock"
                        "income_count" -> "🔓 Add ${achievement.criteriaValue} income(s) to unlock"
                        "budget_goal_created" -> "🔓 Create ${achievement.criteriaValue} budget goal(s) to unlock"
                        "category_created" -> "🔓 Create ${achievement.criteriaValue} category(s) to unlock"
                        "transaction_with_photo" -> "🔓 Add ${achievement.criteriaValue} photo(s) to unlock"
                        else -> "🔓 Complete to unlock"
                    }
                    Text(
                        text = requirementText,
                        fontSize = 11.sp,
                        color = BrandGreen.copy(alpha = 0.7f)
                    )
                } else {
                    // Show XP reward for unlocked achievements
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BrandGreen.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "+${achievement.xpReward} XP",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandGreen
                        )
                    }
                }
            }

            // Unlocked checkmark or lock icon
            if (isUnlocked) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Unlocked",
                    tint = BrandGreen,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}