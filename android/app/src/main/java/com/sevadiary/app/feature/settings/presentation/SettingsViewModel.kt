package com.sevadiary.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevadiary.app.core.network.SevaDiaryApiService
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
    val errorMessage: String? = null
)

class SettingsViewModel(
    private val apiService: SevaDiaryApiService,
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
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
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            preferencesManager.clearSessionPreferences()
            onLoggedOut()
        }
    }
}
