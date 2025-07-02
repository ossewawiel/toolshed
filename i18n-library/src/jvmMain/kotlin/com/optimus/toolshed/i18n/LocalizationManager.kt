/*
 * Optimus Toolshed - Internationalization Library
 * 
 * Core internationalization services providing locale management,
 * resource loading, and text localization for all application components.
 */

package com.optimus.toolshed.i18n

import java.util.Locale
import java.util.ResourceBundle

/**
 * Supported locales for Optimus Toolshed
 */
enum class SupportedLocale(val locale: Locale, val displayName: String) {
    ENGLISH(Locale.ENGLISH, "English"),
    SPANISH(Locale("es"), "Español"),
    FRENCH(Locale.FRENCH, "Français"),
    GERMAN(Locale.GERMAN, "Deutsch"),
    JAPANESE(Locale.JAPANESE, "日本語")
}

/**
 * Central localization manager for the application
 */
class LocalizationManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: LocalizationManager? = null
        
        fun getInstance(): LocalizationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalizationManager().also { INSTANCE = it }
            }
        }
    }
    
    private var currentLocale: Locale = Locale.getDefault()
    private var resourceBundle: ResourceBundle? = null
    
    /**
     * Initialize the localization manager with a specific locale
     */
    fun initialize(locale: Locale = Locale.getDefault()) {
        currentLocale = locale
        loadResourceBundle()
    }
    
    /**
     * Get localized text for the given key
     */
    fun getText(key: String, vararg args: Any): String {
        return try {
            val text = resourceBundle?.getString(key) ?: key
            if (args.isNotEmpty()) {
                String.format(currentLocale, text, *args)
            } else {
                text
            }
        } catch (e: Exception) {
            key // Return key as fallback
        }
    }
    
    /**
     * Get the current application locale
     */
    fun getCurrentLocale(): Locale = currentLocale
    
    /**
     * Set a new locale and reload resources
     */
    fun setLocale(locale: Locale) {
        if (locale != currentLocale) {
            currentLocale = locale
            loadResourceBundle()
        }
    }
    
    /**
     * Get list of supported locales
     */
    fun getSupportedLocales(): List<SupportedLocale> = SupportedLocale.values().toList()
    
    private fun loadResourceBundle() {
        try {
            resourceBundle = ResourceBundle.getBundle("messages", currentLocale)
        } catch (e: Exception) {
            // Fallback to default bundle or empty
            resourceBundle = null
        }
    }
}

/**
 * Convenience extension function for getting localized text
 */
fun String.localized(vararg args: Any): String {
    return LocalizationManager.getInstance().getText(this, *args)
}