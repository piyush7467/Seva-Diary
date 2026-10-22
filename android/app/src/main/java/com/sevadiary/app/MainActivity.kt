package com.sevadiary.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.sevadiary.app.core.designsystem.theme.SevaDiaryTheme
import com.sevadiary.app.core.network.ApiClient
import com.sevadiary.app.core.preferences.PreferencesManager
import com.sevadiary.app.core.security.SecureTokenManager
import com.sevadiary.app.feature.auth.data.AuthRepository
import com.sevadiary.app.feature.auth.presentation.AuthViewModel
import com.sevadiary.app.feature.chanting.data.ChantingRepository
import com.sevadiary.app.feature.chanting.presentation.ChantingViewModel
import com.sevadiary.app.feature.reports.presentation.ReportsViewModel
import com.sevadiary.app.feature.seva.data.SevaRepository
import com.sevadiary.app.feature.seva.presentation.SevaViewModel
import com.sevadiary.app.feature.settings.presentation.SettingsViewModel
import com.sevadiary.app.navigation.NavGraph
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager

    override fun attachBaseContext(newBase: Context) {
        preferencesManager = PreferencesManager(newBase)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core Infrastructure & Network Clients
        val tokenManager = SecureTokenManager(applicationContext)
        val apiService = ApiClient.create(tokenManager)

        // Repositories
        val authRepository = AuthRepository(apiService, tokenManager)
        val chantingRepository = ChantingRepository(apiService)
        val sevaRepository = SevaRepository(apiService)

        // ViewModels
        val authViewModel = AuthViewModel(authRepository)
        val chantingViewModel = ChantingViewModel(chantingRepository)
        val sevaViewModel = SevaViewModel(sevaRepository)
        val reportsViewModel = ReportsViewModel(sevaRepository, chantingRepository)
        val settingsViewModel = SettingsViewModel(apiService, authRepository, preferencesManager)

        setContent {
            val isOnboardingCompleted by preferencesManager.isOnboardingCompleted.collectAsState(initial = false)
            val currentLanguage by preferencesManager.appLanguage.collectAsState(initial = "en")
            val currentTheme by preferencesManager.appTheme.collectAsState(initial = "system")
            val devotionalName by preferencesManager.devotionalName.collectAsState(initial = "")

            val darkTheme = when (currentTheme) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            val navController = rememberNavController()

            SevaDiaryTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        chantingViewModel = chantingViewModel,
                        sevaViewModel = sevaViewModel,
                        reportsViewModel = reportsViewModel,
                        settingsViewModel = settingsViewModel,
                        isOnboardingCompleted = isOnboardingCompleted,
                        isLoggedIn = !tokenManager.getAccessToken().isNullOrBlank() || devotionalName.isNotBlank(),
                        currentLanguage = currentLanguage,
                        currentTheme = currentTheme,
                        devotionalName = devotionalName,
                        onLanguageChange = { lang ->
                            lifecycleScope.launch {
                                preferencesManager.setLanguage(lang)
                                updateLocale(lang)
                            }
                        },
                        onThemeChange = { theme ->
                            lifecycleScope.launch {
                                preferencesManager.setTheme(theme)
                            }
                        },
                        onFinishOnboarding = {
                            lifecycleScope.launch {
                                preferencesManager.setOnboardingCompleted(true)
                            }
                        },
                        onLoginSuccess = { name ->
                            lifecycleScope.launch {
                                preferencesManager.setDevotionalName(name)
                                chantingViewModel.loadData()
                                sevaViewModel.loadRecords()
                                settingsViewModel.loadProfile()
                            }
                        },
                        onLogout = {
                            lifecycleScope.launch {
                                preferencesManager.clearSessionPreferences()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }
}
