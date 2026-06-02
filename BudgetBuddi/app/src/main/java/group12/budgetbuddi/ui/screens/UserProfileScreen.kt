package group12.budgetbuddi.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import group12.budgetbuddi.data.AchievementsData
import group12.budgetbuddi.data.entity.AchievementEntity
import group12.budgetbuddi.ui.components.AppScaffold
import group12.budgetbuddi.ui.theme.BrandGreen
import group12.budgetbuddi.ui.theme.TextBlack
import group12.budgetbuddi.viewmodel.AuthViewModel
import group12.budgetbuddi.viewmodel.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: UserProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val uiState by profileViewModel.uiState.collectAsState()

    var isEditingUsername by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf(currentUser?.username ?: "") }
    var showAvatarPicker by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { profileViewModel.setUserId(it) }
    }

    // Get XP progress
    val xpProgress = uiState.levelProgress
    val currentXP = uiState.currentXP
    val currentLevel = uiState.currentLevel
    val xpToNext = uiState.xpToNextLevel
    val levelTitle = uiState.levelTitle

    // Get selected avatar
    val selectedAvatarId = uiState.userProfile?.selectedAvatarBadgeId
    val selectedAchievement = uiState.unlockedAchievements.find { it.id == selectedAvatarId }

    AppScaffold(
        navController = navController,
        selectedRoute = "user_profile",
        authViewModel = authViewModel
    ) { modifier ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Header with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(BrandGreen, Color(0xFF0D8B5E))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Clickable Avatar
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .border(3.dp, Color.White, CircleShape)
                            .clickable { showAvatarPicker = true },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (selectedAchievement != null) {
                                AsyncImage(
                                    model = selectedAchievement.badgeUrl,
                                    contentDescription = "Profile Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = currentUser?.username?.take(1)?.uppercase() ?: "?",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Username (editable) - UPDATED
                    if (isEditingUsername) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            OutlinedTextField(
                                value = newUsername,
                                onValueChange = { newUsername = it },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                                ),
                                textStyle = LocalTextStyle.current.copy(color = Color.White)
                            )
                            IconButton(onClick = {
                                when {
                                    newUsername.isBlank() -> {
                                        Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                                    }
                                    newUsername == currentUser?.username -> {
                                        isEditingUsername = false
                                    }
                                    else -> {
                                        authViewModel.updateUsername(newUsername)
                                        isEditingUsername = false
                                        Toast.makeText(context, "Username updated to: $newUsername", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.White)
                            }
                            IconButton(onClick = {
                                isEditingUsername = false
                                newUsername = currentUser?.username ?: ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                newUsername = currentUser?.username ?: ""
                                isEditingUsername = true
                            }
                        ) {
                            Text(
                                text = currentUser?.username ?: "User",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = currentUser?.email ?: "",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Level Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Current Level", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text("Level $currentLevel", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(levelTitle, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                                }
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFFFD700)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🏆", fontSize = 28.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // XP Progress Bar
                            Text("$currentXP / ${currentXP + xpToNext} XP", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(xpProgress)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFFD700))
                                )
                            }
                            Text("$xpToNext XP to next level", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total XP",
                    value = "${uiState.currentXP}",
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Achievements",
                    value = "${uiState.unlockedAchievements.size}/${uiState.allAchievements.size}",
                    icon = Icons.Default.EmojiEvents,
                    modifier = Modifier.weight(1f)
                )
            }

            // Achievements Section
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Achievements",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }

                if (uiState.unlockedAchievements.isNotEmpty()) {
                    item {
                        Text(
                            text = "Unlocked (${uiState.unlockedAchievements.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandGreen,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(uiState.unlockedAchievements) { achievement ->
                        AchievementListItem(
                            achievement = achievement,
                            isUnlocked = true
                        )
                    }
                }

                if (uiState.lockedAchievements.isNotEmpty()) {
                    item {
                        Text(
                            text = "Locked (${uiState.lockedAchievements.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    items(uiState.lockedAchievements) { achievement ->
                        AchievementListItem(
                            achievement = achievement,
                            isUnlocked = false
                        )
                    }
                }
            }
        }
    }

    // Avatar Picker Dialog
    if (showAvatarPicker) {
        Dialog(onDismissRequest = { showAvatarPicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Choose Profile Picture",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )

                    Text(
                        text = "Select an achievement badge as your profile picture",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    if (uiState.unlockedAchievements.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Complete achievements to unlock profile pictures!",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Default avatar option
                            item {
                                AvatarOption(
                                    badgeId = null,
                                    badgeUrl = null,
                                    label = "Default",
                                    isSelected = uiState.userProfile?.selectedAvatarBadgeId == null,
                                    onClick = {
                                        profileViewModel.updateAvatar(null)
                                        showAvatarPicker = false
                                        Toast.makeText(context, "Profile picture reset to default", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            items(uiState.unlockedAchievements) { achievement ->
                                AvatarOption(
                                    badgeId = achievement.id,
                                    badgeUrl = achievement.badgeUrl,
                                    label = achievement.name,
                                    isSelected = uiState.userProfile?.selectedAvatarBadgeId == achievement.id,
                                    onClick = {
                                        profileViewModel.updateAvatar(achievement.id)
                                        showAvatarPicker = false
                                        Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showAvatarPicker = false },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = BrandGreen.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(18.dp))
                }
            }
            Column {
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Text(text = title, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AchievementListItem(
    achievement: AchievementEntity,
    isUnlocked: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color.White else Color(0xFFFAFAFA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (isUnlocked) BrandGreen.copy(alpha = 0.1f) else Color(0xFFEEEEEE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isUnlocked) {
                        AsyncImage(
                            model = achievement.badgeUrl,
                            contentDescription = achievement.name,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUnlocked) Color(0xFF1A1A1A) else Color.Gray
                )
                Text(
                    text = achievement.description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                if (!isUnlocked) {
                    val requirementText = when (achievement.criteriaType) {
                        "expense_count" -> "Add ${achievement.criteriaValue} expense(s)"
                        "income_count" -> "Add ${achievement.criteriaValue} income(s)"
                        "budget_goal_created" -> "Create ${achievement.criteriaValue} budget goal(s)"
                        "category_created" -> "Create ${achievement.criteriaValue} category(s)"
                        "transaction_with_photo" -> "Add ${achievement.criteriaValue} photo(s)"
                        else -> "${achievement.criteriaValue} ${achievement.criteriaType}"
                    }
                    Text(requirementText, fontSize = 10.sp, color = BrandGreen.copy(alpha = 0.7f))
                } else {
                    Text("+${achievement.xpReward} XP", fontSize = 10.sp, color = BrandGreen, fontWeight = FontWeight.Medium)
                }
            }

            if (isUnlocked) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Unlocked", tint = BrandGreen, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AvatarOption(
    badgeId: String?,
    badgeUrl: String?,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(70.dp),
            shape = CircleShape,
            color = if (isSelected) BrandGreen.copy(alpha = 0.2f) else Color.Transparent,
            border = if (isSelected) BorderStroke(3.dp, BrandGreen) else null
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (badgeUrl != null) {
                    AsyncImage(
                        model = badgeUrl,
                        contentDescription = label,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = Color(0xFFE0E0E0)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Default",
                                modifier = Modifier.size(30.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = label.take(15),
            fontSize = 10.sp,
            color = if (isSelected) BrandGreen else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}