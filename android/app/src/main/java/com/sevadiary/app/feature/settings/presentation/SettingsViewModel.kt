package com.sevadiary.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevadiary.app.core.network.SevaDiaryApiService
import com.sevadiary.app.core.network.dto.ChangePasswordOtpRequest
import com.sevadiary.app.core.network.dto.UpdateProfileRequest
import com.sevadiary.app.core.network.dto.UserProfileDto
import com.sevadiary.app.core.preferences.PreferencesManager
import com.sevadiary.app.feature.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userProfile: UserProfileDto? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isRequestingOtp: Boolean = false,
    val isVerifyingOtp: Boolean = false,
    val otpSentSuccess: Boolean = false,
    val maskedEmail: String? = null,
    val changePasswordSuccess: Boolean = false,
    val isLoggingOut: Boolean = false
)

class SettingsViewModel(
    private val apiService: SevaDiaryApiService,
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        if (preferencesManager.isSessionValid()) {
            loadProfile()
        }
    }

    fun loadProfile() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = apiService.getProfile()
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    val profile = response.body()!!.data!!
                    _uiState.value = _uiState.value.copy(
                        userProfile = profile,
                        isLoading = false
                    )
                    preferencesManager.setDevotionalName(profile.displayName)
                    preferencesManager.refreshSessionTimestamp()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateProfile(
        request: UpdateProfileRequest,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = apiService.updateProfile(request)
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    val updated = response.body()!!.data!!
                    _uiState.value = _uiState.value.copy(
                        userProfile = updated,
                        isSaving = false,
                        isSuccess = true
                    )
                    preferencesManager.setDevotionalName(updated.displayName)
                    onSuccess()
                } else {
                    val errorMsg = response.body()?.error?.message ?: response.errorBody()?.string() ?: "Failed to update profile."
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = errorMsg)
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Network error. Please try again."
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = errorMsg)
                onError(errorMsg)
            }
        }
    }

    fun requestChangePasswordOtp(
        onSuccess: (String?) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        _uiState.value = _uiState.value.copy(isRequestingOtp = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = apiService.requestChangePasswordOtp()
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    val masked = data?.get("maskedEmail")
                    _uiState.value = _uiState.value.copy(
                        isRequestingOtp = false,
                        otpSentSuccess = true,
                        maskedEmail = masked
                    )
                    onSuccess(masked)
                } else {
                    val errorMsg = response.body()?.error?.message ?: "Failed to send verification OTP."
                    _uiState.value = _uiState.value.copy(isRequestingOtp = false, errorMessage = errorMsg)
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Failed to send verification OTP."
                _uiState.value = _uiState.value.copy(isRequestingOtp = false, errorMessage = errorMsg)
                onError(errorMsg)
            }
        }
    }

    fun verifyChangePasswordOtp(
        otp: String,
        newPassword: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        _uiState.value = _uiState.value.copy(isVerifyingOtp = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = apiService.verifyChangePasswordOtp(
                    ChangePasswordOtpRequest(otp = otp.trim(), newPassword = newPassword)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isVerifyingOtp = false,
                        changePasswordSuccess = true
                    )
                    onSuccess()
                } else {
                    val errorMsg = response.body()?.error?.message ?: "Invalid or expired OTP."
                    _uiState.value = _uiState.value.copy(isVerifyingOtp = false, errorMessage = errorMsg)
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Failed to verify OTP."
                _uiState.value = _uiState.value.copy(isVerifyingOtp = false, errorMessage = errorMsg)
                onError(errorMsg)
            }
        }
    }

    fun resetPasswordState() {
        _uiState.value = _uiState.value.copy(
            otpSentSuccess = false,
            maskedEmail = null,
            changePasswordSuccess = false,
            errorMessage = null
        )
    }

    fun logout(onLoggedOut: () -> Unit) {
        _uiState.value = _uiState.value.copy(isLoggingOut = true)
        viewModelScope.launch {
            authRepository.logout()
            preferencesManager.clearSessionPreferences()
            _uiState.value = _uiState.value.copy(isLoggingOut = false)
            onLoggedOut()
        }
    }
}
