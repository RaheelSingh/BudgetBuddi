package group12.budgetbuddi.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.R
import group12.budgetbuddi.utils.PasswordUtils
import group12.budgetbuddi.viewmodel.AuthViewModel

// Colors
val backgroundColorReg = Color(0xFF139A43)
val cardBackgroundReg = Color(0xFFFFFFFF)
val textLightReg = Color(0xFFFFFFFF)
val inputBorderColorReg = Color(0xFF139A43)
val textDarkReg = Color(0xFF1A1A1A)
val errorRedReg = Color(0xFFD32F2F)
val linkGreenReg = Color(0xFF139A43)

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {

    var contentVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Password validation state
    var passwordErrors by remember { mutableStateOf(listOf<String>()) }
    var isPasswordValid by remember { mutableStateOf(false) }

    // Validate password when it changes
    LaunchedEffect(password) {
        val validation = PasswordUtils.isValidPassword(password)
        passwordErrors = validation.errors
        isPasswordValid = validation.isValid
    }

    val showBubuLooking = email.isNotBlank() && password.isBlank()
    val showBubuEyesClosed = email.isNotBlank() && password.isNotBlank()

    val image1Scale by animateFloatAsState(
        targetValue = if (showBubuLooking) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "image1Scale"
    )

    val image1Alpha by animateFloatAsState(
        targetValue = if (showBubuLooking) 1f else 0f,
        animationSpec = tween(300),
        label = "image1Alpha"
    )

    val image2Scale by animateFloatAsState(
        targetValue = if (showBubuEyesClosed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "image2Scale"
    )

    val image2Alpha by animateFloatAsState(
        targetValue = if (showBubuEyesClosed) 1f else 0f,
        animationSpec = tween(300),
        label = "image2Alpha"
    )

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

    val cardOffset by animateFloatAsState(
        targetValue = if (cardVisible) 0f else 200f,
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

    val registerState by authViewModel.registerState.collectAsState()

    LaunchedEffect(registerState.isSuccess) {
        if (registerState.isSuccess) {
            navController.navigate("login") {
                popUpTo("register") { inclusive = true }
            }
            authViewModel.resetRegisterState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColorReg)
    ) {

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
                text = "Create Account",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = textLightReg,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Join Budget Buddi today",
                fontSize = 18.sp,
                color = textLightReg.copy(alpha = 0.9f),
                letterSpacing = 1.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bubulookingdown),
                contentDescription = "Bubu Looking",
                modifier = Modifier
                    .size(800.dp)
                    .scale(image1Scale)
                    .graphicsLayer(alpha = image1Alpha)
            )

            Image(
                painter = painterResource(id = R.drawable.bubublockingeyes),
                contentDescription = "Bubu Eyes Closed",
                modifier = Modifier
                    .size(800.dp)
                    .scale(image2Scale)
                    .graphicsLayer(alpha = image2Alpha)
            )
        }

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
                    .height(560.dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 0.dp, bottomEnd = 0.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = cardBackgroundReg
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inputBorderColorReg,
                            unfocusedBorderColor = inputBorderColorReg.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inputBorderColorReg,
                            unfocusedBorderColor = inputBorderColorReg.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Input - UPDATED with new validation
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inputBorderColorReg,
                            unfocusedBorderColor = inputBorderColorReg.copy(alpha = 0.5f)
                        ),
                        trailingIcon = {
                            TextButton(
                                onClick = { passwordVisible = !passwordVisible }
                            ) {
                                Text(if (passwordVisible) "Hide" else "Show", fontSize = 12.sp, color = linkGreenReg)
                            }
                        },
                        isError = password.isNotEmpty() && passwordErrors.isNotEmpty(),
                        supportingText = {
                            if (password.isNotEmpty() && passwordErrors.isNotEmpty()) {
                                Column {
                                    passwordErrors.forEach { error ->
                                        Text(error, fontSize = 11.sp, color = errorRedReg)
                                    }
                                }
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
                            focusedBorderColor = inputBorderColorReg,
                            unfocusedBorderColor = inputBorderColorReg.copy(alpha = 0.5f)
                        ),
                        isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                        supportingText = {
                            if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                                Text("Passwords do not match", fontSize = 11.sp, color = errorRedReg)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Register Button - UPDATED enabled condition
                    Button(
                        onClick = { authViewModel.register(email, username, password) },
                        enabled = !registerState.isLoading &&
                                email.isNotBlank() &&
                                username.isNotBlank() &&
                                isPasswordValid &&
                                password == confirmPassword,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = backgroundColorReg
                        )
                    ) {
                        if (registerState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = textLightReg
                            )
                        } else {
                            Text(
                                text = "Sign Up",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textLightReg
                            )
                        }
                    }

                    registerState.error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = errorRedReg,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login Link
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Already have an account? ",
                            color = textDarkReg,
                            fontSize = 14.sp
                        )
                        TextButton(
                            onClick = { navController.navigate("login") }
                        ) {
                            Text(
                                "Login",
                                fontWeight = FontWeight.Bold,
                                color = linkGreenReg,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Back to Welcome
                    TextButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Back to Welcome",
                            fontWeight = FontWeight.Normal,
                            color = linkGreenReg,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}


/**
 * references:
 * developer.android (2026) 'State in Compose', Android Developers.
 * Available at: https://developer.android.com/jetpack/compose/state
 * (Accessed: 29 April 2026).
 * developer.android (2026) 'Repository pattern - Result wrapper', Android Developers.
 * Available at: https://developer.android.com/jetpack/guide
 * (Accessed: 29 April 2026).
 * developer.android (2026) 'Text fields in Compose', Android Developers.
 *      Available at: https://developer.android.com/jetpack/compose/text
 *      (Accessed: 29 April 2026).
 * developer.android (2026) 'Form validation in Compose', Android Developers.
 *      Available at: https://developer.android.com/jetpack/compose/validation
 *      (Accessed: 29 April 2026).
 */

