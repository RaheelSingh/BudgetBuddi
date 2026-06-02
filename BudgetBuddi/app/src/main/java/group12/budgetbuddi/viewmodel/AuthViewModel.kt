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

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private val _resetState = MutableStateFlow(ResetUiState())
    val resetState: StateFlow<ResetUiState> = _resetState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                _currentUser.update { user }
                Log.d(TAG, "Auth state: ${user?.email ?: "Logged out"}")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, error = null) }

            if (email.isBlank()) {
                _loginState.update { it.copy(isLoading = false, error = "Email required") }
                return@launch
            }
            if (password.isBlank()) {
                _loginState.update { it.copy(isLoading = false, error = "Password required") }
                return@launch
            }
            if (!PasswordUtils.isValidEmail(email)) {
                _loginState.update { it.copy(isLoading = false, error = "Invalid email") }
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
                    _loginState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun register(email: String, username: String, password: String) {
        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, error = null) }

            if (email.isBlank()) {
                _registerState.update { it.copy(isLoading = false, error = "Email required") }
                return@launch
            }
            if (username.isBlank()) {
                _registerState.update { it.copy(isLoading = false, error = "Username required") }
                return@launch
            }
            if (password.isBlank()) {
                _registerState.update { it.copy(isLoading = false, error = "Password required") }
                return@launch
            }
            if (!PasswordUtils.isValidEmail(email)) {
                _registerState.update { it.copy(isLoading = false, error = "Invalid email") }
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

                    // Sign out immediately after registration
                    kotlinx.coroutines.delay(500)
                    authRepository.signOut()
                    _currentUser.update { null }

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
                _resetState.update { it.copy(isLoading = false, error = "Email required") }
                return@launch
            }
            if (!PasswordUtils.isValidEmail(email)) {
                _resetState.update { it.copy(isLoading = false, error = "Invalid email") }
                return@launch
            }

            val result = authRepository.sendPasswordResetEmail(email.trim())

            when (result) {
                is PasswordResetResult.Success -> {
                    Log.d(TAG, "Reset email sent to: $email")
                    _resetState.update {
                        it.copy(
                            isLoading = false,
                            isCodeSent = true,
                            message = result.message,
                            resetToken = "sent"
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

    // ===== ADD THIS FUNCTION FOR USERNAME UPDATE =====
    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            try {
                val userId = _currentUser.value?.uid
                if (userId.isNullOrBlank()) {
                    Log.e(TAG, "No user logged in")
                    return@launch
                }

                authRepository.updateUsername(userId, newUsername)

                // Update local state
                _currentUser.update { currentUser ->
                    currentUser?.copy(username = newUsername)
                }

                Log.d(TAG, "Username updated to: $newUsername")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update username: ${e.message}", e)
            }
        }
    }
    // ===== END OF ADDED FUNCTION =====

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
}

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