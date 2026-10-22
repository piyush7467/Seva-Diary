package com.sevadiary.app.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "seva_diary_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_THEME = stringPreferencesKey("app_theme")
        private val KEY_DEVOTIONAL_NAME = stringPreferencesKey("devotional_name")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: "en"
    }

    val appTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "system"
    }

    val devotionalName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEVOTIONAL_NAME] ?: ""
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun setDevotionalName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DEVOTIONAL_NAME] = name
        }
    }

    suspend fun clearSessionPreferences() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_DEVOTIONAL_NAME)
            // Onboarding and language/theme preferences are preserved as per spec
        }
    }
}
