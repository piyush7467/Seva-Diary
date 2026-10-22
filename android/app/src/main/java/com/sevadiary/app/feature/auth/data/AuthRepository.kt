package com.sevadiary.app.feature.auth.data

import com.sevadiary.app.core.network.SevaDiaryApiService
import com.sevadiary.app.core.network.dto.*
import com.sevadiary.app.core.security.SecureTokenManager

class AuthRepository(
    private val apiService: SevaDiaryApiService,
    private val tokenManager: SecureTokenManager
) {

    private fun parseErrorMessage(response: retrofit2.Response<*>): String {
        return try {
            val raw = response.errorBody()?.string()
            if (!raw.isNullOrBlank()) {
                val json = org.json.JSONObject(raw)
                if (json.has("error")) {
                    val err = json.getJSONObject("error")
                    err.optString("message", "Request failed (HTTP ${response.code()})")
                } else {
                    raw
                }
            } else {
                "Request failed (HTTP ${response.code()})"
            }
        } catch (_: Exception) {
            "Request failed (HTTP ${response.code()})"
        }
    }

    suspend fun register(request: RegisterRequest): Result<UserProfileDto> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Registration succeeded but user profile was empty."))
                }
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Connection timed out. Please verify your internet connection."))
        } catch (e: java.net.ConnectException) {
            Result.failure(Exception("Cannot reach server at ${com.sevadiary.app.core.network.ApiClient.baseUrl}. Please check your connection."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Could not connect to server. Is the backend running?"))
        }
    }

    suspend fun login(request: LoginRequest): Result<AuthResponseData> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    tokenManager.saveTokens(data.accessToken, data.refreshToken)
                    Result.success(data)
                } else {
                    Result.failure(Exception("Login succeeded but token response was empty."))
                }
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Connection timed out. Please verify your internet connection."))
        } catch (e: java.net.ConnectException) {
            Result.failure(Exception("Cannot reach server at ${com.sevadiary.app.core.network.ApiClient.baseUrl}. Please check your connection."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Could not connect to server. Is the backend running?"))
        }
    }

    suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                Result.success("Password reset link sent if email is registered.")
            } else {
                Result.failure(Exception("Failed to send reset link."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Network connection error."))
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val currentRefreshToken = tokenManager.getRefreshToken()
            if (!currentRefreshToken.isNullOrBlank()) {
                apiService.logout(RefreshRequest(currentRefreshToken))
            }
            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clearTokens()
            Result.success(Unit)
        }
    }
}
