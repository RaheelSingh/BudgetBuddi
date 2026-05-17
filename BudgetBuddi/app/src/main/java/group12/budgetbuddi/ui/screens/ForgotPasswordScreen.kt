package group12.budgetbuddi.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

// Colors
val backgroundColorForgot = Color(0xFF139A43)
val cardBackgroundForgot = Color(0xFFFFFFFF)
val textLightForgot = Color(0xFFFFFFFF)
val inputBorderColorForgot = Color(0xFF139A43)
val textDarkForgot = Color(0xFF1A1A1A)
val errorRedForgot = Color(0xFFD32F2F)
val linkGreenForgot = Color(0xFF139A43)

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    // Animation states
    var contentVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        contentVisible = true
        delay(200)
        cardVisible = true
    }

    // Content animation
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

    // Card animation
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

    val resetState by authViewModel.resetState.collectAsState()

    // When reset email is sent successfully, show toast and go back to login
    LaunchedEffect(resetState.isCodeSent) {
        if (resetState.isCodeSent) {
            Toast.makeText(context, resetState.message, Toast.LENGTH_LONG).show()
            delay(2000)
            navController.popBackStack()  // Go back to login screen
            authViewModel.resetResetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColorForgot)
    ) {
        // Main Content - Heading
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
                text = "Forgot Password?",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = textLightForgot,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter your email to receive a reset link",
                fontSize = 18.sp,
                color = textLightForgot.copy(alpha = 0.9f),
                letterSpacing = 1.sp
            )
        }

        // Centered Card
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
                    .height(380.dp)
                    .clip(RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = cardBackgroundForgot
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
                    // Email Input
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inputBorderColorForgot,
                            unfocusedBorderColor = inputBorderColorForgot.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Send Reset Link Button
                    Button(
                        onClick = { authViewModel.sendResetCode(resetEmail) },
                        enabled = !resetState.isLoading && resetEmail.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = backgroundColorForgot
                        )
                    ) {
                        if (resetState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = textLightForgot
                            )
                        } else {
                            Text(
                                text = "Send Reset Link",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textLightForgot
                            )
                        }
                    }

                    resetState.error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = errorRedForgot,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Back to Login Link
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Remember your password? ",
                            color = textDarkForgot,
                            fontSize = 14.sp
                        )
                        TextButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text(
                                "Back to Login",
                                fontWeight = FontWeight.Bold,
                                color = linkGreenForgot,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}