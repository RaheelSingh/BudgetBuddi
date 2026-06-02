package group12.budgetbuddi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import group12.budgetbuddi.data.entity.AchievementEntity
import group12.budgetbuddi.ui.theme.BrandGreen

@Composable
fun AchievementUnlockDialog(
    achievement: AchievementEntity,
    oldLevel: Int,
    newLevel: Int,
    onDismiss: () -> Unit
) {
    val leveledUp = newLevel > oldLevel

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Celebration Emoji
                Text(text = "🎉", fontSize = 48.sp)

                Spacer(modifier = Modifier.height(8.dp))

                // Achievement Badge Image
                AsyncImage(
                    model = achievement.badgeUrl,
                    contentDescription = achievement.name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Achievement Name
                Text(
                    text = achievement.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreen
                )

                // Achievement Description
                Text(
                    text = achievement.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // XP Reward Badge
                Surface(
                    modifier = Modifier.padding(vertical = 8.dp),
                    shape = CircleShape,
                    color = BrandGreen.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "+${achievement.xpReward} XP",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreen
                    )
                }

                // Level Up Message (if leveled up)
                if (leveledUp) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🏆 LEVEL UP! 🏆",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8F00)
                            )
                            Text(
                                text = "Level $oldLevel → Level $newLevel",
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Awesome!")
                }
            }
        }
    }
}