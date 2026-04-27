package group12.budgetbuddi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import group12.budgetbuddi.repository.AuthRepository
import group12.budgetbuddi.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
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
    val resetToken: String = "",
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)
    private val sessionManager = SessionManager(application)

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private val _resetState = MutableStateFlow(ResetUiState())
    val resetState: StateFlow<ResetUiState> = _resetState.asStateFlow()


    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()


    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _isLoggedIn.value = false
            _username.value = ""
            _loginState.value = LoginUiState()
            _registerState.value = RegisterUiState()
            _resetState.value = ResetUiState()
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, error = null)
            val result = repository.login(email, password)
            if (result.isSuccess) {
                result.user?.let { user ->
                    sessionManager.saveSession(user.email, user.username)
                    _isLoggedIn.value = true
                    _username.value = user.username
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                }
            } else {
                _loginState.value = _loginState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = result.message
                )
            }
        }
    }

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = sessionManager.isLoggedIn()
            _username.value = sessionManager.getUsername() ?: ""
        }
    }

    fun register(email: String, username: String, password: String) {
        viewModelScope.launch {
            _registerState.value = _registerState.value.copy(isLoading = true, error = null)
            val result = repository.register(email, username, password)
            if (result.isSuccess) {
                _registerState.value = _registerState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
            } else {
                _registerState.value = _registerState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = result.message
                )
            }
        }
    }

    fun sendResetCode(email: String) {
        viewModelScope.launch {
            _resetState.value = _resetState.value.copy(isLoading = true, error = null)
            val result = repository.sendPasswordResetCode(email)
            if (result.isSuccess) {
                _resetState.value = _resetState.value.copy(
                    isLoading = false,
                    isCodeSent = true,
                    resetToken = result.token,
                    error = null
                )
            } else {
                _resetState.value = _resetState.value.copy(
                    isLoading = false,
                    isCodeSent = false,
                    error = result.message
                )
            }
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _resetState.value = _resetState.value.copy(isLoading = true, error = null)
            val result = repository.resetPassword(token, newPassword)
            if (result.isSuccess) {
                _resetState.value = _resetState.value.copy(
                    isLoading = false,
                    isResetComplete = true,
                    error = null
                )
            } else {
                _resetState.value = _resetState.value.copy(
                    isLoading = false,
                    isResetComplete = false,
                    error = result.message
                )
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginUiState()
    }

    fun resetRegisterState() {
        _registerState.value = RegisterUiState()
    }

    fun resetResetState() {
        _resetState.value = ResetUiState()
    }
}