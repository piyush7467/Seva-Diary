package com.sevadiary.app.core.network.dto

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ApiError?
)

data class ApiError(
    val code: String,
    val message: String
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String = "",
    val gender: String = "MALE",
    val displayName: String,
    val username: String,
    val email: String,
    val phone: String = "",
    val address: AddressDto = AddressDto(),
    val mantraStage: String = "Pratham Nam",
    val mantraDetails: String = "",
    val password: String
)

data class AddressDto(
    val addressLine: String = "",
    val cityVillage: String = "",
    val tehsil: String = "",
    val district: String = "",
    val state: String = "",
    val pinCode: String = ""
)

data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

data class AuthResponseData(
    val accessToken: String,
    val refreshToken: String,
    val user: UserProfileDto
)

data class RefreshRequest(
    val refreshToken: String
)

data class RefreshResponseData(
    val accessToken: String,
    val refreshToken: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponseData(
    val email: String? = null,
    val maskedEmail: String? = null
)

data class ResetPasswordRequest(
    val email: String? = null,
    val otp: String? = null,
    val token: String? = null,
    val newPassword: String
)

data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String? = null,
    val displayName: String? = null,
    val phone: String? = null,
    val address: AddressDto? = null,
    val mantraStage: String? = null,
    val mantraDetails: String? = null
)

data class ChangePasswordOtpRequest(
    val otp: String,
    val newPassword: String
)

data class UserProfileDto(
    val id: String? = null,
    val userId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String = "MALE",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val address: AddressDto = AddressDto(),
    val mantraStage: String = "Pratham Nam",
    val mantraDetails: String = "",
    val preferences: PreferencesDto? = null
)

data class PreferencesDto(
    val theme: String = "system",
    val language: String = "en"
)

data class AppUpdateDto(
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val title: String = "New Update Available",
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val isForceUpdate: Boolean = false
)
