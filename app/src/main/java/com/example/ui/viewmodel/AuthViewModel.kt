package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    val currentSession: StateFlow<UserSession> = repository.currentSession

    val usernameInput = MutableStateFlow("")
    val emailInput = MutableStateFlow("")
    val passwordInput = MutableStateFlow("")

    val isSignUpMode = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
    val successMessage = MutableStateFlow<String?>(null)
    val isLoading = MutableStateFlow(false)

    fun toggleAuthMode() {
        isSignUpMode.value = !isSignUpMode.value
        errorMessage.value = null
        successMessage.value = null
    }

    fun submitAuth() {
        errorMessage.value = null
        successMessage.value = null
        isLoading.value = true

        viewModelScope.launch {
            if (isSignUpMode.value) {
                val result = repository.signUp(
                    username = usernameInput.value,
                    email = emailInput.value,
                    password = passwordInput.value
                )
                isLoading.value = false
                result.onSuccess {
                    successMessage.value = "Account created successfully! Welcome, ${it.username}."
                    clearInputs()
                }.onFailure {
                    errorMessage.value = it.message ?: "Registration failed."
                }
            } else {
                val result = repository.signIn(
                    emailOrUsername = emailInput.value.ifBlank { usernameInput.value },
                    password = passwordInput.value
                )
                isLoading.value = false
                result.onSuccess {
                    successMessage.value = "Welcome back, ${it.username}!"
                    clearInputs()
                }.onFailure {
                    errorMessage.value = it.message ?: "Sign-in failed."
                }
            }
        }
    }

    fun continueAsGuest() {
        repository.continueAsGuest()
        errorMessage.value = null
    }

    fun signOut() {
        repository.signOut()
        clearInputs()
    }

    private fun clearInputs() {
        usernameInput.value = ""
        emailInput.value = ""
        passwordInput.value = ""
    }
}
