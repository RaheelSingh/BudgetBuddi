package group12.budgetbuddi.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.viewmodel.AuthViewModel

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

    val resetState by authViewModel.resetState.collectAsState()

    // Function to copy token to clipboard
    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ResetCode", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Reset code copied!", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(resetState.isResetComplete) {
        if (resetState.isResetComplete) {
            navController.navigate("login") {
                popUpTo("reset/$token") { inclusive = true }
            }
            authViewModel.resetResetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Reset Password", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Enter the reset code and your new password", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(48.dp))

        // Card with copyable token
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Your reset code:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = token,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Simple "+" button to copy
                Button(
                    onClick = { copyToClipboard(token) },
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+", fontSize = MaterialTheme.typography.titleLarge.fontSize)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // User pastes or types the token here
        OutlinedTextField(
            value = enteredToken,
            onValueChange = { enteredToken = it },
            label = { Text("Reset Code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = newPassword.isNotEmpty() && newPassword.length < 6,
            supportingText = {
                if (newPassword.isNotEmpty() && newPassword.length < 6) {
                    Text("Password must be at least 6 characters")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword,
            supportingText = {
                if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                    Text("Passwords do not match")
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { authViewModel.resetPassword(enteredToken, newPassword) },
            enabled = !resetState.isLoading &&
                    enteredToken.isNotBlank() &&
                    newPassword.length >= 6 &&
                    newPassword == confirmPassword,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (resetState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text("Reset Password")
        }

        resetState.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back to Login")
        }
    }
}