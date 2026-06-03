package group12.budgetbuddi.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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

private val RegisterGreen = Color(0xFF139A43)
private val RegisterError = Color(0xFFD32F2F)
private val RegisterTextDark = Color(0xFF1A1A1A)

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordErrors by remember { mutableStateOf(listOf<String>()) }
    var isPasswordValid by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    val registerState by authViewModel.registerState.collectAsState()

    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(password) {
        val validation = PasswordUtils.isValidPassword(password)
        passwordErrors = validation.errors
        isPasswordValid = validation.isValid
    }

    LaunchedEffect(registerState.isSuccess) {
        if (registerState.isSuccess) {
            Toast.makeText(context, "Registration successful! Please log in.", Toast.LENGTH_LONG).show()
            navController.navigate("login") {
                popUpTo("register") { inclusive = true }
                launchSingleTop = true
            }
            authViewModel.resetRegisterState()
        }
    }

    LaunchedEffect(registerState.error) {
        if (!registerState.error.isNullOrBlank()) {
            Toast.makeText(context, registerState.error, Toast.LENGTH_LONG).show()
        }
    }

    val alpha by animateFloatAsState(if (visible) 1f else 0f, label = "registerAlpha")

    Box(
        modifier = Modifier.fillMaxSize().background(RegisterGreen).graphicsLayer(alpha = alpha)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Create Account", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Join Budget Buddi today", fontSize = 17.sp, color = Color.White.copy(alpha = 0.9f))
        }

        Image(
            painter = painterResource(id = R.drawable.bubulookingdown),
            contentDescription = "Bubu",
            modifier = Modifier.align(Alignment.Center).size(340.dp).offset(y = (-70).dp)
        )

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(590.dp).clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RegisterGreen, unfocusedBorderColor = RegisterGreen.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RegisterGreen, unfocusedBorderColor = RegisterGreen.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    isError = password.isNotEmpty() && passwordErrors.isNotEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RegisterGreen, unfocusedBorderColor = RegisterGreen.copy(alpha = 0.5f)),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "Hide" else "Show", fontSize = 12.sp, color = RegisterGreen)
                        }
                    },
                    supportingText = {
                        if (password.isNotEmpty() && passwordErrors.isNotEmpty()) {
                            Text(passwordErrors.first(), fontSize = 10.sp, color = RegisterError, maxLines = 1)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RegisterGreen, unfocusedBorderColor = RegisterGreen.copy(alpha = 0.5f)),
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                            Text("Passwords do not match", fontSize = 10.sp, color = RegisterError)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { authViewModel.register(email, username, password) },
                    enabled = !registerState.isLoading && email.isNotBlank() && username.isNotBlank() && isPasswordValid && password == confirmPassword,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RegisterGreen)
                ) {
                    if (registerState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                if (!registerState.error.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(registerState.error ?: "", color = RegisterError, fontSize = 12.sp, maxLines = 1)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account?", color = RegisterTextDark, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick = { navController.navigate("login") },
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Text("Login", fontWeight = FontWeight.Bold, color = RegisterGreen, fontSize = 13.sp)
                    }
                }

                TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to Welcome", color = RegisterGreen, fontSize = 13.sp)
                }
            }
        }
    }
}
