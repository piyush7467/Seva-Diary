package com.sevadiary.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")
    object JaapOnboarding : Screen("jaap_onboarding")
    object Chanting : Screen("chanting")
    object AshramDiary : Screen("ashram_diary")
    object VisitDetails : Screen("visit_details/{visitId}") {
        fun createRoute(visitId: String) = "visit_details/$visitId"
    }
    object AddEditVisit : Screen("add_edit_visit?visitId={visitId}") {
        fun createRoute(visitId: String? = null) = if (visitId != null) "add_edit_visit?visitId=$visitId" else "add_edit_visit"
    }
    object Reports : Screen("reports")
    object ReportPreview : Screen("report_preview/{reportType}") {
        fun createRoute(reportType: String) = "report_preview/$reportType"
    }
    object Settings : Screen("settings")
    object EditProfile : Screen("edit_profile")
}
