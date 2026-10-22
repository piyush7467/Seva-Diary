package com.sevadiary.app.core.network

import android.os.Build
import com.sevadiary.app.core.network.dto.RefreshRequest
import com.sevadiary.app.core.security.SecureTokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
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
    private val tokenManager: SecureTokenManager,
    private val getRefreshService: () -> SevaDiaryApiService
) : Authenticator {

    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.contains("/auth/refresh")) {
            return null
        }

        val currentRefreshToken = tokenManager.getRefreshToken() ?: return null

        val refreshResponse = runBlocking {
            try {
                getRefreshService().refresh(RefreshRequest(currentRefreshToken))
            } catch (e: Exception) {
                null
            }
        }

        val newTokens = refreshResponse?.body()?.data
        if (refreshResponse != null && refreshResponse.isSuccessful && newTokens != null) {
            tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.accessToken}")
                .build()
        } else {
            tokenManager.clearTokens()
            return null
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

        if (originalUrl.port == 5000) {
            val knownHost = activeHost
            if (knownHost != null && knownHost != originalUrl.host) {
                val updatedUrl = originalUrl.newBuilder().host(knownHost).build()
                val updatedRequest = originalRequest.newBuilder().url(updatedUrl).build()
                try {
                    return chain.proceed(updatedRequest)
                } catch (_: Exception) {
                    activeHost = null
                }
            }

            try {
                val response = chain.proceed(originalRequest)
                if (response.isSuccessful || response.code < 500) {
                    activeHost = originalUrl.host
                }
                return response
            } catch (primaryException: Exception) {
                for (candidate in candidateHosts) {
                    if (candidate == originalUrl.host) continue
                    val fallbackUrl = originalUrl.newBuilder().host(candidate).build()
                    val fallbackRequest = originalRequest.newBuilder().url(fallbackUrl).build()
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

        return chain.proceed(originalRequest)
    }
}

object ApiClient {
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    // Default to 127.0.0.1 on physical device (adb reverse) or 10.0.2.2 on emulator
    var baseUrl: String = if (isEmulator()) {
        "http://10.0.2.2:5000/"
    } else {
        "http://127.0.0.1:5000/"
    }

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

        var retrofitInstance: Retrofit? = null

        val authInterceptor = AuthInterceptor(tokenManager)
        val tokenAuthenticator = TokenAuthenticator(tokenManager) {
            retrofitInstance!!.create(SevaDiaryApiService::class.java)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(failoverInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        retrofitInstance = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofitInstance.create(SevaDiaryApiService::class.java)
    }
}
