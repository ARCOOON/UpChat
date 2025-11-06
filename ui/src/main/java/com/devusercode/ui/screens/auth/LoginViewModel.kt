package com.devusercode.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devusercode.core.domain.auth.actions.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInWithEmail: SignInWithEmail,
    private val sendPasswordReset: SendPasswordReset,
    private val observeRememberMe: ObserveRememberMe,
    private val observeSavedEmail: ObserveSavedEmail,
    private val setRememberMe: SetRememberMe,
    private val setSavedEmail: SetSavedEmail
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeRememberMe().combine(observeSavedEmail()) { remember, savedEmail ->
                remember to (savedEmail ?: "")
            }.collect { (remember, email) ->
                _state.update { it.copy(rememberMe = remember, email = if (remember) email else it.email) }
            }
        }
    }

    fun onEmailChange(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onRememberMeChange(v: Boolean) {
        _state.update { it.copy(rememberMe = v) }
        viewModelScope.launch { setRememberMe(v); if (!v) setSavedEmail(null) }
    }

    fun login(onSuccess: () -> Unit) {
        val (email, password) = state.value.let { it.email.trim() to it.password }

        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Email and password required") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            runCatching { signInWithEmail(email, password) }
                .onSuccess {
                    if (state.value.rememberMe) setSavedEmail(email)
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Login failed") }
                }
        }
    }

    fun forgotPassword() {
        val email = state.value.email.trim()

        if (email.isBlank()) {
            _state.update { it.copy(error = "Enter your email to reset") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            runCatching { sendPasswordReset(email) }
                .onSuccess { _state.update { it.copy(isLoading = false, error = "Password reset email sent") } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to send reset email") } }
        }
    }
}
