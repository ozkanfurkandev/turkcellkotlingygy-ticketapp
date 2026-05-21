package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.data.network.ApiException
import com.turkcell.data.network.NetworkException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false,
) {
    val passwordsMatch: Boolean get() = password == confirmPassword
    val canSubmit: Boolean
        get() = email.isNotBlank() &&
                password.length in 8..128 &&
                passwordsMatch &&
                !isLoading
}

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChange(value: String) = _state.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun consumeError() = _state.update { it.copy(errorMessage = null) }

    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.register(current.email, current.password)
                .onSuccess { _state.update { it.copy(isLoading = false, isRegistered = true) } }
                .onFailure { error -> _state.update { it.copy(isLoading = false, errorMessage = error.toRegisterUserMessage()) } }
        }
    }
}

internal fun Throwable.toRegisterUserMessage(): String = when (this) {
    is ApiException -> when (code) {
        409 -> "Bu email zaten kayıtlı"
        400 -> "Geçersiz email veya şifre formatı"
        in 500..599 -> "Sunucu şu anda cevap veremiyor"
        else -> "Beklenmeyen bir hata oluştu"
    }
    is NetworkException -> "İnternet bağlantısı yok"
    else -> message ?: "Bilinmeyen bir hata oluştu."
}
