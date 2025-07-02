/*
 * Optimus Toolshed - Plugin API
 * 
 * Core plugin architecture module defining contracts, interfaces, and service
 * provider framework for the plugin system.
 */

package com.optimus.toolshed.plugin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Plugin metadata information
 */
data class PluginMetadata(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val author: String,
    val category: PluginCategory = PluginCategory.TOOL
)

/**
 * Plugin categories for organization in the master interface
 */
enum class PluginCategory(val displayName: String) {
    SERVER_MANAGEMENT("Server Management"),
    CONFIGURATION("Configuration"),
    MONITORING("Monitoring"),
    TOOL("Tools"),
    UTILITY("Utilities")
}

/**
 * Plugin execution context providing access to application services
 */
interface PluginContext {
    /**
     * Get localized text using the application's i18n system
     */
    fun getText(key: String, vararg args: Any): String
    
    /**
     * Log messages to the application log
     */
    fun log(level: LogLevel, message: String, exception: Throwable? = null)
    
    /**
     * Get application configuration values
     */
    fun getConfigValue(key: String): String?
}

/**
 * Logging levels for plugin messages
 */
enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

/**
 * Main plugin interface that all plugins must implement
 */
interface OptimusPlugin {
    /**
     * Plugin metadata
     */
    val metadata: PluginMetadata
    
    /**
     * Initialize the plugin with the given context
     */
    fun initialize(context: PluginContext)
    
    /**
     * Check if the plugin is currently available/enabled
     */
    fun isAvailable(): Boolean = true
    
    /**
     * Get the plugin's UI component for the detail panel
     */
    @Composable
    fun PluginContent(modifier: Modifier = Modifier)
    
    /**
     * Get the plugin's toolbar actions (optional)
     */
    @Composable
    fun PluginToolbar(modifier: Modifier = Modifier) {
        // Default: no toolbar
    }
    
    /**
     * Clean up resources when the plugin is being unloaded
     */
    fun cleanup() {
        // Default: no cleanup needed
    }
}

/**
 * Plugin registry for managing loaded plugins
 */
class PluginRegistry private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: PluginRegistry? = null
        
        fun getInstance(): PluginRegistry {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PluginRegistry().also { INSTANCE = it }
            }
        }
    }
    
    private val plugins = mutableMapOf<String, OptimusPlugin>()
    
    /**
     * Register a plugin
     */
    fun registerPlugin(plugin: OptimusPlugin) {
        plugins[plugin.metadata.id] = plugin
    }
    
    /**
     * Get all registered plugins
     */
    fun getAllPlugins(): List<OptimusPlugin> = plugins.values.toList()
    
    /**
     * Get plugins by category
     */
    fun getPluginsByCategory(category: PluginCategory): List<OptimusPlugin> {
        return plugins.values.filter { it.metadata.category == category }
    }
    
    /**
     * Get a specific plugin by ID
     */
    fun getPlugin(id: String): OptimusPlugin? = plugins[id]
    
    /**
     * Unregister a plugin
     */
    fun unregisterPlugin(id: String) {
        plugins[id]?.cleanup()
        plugins.remove(id)
    }
}