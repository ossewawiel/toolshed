/*
 * Optimus Toolshed - Application Plugin Context
 * 
 * Implementation of PluginContext providing access to application services
 * for plugins running within the Optimus Toolshed environment.
 */

package com.optimus.toolshed

import com.optimus.toolshed.plugin.PluginContext
import com.optimus.toolshed.plugin.LogLevel
import com.optimus.toolshed.i18n.LocalizationManager
import java.util.Properties
import java.io.FileInputStream

/**
 * Application implementation of PluginContext
 */
class AppPluginContext : PluginContext {
    
    private val config = Properties().apply {
        try {
            // Try to load application configuration
            load(javaClass.getResourceAsStream("/application.properties") ?: "".byteInputStream())
        } catch (e: Exception) {
            // Default configuration if file not found
            setProperty("app.version", "1.0.0")
            setProperty("app.debug", "false")
        }
    }
    
    override fun getText(key: String, vararg args: Any): String {
        return LocalizationManager.getInstance().getText(key, *args)
    }
    
    override fun log(level: LogLevel, message: String, exception: Throwable?) {
        val timestamp = java.time.LocalDateTime.now()
        val logMessage = "[$timestamp] [${level.name}] $message"
        
        when (level) {
            LogLevel.DEBUG -> if (config.getProperty("app.debug", "false").toBoolean()) {
                println("🐛 $logMessage")
            }
            LogLevel.INFO -> println("ℹ️ $logMessage")
            LogLevel.WARN -> println("⚠️ $logMessage")
            LogLevel.ERROR -> {
                println("❌ $logMessage")
                exception?.printStackTrace()
            }
        }
    }
    
    override fun getConfigValue(key: String): String? {
        return config.getProperty(key)
    }
}