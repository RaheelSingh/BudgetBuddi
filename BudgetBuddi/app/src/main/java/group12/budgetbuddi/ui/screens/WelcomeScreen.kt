package group12.budgetbuddi.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import group12.budgetbuddi.R

// Background color
val backgroundColor = Color(0xFF139A43)

@Composable
fun WelcomeScreen(navController: NavController) {
    // Animation states
    var headingVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }
    var imageVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        headingVisible = true
        kotlinx.coroutines.delay(200)
        cardVisible = true
        kotlinx.coroutines.delay(300)
        imageVisible = true
    }

    // Heading animation
    val headingScale by animateFloatAsState(
        targetValue = if (headingVisible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "headingScale"
    )

    val headingAlpha by animateFloatAsState(
        targetValue = if (headingVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "headingAlpha"
    )

    // Card animation
    val cardOffset by animateFloatAsState(
        targetValue = if (cardVisible) 0f else 150f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "cardOffset"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "cardAlpha"
    )

    // Image animation
    val imageAlpha by animateFloatAsState(
        targetValue = if (imageVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "imageAlpha"
    )

    val imageScaleValue by animateFloatAsState(
        targetValue = if (imageVisible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "imageScaleValue"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Column(
                modifier = Modifier
                    .padding(top = 250.dp)
                    .scale(headingScale)
                    .graphicsLayer(alpha = headingAlpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BUDGET",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "BUDDI",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Budget Smarter, Live Better",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
            }
        }

        // White Card at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = cardOffset.dp)
                .graphicsLayer(alpha = cardAlpha)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomStart = 0.dp, bottomEnd = 0.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = backgroundColor
                        )
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { navController.navigate("register") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = backgroundColor
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp
                        )
                    ) {
                        Text(
                            text = "Register",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Character image
        Image(
            painter = painterResource(id = R.drawable.bubu),
            contentDescription = "Character",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer(alpha = imageAlpha)
                .scale(imageScaleValue)
                .size(400.dp)
                .offset(y = (-80).dp)
                .offset(x = 20.dp)
        )
    }
}