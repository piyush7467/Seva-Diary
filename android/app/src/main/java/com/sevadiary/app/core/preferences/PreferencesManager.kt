package com.sevadiary.app.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sevadiary.app.core.util.LocaleHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "seva_diary_preferences")

class PreferencesManager(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        private const val THEME_PREFS_NAME = "seva_diary_theme_pref"
        private const val KEY_THEME_STRING = "app_theme"

        private const val SESSION_PREFS_NAME = "seva_diary_session_pref"
        private const val KEY_ONBOARDING_COMPLETED_STRING = "onboarding_completed"
        private const val KEY_JAAP_ONBOARDING_COMPLETED_STRING = "jaap_onboarding_completed"
        private const val KEY_IS_LOGGED_IN_STRING = "is_logged_in"
        private const val KEY_LOGIN_TIMESTAMP_STRING = "login_timestamp"
        private const val KEY_DEVOTIONAL_NAME_STRING = "devotional_name"

        // 1 Day Login Session (24 hours in milliseconds)
        const val SESSION_DURATION_MILLIS = 24 * 60 * 60 * 1000L

        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_JAAP_ONBOARDING_COMPLETED = booleanPreferencesKey("jaap_onboarding_completed")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_LOGIN_TIMESTAMP = longPreferencesKey("login_timestamp")
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_THEME = stringPreferencesKey("app_theme")
        private val KEY_DEVOTIONAL_NAME = stringPreferencesKey("devotional_name")
    }

    val isOnboardingCompleted: Flow<Boolean> = appContext.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] ?: isOnboardingCompletedSync()
        }

    val isJaapOnboardingCompleted: Flow<Boolean> = appContext.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_JAAP_ONBOARDING_COMPLETED] ?: isJaapOnboardingCompletedSync()
        }

    val isLoggedIn: Flow<Boolean> = appContext.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_IS_LOGGED_IN] ?: isSessionValid()
        }

    /**
     * Emits the raw language choice ("system", "en", or "hi").
     * Defaults to "system" so the app aligns with device settings by default.
     */
    val rawAppLanguage: Flow<String> = appContext.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_LANGUAGE] ?: LocaleHelper.getSavedRawLanguagePreference(appContext)
        }

    /**
     * Emits the resolved effective language code ("hi" or "en") for string loading.
     * If user chose "system" or hasn't selected yet, dynamically resolves to system language.
     */
    val appLanguage: Flow<String> = appContext.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val saved = preferences[KEY_LANGUAGE] ?: LocaleHelper.getSavedRawLanguagePreference(appContext)
            LocaleHelper.getEffectiveLanguage(saved)
        }

    /**
     * Emits the theme mode: "system" (default), "light", or "dark".
     */
    val appTheme: Flow<String> = appContext.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_THEME] ?: getInitialTheme()
        }

    val devotionalName: Flow<String> = appContext.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_DEVOTIONAL_NAME] ?: getInitialDevotionalName()
        }

    fun getInitialLanguage(): String {
        return LocaleHelper.getSavedLanguagePreference(appContext)
    }

    fun getInitialRawLanguage(): String {
        return LocaleHelper.getSavedRawLanguagePreference(appContext)
    }

    /**
     * Reads saved theme synchronously for immediate startup without async flicker.
     */
    fun getInitialTheme(): String {
        val prefs = appContext.getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME_STRING, "system") ?: "system"
    }

    /**
     * Reads onboarding completion status synchronously for instant startup routing.
     */
    fun isOnboardingCompletedSync(): Boolean {
        val prefs = appContext.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED_STRING, false)
    }

    /**
     * Reads Naam Jaap devotional 6-step onboarding completion status synchronously.
     */
    fun isJaapOnboardingCompletedSync(): Boolean {
        val prefs = appContext.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_JAAP_ONBOARDING_COMPLETED_STRING, false)
    }

    /**
     * Reads devotional display name synchronously for zero-lag display in UI.
     */
    fun getInitialDevotionalName(): String {
        val prefs = appContext.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DEVOTIONAL_NAME_STRING, "") ?: ""
    }

    /**
     * Validates whether the user has an active, unexpired login session.
     * Session is valid for 1 day (24 hours). If expired, automatically clears session flag.
     */
    fun isSessionValid(): Boolean {
        val prefs = appContext.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN_STRING, false)
        if (!loggedIn) return false

        val loginTimestamp = prefs.getLong(KEY_LOGIN_TIMESTAMP_STRING, 0L)
        if (loginTimestamp <= 0L) return false

        val now = System.currentTimeMillis()
        val elapsed = now - loginTimestamp

        // Check if within the 1-day session window
        val isValid = elapsed in 0..SESSION_DURATION_MILLIS

        if (!isValid) {
            // Expired: automatically clear logged in state
            prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN_STRING, false)
                .putLong(KEY_LOGIN_TIMESTAMP_STRING, 0L)
                .apply()
        }
        return isValid
    }

    /**
     * Refreshes the session timestamp to maintain the rolling 1-day active session window while user uses the app.
     */
    fun refreshSessionTimestamp() {
        val prefs = appContext.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_IS_LOGGED_IN_STRING, false)) {
            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_LOGIN_TIMESTAMP_STRING, now).apply()
        }
    }

    /**
     * Saves user login session for 1 day, updating both synchronous SharedPreferences and DataStore.
     */
    suspend fun saveLoginSession(name: String) {
        val now = System.currentTimeMillis()

        // 1. Synchronously persist to SharedPreferences for instant retrieval on next app launch
        appContext.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_LOGGED_IN_STRING, true)
            .putLong(KEY_LOGIN_TIMESTAMP_STRING, now)
            .putString(KEY_DEVOTIONAL_NAME_STRING, name)
            .apply()

        // 2. Persist to DataStore to notify Compose flows
        appContext.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = true
            preferences[KEY_LOGIN_TIMESTAMP] = now
            preferences[KEY_DEVOTIONAL_NAME] = name
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        // Synchronously persist
        appContext.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED_STRING, completed)
            .apply()

        appContext.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setJaapOnboardingCompleted(completed: Boolean) {
        appContext.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_JAAP_ONBOARDING_COMPLETED_STRING, completed)
            .apply()

        appContext.dataStore.edit { preferences ->
            preferences[KEY_JAAP_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setLanguage(language: String) {
        LocaleHelper.saveLanguagePreference(appContext, language)
        appContext.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }

    suspend fun setTheme(theme: String) {
        appContext.getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_STRING, theme)
            .apply()

        appContext.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun setDevotionalName(name: String) {
        appContext.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DEVOTIONAL_NAME_STRING, name)
            .apply()

        appContext.dataStore.edit { preferences ->
            preferences[KEY_DEVOTIONAL_NAME] = name
        }
    }

    /**
     * Clears all session preferences when user logs out.
     */
    suspend fun clearSessionPreferences() {
        // 1. Synchronously clear SharedPreferences
        appContext.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_LOGGED_IN_STRING, false)
            .putLong(KEY_LOGIN_TIMESTAMP_STRING, 0L)
            .remove(KEY_DEVOTIONAL_NAME_STRING)
            .apply()

        // 2. Clear DataStore session keys
        appContext.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = false
            preferences[KEY_LOGIN_TIMESTAMP] = 0L
            preferences.remove(KEY_DEVOTIONAL_NAME)
        }
    }
}
