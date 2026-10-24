package com.sevadiary.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sevadiary.app.feature.auth.ForgotPasswordScreen
import com.sevadiary.app.feature.auth.LoginScreen
import com.sevadiary.app.feature.auth.RegisterScreen
import com.sevadiary.app.feature.auth.presentation.AuthViewModel
import com.sevadiary.app.feature.chanting.ChantingScreen
import com.sevadiary.app.feature.chanting.JaapOnboardingScreen
import com.sevadiary.app.feature.chanting.presentation.ChantingViewModel
import com.sevadiary.app.feature.home.AllFeaturesScreen
import com.sevadiary.app.feature.home.DashboardScreen
import com.sevadiary.app.feature.onboarding.OnboardingScreen
import com.sevadiary.app.feature.reports.ReportsScreen
import com.sevadiary.app.feature.reports.presentation.ReportsViewModel
import com.sevadiary.app.feature.seva.AddEditVisitScreen
import com.sevadiary.app.feature.seva.AshramDiaryScreen
import com.sevadiary.app.feature.seva.AshramVisitDetailsScreen
import com.sevadiary.app.feature.seva.presentation.SevaViewModel
import com.sevadiary.app.feature.settings.SettingsScreen
import com.sevadiary.app.feature.settings.presentation.SettingsViewModel
import com.sevadiary.app.feature.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    chantingViewModel: ChantingViewModel,
    sevaViewModel: SevaViewModel,
    reportsViewModel: ReportsViewModel,
    settingsViewModel: SettingsViewModel,
    isOnboardingCompleted: Boolean,
    isLoggedIn: Boolean,
    isJaapOnboardingCompleted: Boolean = false,
    currentLanguage: String,
    rawLanguage: String,
    currentTheme: String,
    devotionalName: String,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onFinishOnboarding: () -> Unit,
    onFinishJaapOnboarding: () -> Unit = {},
    onLoginSuccess: (devotionalName: String) -> Unit,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                isOnboardingCompleted = isOnboardingCompleted,
                isLoggedIn = isLoggedIn
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                currentLanguage = currentLanguage,
                rawLanguage = rawLanguage,
                currentTheme = currentTheme,
                onLanguageChange = onLanguageChange,
                onThemeChange = onThemeChange,
                onFinishOnboarding = {
                    onFinishOnboarding()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onLoginSuccess = { name ->
                    onLoginSuccess(name)
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegistrationSuccess = { name ->
                    onLoginSuccess(name)
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                devotionalName = devotionalName,
                chantingViewModel = chantingViewModel,
                sevaViewModel = sevaViewModel,
                onNavigateToChanting = { navController.navigate(Screen.JaapOnboarding.route) },
                onNavigateToAshram = { navController.navigate(Screen.AshramDiary.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAllFeatures = { navController.navigate(Screen.AllFeatures.route) }
            )
        }

        composable(Screen.AllFeatures.route) {
            AllFeaturesScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToChanting = { navController.navigate(Screen.JaapOnboarding.route) },
                onNavigateToAshram = { navController.navigate(Screen.AshramDiary.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.JaapOnboarding.route) {
            JaapOnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Screen.Chanting.route) {
                        popUpTo(Screen.JaapOnboarding.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Chanting.route) {
            ChantingScreen(
                viewModel = chantingViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AshramDiary.route) {
            AshramDiaryScreen(
                viewModel = sevaViewModel,
                onBackClick = { navController.popBackStack() },
                onAddNewVisit = { navController.navigate(Screen.AddEditVisit.createRoute(null)) },
                onViewVisitDetails = { visitId -> navController.navigate(Screen.VisitDetails.createRoute(visitId)) },
                onEditVisit = { visitId -> navController.navigate(Screen.AddEditVisit.createRoute(visitId)) }
            )
        }

        composable(
            route = Screen.VisitDetails.route,
            arguments = listOf(
                navArgument("visitId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val visitId = backStackEntry.arguments?.getString("visitId") ?: ""
            AshramVisitDetailsScreen(
                viewModel = sevaViewModel,
                visitId = visitId,
                onBackClick = { navController.popBackStack() },
                onEditVisit = { id -> navController.navigate(Screen.AddEditVisit.createRoute(id)) },
                onDeleteVisitSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = "add_edit_visit?visitId={visitId}",
            arguments = listOf(
                navArgument("visitId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val visitId = backStackEntry.arguments?.getString("visitId")
            AddEditVisitScreen(
                viewModel = sevaViewModel,
                visitId = visitId,
                onBackClick = { navController.popBackStack() },
                onSavedSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                viewModel = reportsViewModel,
                devotionalName = devotionalName,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                devotionalName = devotionalName,
                currentLanguage = currentLanguage,
                rawLanguage = rawLanguage,
                currentTheme = currentTheme,
                onLanguageChange = onLanguageChange,
                onThemeChange = onThemeChange,
                onOpenGuide = { navController.navigate(Screen.Onboarding.route) },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToJaap = { navController.navigate(Screen.JaapOnboarding.route) },
                onNavigateToVisits = { navController.navigate(Screen.AshramDiary.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) }
            )
        }
    }
}
