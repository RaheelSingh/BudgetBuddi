package group12.budgetbuddi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import group12.budgetbuddi.repository.AuthResult
import group12.budgetbuddi.repository.FirebaseAuthRepository
import group12.budgetbuddi.repository.FirebaseUser
import group12.budgetbuddi.repository.PasswordResetResult
import group12.budgetbuddi.utils.PasswordUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val authRepository = FirebaseAuthRepository()

    // UI State for Login
    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    // UI State for Register
    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    // UI State for Password Reset
    private val _resetState = MutableStateFlow(ResetUiState())
    val resetState: StateFlow<ResetUiState> = _resetState.asStateFlow()

    // Current user session
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        // Start observing auth state changes
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                _currentUser.update { user }
                Log.d(TAG, "Auth state changed: ${user?.email ?: "No user"}")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, error = null) }

            // Local validation
            if (email.isBlank()) {
                _loginState.update { it.copy(isLoading = false, error = "Email is required") }
                return@launch
            }
            if (password.isBlank()) {
                _loginState.update { it.copy(isLoading = false, error = "Password is required") }
                return@launch
            }
            if (!PasswordUtils.isValidEmail(email)) {
                _loginState.update { it.copy(isLoading = false, error = "Invalid email format") }
                return@launch
            }

            val result = authRepository.loginUser(email.trim(), password)

            when (result) {
                is AuthResult.Success -> {
                    Log.d(TAG, "Login success: ${result.email}")
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            userEmail = result.email,
                            username = result.username
                        )
                    }
                }
                is AuthResult.Error -> {
                    Log.e(TAG, "Login failed: ${result.message}")
                    _loginState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun register(email: String, username: String, password: String) {
        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, error = null) }

            // Local validation
            if (email.isBlank()) {
                _registerState.update { it.copy(isLoading = false, error = "Email is required") }
                return@launch
            }
            if (username.isBlank()) {
                _registerState.update { it.copy(isLoading = false, error = "Username is required") }
                return@launch
            }
            if (password.isBlank()) {
                _registerState.update { it.copy(isLoading = false, error = "Password is required") }
                return@launch
            }
            if (!PasswordUtils.isValidEmail(email)) {
                _registerState.update { it.copy(isLoading = false, error = "Invalid email format") }
                return@launch
            }

            val passwordValidation = PasswordUtils.isValidPassword(password)
            if (!passwordValidation.isValid) {
                _registerState.update { it.copy(isLoading = false, error = passwordValidation.errors.first()) }
                return@launch
            }

            val result = authRepository.registerUser(email.trim(), password, username.trim())

            when (result) {
                is AuthResult.Success -> {
                    Log.d(TAG, "Register success: ${result.email}")
                    _registerState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is AuthResult.Error -> {
                    Log.e(TAG, "Register failed: ${result.message}")
                    _registerState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun sendResetCode(email: String) {
        viewModelScope.launch {
            _resetState.update { it.copy(isLoading = true, error = null) }

            if (email.isBlank()) {
                _resetState.update { it.copy(isLoading = false, error = "Email is required") }
                return@launch
            }
            if (!PasswordUtils.isValidEmail(email)) {
                _resetState.update { it.copy(isLoading = false, error = "Invalid email format") }
                return@launch
            }

            val result = authRepository.sendPasswordResetEmail(email.trim())

            when (result) {
                is PasswordResetResult.Success -> {
                    Log.d(TAG, "Reset email sent successfully")
                    _resetState.update {
                        it.copy(
                            isLoading = false,
                            isCodeSent = true,
                            message = result.message
                        )
                    }
                }
                is PasswordResetResult.Error -> {
                    Log.e(TAG, "Reset email failed: ${result.message}")
                    _resetState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        // For Firebase, password reset is handled via email link
        // This method is kept for compatibility but now just informs user
        _resetState.update {
            it.copy(error = "Please use the link sent to your email to reset your password")
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            _currentUser.update { null }
        }
    }

    fun resetLoginState() {
        _loginState.update { LoginUiState() }
    }

    fun resetRegisterState() {
        _registerState.update { RegisterUiState() }
    }

    fun resetResetState() {
        _resetState.update { ResetUiState() }
    }

    fun isLoggedIn(): Boolean {
        return _currentUser.value != null
    }
}

// UI State classes
data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val userEmail: String = "",
    val username: String = ""
)

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class ResetUiState(
    val isLoading: Boolean = false,
    val isCodeSent: Boolean = false,
    val isResetComplete: Boolean = false,
    val message: String = "",
    val error: String? = null,
    val resetToken: String = ""
)

/**
 * references:
 *
 * developer.android.com(2026) 'ViewModel overview', Android Developers.
 *    Available at: https://developer.android.com/topic/libraries/architecture/viewmodel
 *    (Accessed: 29 April 2026).
 *
 * JetBrains (2026) 'Kotlin Coroutines - StateFlow', Kotlin Language Documentation.
 *    Available at: https://kotlinlang.org/docs/flow.html
 *    (Accessed: 29 April 2026).
 */