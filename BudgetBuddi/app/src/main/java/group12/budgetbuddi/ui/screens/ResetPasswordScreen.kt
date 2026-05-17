package group12.budgetbuddi.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.viewmodel.AuthViewModel

val backgroundColorReset = Color(0xFF139A43)
val cardBackgroundReset = Color(0xFFFFFFFF)
val textLightReset = Color(0xFFFFFFFF)
val textDarkReset = Color(0xFF1A1A1A)
val linkGreenReset = Color(0xFF139A43)

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    token: String,
    authViewModel: AuthViewModel = viewModel()
) {
    var contentVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
        kotlinx.coroutines.delay(200)
        cardVisible = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "contentAlpha"
    )

    val contentOffset by animateFloatAsState(
        targetValue = if (contentVisible) 0f else 50f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "contentOffset"
    )

    val cardScale by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "cardAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColorReset)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = contentAlpha)
                .offset(y = contentOffset.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "Check Your Email!",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = textLightReset,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Password Reset Instructions Sent",
                fontSize = 18.sp,
                color = textLightReset.copy(alpha = 0.9f),
                letterSpacing = 1.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .scale(cardScale)
                .graphicsLayer(alpha = cardAlpha),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = cardBackgroundReset
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📧",
                        fontSize = 64.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "We've sent a password reset link to your email address.",
                        fontSize = 16.sp,
                        color = textDarkReset,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Please check your inbox and follow the instructions to reset your password.",
                        fontSize = 14.sp,
                        color = textDarkReset.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Didn't receive the email? Check your spam folder.",
                        fontSize = 12.sp,
                        color = textDarkReset.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            navController.popBackStack("login", false)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = backgroundColorReset
                        )
                    ) {
                        Text(
                            text = "Back to Login",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textLightReset
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Text(
                            "Need to resend?",
                            fontWeight = FontWeight.Bold,
                            color = linkGreenReset,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}