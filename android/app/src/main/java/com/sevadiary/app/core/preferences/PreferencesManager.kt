package com.sevadiary.app.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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

        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_THEME = stringPreferencesKey("app_theme")
        private val KEY_DEVOTIONAL_NAME = stringPreferencesKey("devotional_name")
    }

    val isOnboardingCompleted: Flow<Boolean> = appContext.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] ?: false
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
            preferences[KEY_DEVOTIONAL_NAME] ?: ""
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

    suspend fun setOnboardingCompleted(completed: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setLanguage(language: String) {
        LocaleHelper.saveLanguagePreference(appContext, language)
        appContext.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }

    suspend fun setTheme(theme: String) {
        // 1. Immediately persist synchronously to SharedPreferences for instant retrieval on next launch
        appContext.getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_STRING, theme)
            .apply()

        // 2. Persist to DataStore to notify any Flow collectors
        appContext.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun setDevotionalName(name: String) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_DEVOTIONAL_NAME] = name
        }
    }

    suspend fun clearSessionPreferences() {
        appContext.dataStore.edit { preferences ->
            preferences.remove(KEY_DEVOTIONAL_NAME)
        }
    }
}
