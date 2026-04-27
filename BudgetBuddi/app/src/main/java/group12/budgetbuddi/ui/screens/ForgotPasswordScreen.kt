package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import group12.budgetbuddi.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var resetEmail by remember { mutableStateOf("") }

    val resetState by authViewModel.resetState.collectAsState()

    LaunchedEffect(resetState.isCodeSent) {
        if (resetState.isCodeSent && resetState.resetToken.isNotBlank()) {
            navController.navigate("reset/${resetState.resetToken}")
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
        Text("Forgot Password", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Enter your email to receive a reset code", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = resetEmail,
            onValueChange = { resetEmail = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { authViewModel.sendResetCode(resetEmail) },
            enabled = !resetState.isLoading && resetEmail.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (resetState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text("Send Reset Code")
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