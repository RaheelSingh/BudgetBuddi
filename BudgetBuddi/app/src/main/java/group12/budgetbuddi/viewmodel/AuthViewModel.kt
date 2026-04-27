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

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)
    private val sessionManager = SessionManager(application)

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, error = null)
            val result = repository.login(email, password)
            if (result.isSuccess) {
                result.user?.let { user ->
                    sessionManager.saveSession(user.email, user.username)
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

    fun resetLoginState() {
        _loginState.value = LoginUiState()
    }
}