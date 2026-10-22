package com.sevadiary.app.core.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.Locale

object LocaleHelper {

    private const val PREFS_NAME = "seva_diary_locale_pref"
    private const val KEY_LANGUAGE = "app_language"

    /**
     * Returns "hi" if the device's system language is Hindi, otherwise "en".
     */
    fun getSystemLanguage(): String {
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0) ?: Locale.getDefault()
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale ?: Locale.getDefault()
        }
        val lang = systemLocale.language.lowercase()
        return if (lang.startsWith("hi")) "hi" else "en"
    }

    /**
     * Resolves the effective language tag to use for string resources.
     * If user explicitly selected "hi" or "en", returns that.
     * If null, blank, or "system", dynamically detects system language ("hi" or "en").
     */
    fun getEffectiveLanguage(savedLang: String?): String {
        return if (!savedLang.isNullOrBlank() && savedLang != "system") {
            savedLang
        } else {
            getSystemLanguage()
        }
    }

    /**
     * Reads saved raw language preference synchronously.
     * Defaults to "system" when not set.
     */
    fun getSavedRawLanguagePreference(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "system") ?: "system"
    }

    /**
     * Reads saved language preference synchronously and resolves to effective language code.
     * Used for attachBaseContext on app launch before Compose/DataStore initialize.
     */
    fun getSavedLanguagePreference(context: Context): String {
        val raw = getSavedRawLanguagePreference(context)
        return getEffectiveLanguage(raw)
    }

    /**
     * Persists language to lightweight SharedPreferences synchronously.
     */
    fun saveLanguagePreference(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    /**
     * Wraps context with the appropriate Locale configuration.
     */
    fun wrapContext(context: Context, languageCode: String): Context {
        val effectiveLang = getEffectiveLanguage(languageCode)
        val locale = Locale.forLanguageTag(effectiveLang)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Updates resources configuration in-place for existing contexts.
     */
    @Suppress("DEPRECATION")
    fun updateResources(context: Context, languageCode: String) {
        val effectiveLang = getEffectiveLanguage(languageCode)
        val locale = Locale.forLanguageTag(effectiveLang)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
