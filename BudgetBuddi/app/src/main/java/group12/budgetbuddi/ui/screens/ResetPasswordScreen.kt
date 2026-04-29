package group12.budgetbuddi.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.viewmodel.AuthViewModel

// Colors
val backgroundColorReset = Color(0xFF139A43)
val cardBackgroundReset = Color(0xFFFFFFFF)
val textLightReset = Color(0xFFFFFFFF)
val inputBorderColorReset = Color(0xFF139A43)
val textDarkReset = Color(0xFF1A1A1A)
val errorRedReset = Color(0xFFD32F2F)
val linkGreenReset = Color(0xFF139A43)

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    token: String,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var enteredToken by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Animation states
    var contentVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }

    // Copy token function
    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ResetCode", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Reset code copied!", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        contentVisible = true
        kotlinx.coroutines.delay(200)
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

    LaunchedEffect(resetState.isResetComplete) {
        if (resetState.isResetComplete) {
            navController.navigate("login") {
                popUpTo("reset/$token") { inclusive = true }
            }
            authViewModel.resetResetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColorReset)
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
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Reset Password",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = textLightReset,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter the reset code and your new password",
                fontSize = 16.sp,
                color = textLightReset.copy(alpha = 0.9f),
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
                    .height(560.dp)
                    .clip(RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = cardBackgroundReset
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Card with copyable token
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = inputBorderColorReset.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Your reset code:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textDarkReset
                                )
                                Text(
                                    text = token,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = linkGreenReset
                                )
                            }

                            Button(
                                onClick = { copyToClipboard(token) },
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = backgroundColorReset
                                )
                            ) {
                                Text(
                                    "+",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textLightReset
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Reset Code Input
                    OutlinedTextField(
                        value = enteredToken,
                        onValueChange = { enteredToken = it },
                        label = { Text("Reset Code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inputBorderColorReset,
                            unfocusedBorderColor = inputBorderColorReset.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // New Password Input
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inputBorderColorReset,
                            unfocusedBorderColor = inputBorderColorReset.copy(alpha = 0.5f)
                        ),
                        trailingIcon = {
                            TextButton(
                                onClick = { passwordVisible = !passwordVisible }
                            ) {
                                Text(if (passwordVisible) "Hide" else "Show", fontSize = 12.sp, color = linkGreenReset)
                            }
                        },
                        isError = newPassword.isNotEmpty() && newPassword.length < 6,
                        supportingText = {
                            if (newPassword.isNotEmpty() && newPassword.length < 6) {
                                Text("Password must be at least 6 characters")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password Input
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inputBorderColorReset,
                            unfocusedBorderColor = inputBorderColorReset.copy(alpha = 0.5f)
                        ),
                        isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword,
                        supportingText = {
                            if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                                Text("Passwords do not match")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Reset Password Button
                    Button(
                        onClick = { authViewModel.resetPassword(enteredToken, newPassword) },
                        enabled = !resetState.isLoading &&
                                enteredToken.isNotBlank() &&
                                newPassword.length >= 6 &&
                                newPassword == confirmPassword,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = backgroundColorReset
                        )
                    ) {
                        if (resetState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = textLightReset
                            )
                        } else {
                            Text(
                                text = "Reset Password",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textLightReset
                            )
                        }
                    }

                    resetState.error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = errorRedReset,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Back to Login Link
                    TextButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Back to Login",
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