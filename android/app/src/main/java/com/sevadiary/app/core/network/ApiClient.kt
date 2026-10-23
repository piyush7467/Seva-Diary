package com.sevadiary.app.core.network

import android.os.Build
import com.sevadiary.app.core.network.dto.RefreshRequest
import com.sevadiary.app.core.security.SecureTokenManager
import okhttp3.Authenticator
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthInterceptor(private val tokenManager: SecureTokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val accessToken = tokenManager.getAccessToken()

        val request = if (!accessToken.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}

class TokenAuthenticator(
    private val tokenManager: SecureTokenManager
) : Authenticator {

    // Bare client dedicated exclusively to token refresh without AuthInterceptor or Authenticator (zero recursion/deadlock risk)
    private val refreshHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.contains("/auth/refresh")) {
            return null
        }

        // Avoid repeated refresh if another concurrent request already updated the token
        val currentAccessToken = tokenManager.getAccessToken()
        val authHeader = response.request.header("Authorization")
        if (authHeader != null && !currentAccessToken.isNullOrBlank() && authHeader != "Bearer $currentAccessToken") {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentAccessToken")
                .build()
        }

        val currentRefreshToken = tokenManager.getRefreshToken() ?: return null

        return try {
            val refreshUrl = ApiClient.baseUrl.trimEnd('/') + "/api/v1/auth/refresh"
            val jsonPayload = JSONObject().apply {
                put("refreshToken", currentRefreshToken)
            }.toString()

            val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())
            val refreshRequest = Request.Builder()
                .url(refreshUrl)
                .post(requestBody)
                .build()

            val refreshResponse = refreshHttpClient.newCall(refreshRequest).execute()
            if (refreshResponse.isSuccessful) {
                val rawBody = refreshResponse.body?.string()
                if (!rawBody.isNullOrBlank()) {
                    val rootJson = JSONObject(rawBody)
                    if (rootJson.optBoolean("success", false) && rootJson.has("data")) {
                        val dataObj = rootJson.getJSONObject("data")
                        val newAccessToken = dataObj.optString("accessToken", "")
                        val newRefreshToken = dataObj.optString("refreshToken", "")
                        if (newAccessToken.isNotBlank() && newRefreshToken.isNotBlank()) {
                            tokenManager.saveTokens(newAccessToken, newRefreshToken)
                            return response.request.newBuilder()
                                .header("Authorization", "Bearer $newAccessToken")
                                .build()
                        }
                    }
                }
                null
            } else if (refreshResponse.code in listOf(400, 401, 403)) {
                // Refresh token has expired or is invalid on server
                tokenManager.clearTokens()
                null
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}

class AutoFailoverInterceptor : Interceptor {
    @Volatile
    var activeHost: String? = null

    // Candidates to try when local development server is on port 5000:
    // 127.0.0.1 -> USB cable with adb reverse
    // 10.35.29.227 -> Wi-Fi LAN address of PC
    // 10.0.2.2 -> Android Studio Emulator loopback
    private val candidateHosts = listOf(
        "127.0.0.1",
        "10.35.29.227",
        "10.0.2.2"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val currentTarget = ApiClient.baseUrl.toHttpUrlOrNull()

        // Dynamically route to current ApiClient.baseUrl if configured
        val targetRequest = if (currentTarget != null &&
            (originalUrl.host != currentTarget.host || originalUrl.scheme != currentTarget.scheme || originalUrl.port != currentTarget.port)) {
            val rewrittenUrl = originalUrl.newBuilder()
                .scheme(currentTarget.scheme)
                .host(currentTarget.host)
                .port(currentTarget.port)
                .build()
            originalRequest.newBuilder().url(rewrittenUrl).build()
        } else {
            originalRequest
        }
        val targetUrl = targetRequest.url

        if (targetUrl.port == 5000) {
            val knownHost = activeHost
            if (knownHost != null && knownHost != targetUrl.host) {
                val updatedUrl = targetUrl.newBuilder().host(knownHost).build()
                val updatedRequest = targetRequest.newBuilder().url(updatedUrl).build()
                try {
                    return chain.proceed(updatedRequest)
                } catch (_: Exception) {
                    activeHost = null
                }
            }

            try {
                val response = chain.proceed(targetRequest)
                if (response.isSuccessful || response.code < 500) {
                    activeHost = targetUrl.host
                }
                return response
            } catch (primaryException: Exception) {
                for (candidate in candidateHosts) {
                    if (candidate == targetUrl.host) continue
                    val fallbackUrl = targetUrl.newBuilder().host(candidate).build()
                    val fallbackRequest = targetRequest.newBuilder().url(fallbackUrl).build()
                    try {
                        val response = chain.proceed(fallbackRequest)
                        activeHost = candidate
                        return response
                    } catch (_: Exception) {
                        // Continue to next candidate
                    }
                }
                throw primaryException
            }
        }

        return chain.proceed(targetRequest)
    }
}

object ApiClient {
    const val CLOUD_URL = "https://seva-diary-backend.vercel.app/"
    const val USB_URL = "http://127.0.0.1:5000/"
    const val WIFI_URL = "http://10.35.29.227:5000/"
    const val EMULATOR_URL = "http://10.0.2.2:5000/"

    // Defaults to live production Vercel cloud; fallbacks/presets selectable in app
    var baseUrl: String = CLOUD_URL

    val failoverInterceptor = AutoFailoverInterceptor()

    fun updateHost(host: String) {
        val trimmed = host.trim()
        val formatted = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "http://$trimmed"
        } else {
            trimmed
        }
        val finalUrl = if (formatted.endsWith("/")) formatted else "$formatted/"
        baseUrl = finalUrl
        failoverInterceptor.activeHost = null
    }

    fun create(tokenManager: SecureTokenManager): SevaDiaryApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = AuthInterceptor(tokenManager)
        val tokenAuthenticator = TokenAuthenticator(tokenManager)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(failoverInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofitInstance = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofitInstance.create(SevaDiaryApiService::class.java)
    }
}
