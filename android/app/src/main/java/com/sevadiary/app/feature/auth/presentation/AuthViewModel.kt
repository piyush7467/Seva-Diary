package com.sevadiary.app.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevadiary.app.core.network.dto.*
import com.sevadiary.app.feature.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class RegisterSuccess(val user: UserProfileDto) : AuthUiState()
    data class LoginSuccess(val authData: AuthResponseData) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun register(
        firstName: String,
        lastName: String,
        gender: String,
        displayName: String,
        username: String,
        email: String,
        phone: String,
        addressLine: String,
        cityVillage: String,
        tehsil: String = "",
        district: String,
        state: String,
        pinCode: String,
        mantraStage: String = "Pratham Nam",
        mantraDetails: String = "",
        password: String
    ) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val request = RegisterRequest(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                gender = gender,
                displayName = displayName.trim(),
                username = username.trim().lowercase(),
                email = email.trim().lowercase(),
                phone = phone.trim(),
                address = AddressDto(
                    addressLine = addressLine.trim(),
                    cityVillage = cityVillage.trim(),
                    tehsil = tehsil.trim(),
                    district = district.trim(),
                    state = state.trim(),
                    pinCode = pinCode.trim()
                ),
                mantraStage = mantraStage.trim(),
                mantraDetails = mantraDetails.trim(),
                password = password
            )

            val result = authRepository.register(request)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.RegisterSuccess(user)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Registration failed.")
                }
            )
        }
    }

    fun login(usernameOrEmail: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val request = LoginRequest(
                usernameOrEmail = usernameOrEmail.trim().lowercase(),
                password = password
            )
            val result = authRepository.login(request)
            result.fold(
                onSuccess = { authData ->
                    _uiState.value = AuthUiState.LoginSuccess(authData)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Login failed.")
                }
            )
        }
    }
}
