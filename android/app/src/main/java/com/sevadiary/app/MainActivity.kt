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
import com.sevadiary.app.core.util.LocaleHelper
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
        val initialLanguage = LocaleHelper.getSavedLanguagePreference(newBase)
        val wrappedContext = LocaleHelper.wrapContext(newBase, initialLanguage)
        preferencesManager = PreferencesManager(wrappedContext)
        super.attachBaseContext(wrappedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val initialLanguage = LocaleHelper.getSavedLanguagePreference(this)
        LocaleHelper.updateResources(this, initialLanguage)
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
            val isOnboardingCompleted by preferencesManager.isOnboardingCompleted.collectAsState(initial = preferencesManager.isOnboardingCompletedSync())
            val isJaapOnboardingCompleted by preferencesManager.isJaapOnboardingCompleted.collectAsState(initial = preferencesManager.isJaapOnboardingCompletedSync())
            val currentLanguage by preferencesManager.appLanguage.collectAsState(initial = preferencesManager.getInitialLanguage())
            val rawLanguage by preferencesManager.rawAppLanguage.collectAsState(initial = preferencesManager.getInitialRawLanguage())
            var currentTheme by remember { mutableStateOf(preferencesManager.getInitialTheme()) }
            val storedTheme by preferencesManager.appTheme.collectAsState(initial = preferencesManager.getInitialTheme())

            LaunchedEffect(storedTheme) {
                currentTheme = storedTheme
            }

            var isSessionValid by remember { mutableStateOf(preferencesManager.isSessionValid()) }
            val devotionalName by preferencesManager.devotionalName.collectAsState(initial = preferencesManager.getInitialDevotionalName())

            // If session is valid on app launch, refresh its timestamp for continuous 1-day rolling window
            LaunchedEffect(Unit) {
                if (preferencesManager.isSessionValid()) {
                    preferencesManager.refreshSessionTimestamp()
                }
            }

            val darkTheme = when (currentTheme) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            val navController = rememberNavController()

            // Session is valid if 1-day duration has not expired and user has valid session credentials
            val isUserLoggedIn = isSessionValid && (devotionalName.isNotBlank() || !tokenManager.getAccessToken().isNullOrBlank())

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
                        isLoggedIn = isUserLoggedIn,
                        isJaapOnboardingCompleted = isJaapOnboardingCompleted,
                        currentLanguage = currentLanguage,
                        rawLanguage = rawLanguage,
                        currentTheme = currentTheme,
                        devotionalName = devotionalName,
                        onLanguageChange = { lang ->
                            lifecycleScope.launch {
                                preferencesManager.setLanguage(lang)
                                LocaleHelper.updateResources(this@MainActivity, lang)
                                recreate()
                            }
                        },
                        onThemeChange = { theme ->
                            currentTheme = theme
                            lifecycleScope.launch {
                                preferencesManager.setTheme(theme)
                            }
                        },
                        onFinishOnboarding = {
                            lifecycleScope.launch {
                                preferencesManager.setOnboardingCompleted(true)
                            }
                        },
                        onFinishJaapOnboarding = {
                            lifecycleScope.launch {
                                preferencesManager.setJaapOnboardingCompleted(true)
                            }
                        },
                        onLoginSuccess = { name ->
                            isSessionValid = true
                            lifecycleScope.launch {
                                preferencesManager.saveLoginSession(name)
                                chantingViewModel.loadData()
                                sevaViewModel.loadRecords()
                                settingsViewModel.loadProfile()
                            }
                        },
                        onLogout = {
                            isSessionValid = false
                            tokenManager.clearTokens()
                            lifecycleScope.launch {
                                preferencesManager.clearSessionPreferences()
                            }
                        }
                    )
                }
            }
        }
    }
}
