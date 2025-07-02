/*
 * Optimus Toolshed - Server Configuration Plugin
 * 
 * Default plugin implementation providing server configuration management
 * functionality within the Optimus Toolshed ecosystem.
 */

package com.optimus.toolshed.plugins.serverconfig

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.optimus.toolshed.i18n.localized
import com.optimus.toolshed.plugin.*
import com.optimus.toolshed.ui.*

/**
 * Server Configuration Plugin implementation
 */
class ServerConfigurationPlugin : OptimusPlugin {
    
    override val metadata = PluginMetadata(
        id = "server-configuration",
        name = "Server Configuration",
        version = "1.0.0",
        description = "Manage Optimus Server configuration settings",
        author = "Optimus Project",
        category = PluginCategory.SERVER_MANAGEMENT
    )
    
    private lateinit var context: PluginContext
    private var isInitialized = false
    
    override fun initialize(context: PluginContext) {
        this.context = context
        this.isInitialized = true
        context.log(LogLevel.INFO, "Server Configuration Plugin initialized")
    }
    
    override fun isAvailable(): Boolean = isInitialized
    
    @Composable
    override fun PluginContent(modifier: Modifier) {
        ServerConfigurationContent(modifier = modifier)
    }
    
    @Composable
    override fun PluginToolbar(modifier: Modifier) {
        Row(
            modifier = modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OptimusActionButton(
                text = "common.save".localized(),
                onClick = { saveConfiguration() }
            )
            OptimusActionButton(
                text = "Load Default",
                onClick = { loadDefaultConfiguration() },
                isPrimary = false
            )
        }
    }
    
    private fun saveConfiguration() {
        context.log(LogLevel.INFO, "Configuration saved")
    }
    
    private fun loadDefaultConfiguration() {
        context.log(LogLevel.INFO, "Default configuration loaded")
    }
}

/**
 * Main content for the server configuration plugin
 */
@Composable
fun ServerConfigurationContent(modifier: Modifier = Modifier) {
    var serverHost by remember { mutableStateOf("localhost") }
    var serverPort by remember { mutableStateOf("8080") }
    var maxConnections by remember { mutableStateOf("100") }
    var enableLogging by remember { mutableStateOf(true) }
    var logLevel by remember { mutableStateOf("INFO") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Server Configuration",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        
        Divider()
        
        // Server Connection Settings
        ConfigurationSection(
            title = "Connection Settings"
        ) {
            OutlinedTextField(
                value = serverHost,
                onValueChange = { serverHost = it },
                label = { Text("Server Host") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = serverPort,
                onValueChange = { serverPort = it },
                label = { Text("Server Port") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = maxConnections,
                onValueChange = { maxConnections = it },
                label = { Text("Max Connections") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Logging Settings
        ConfigurationSection(
            title = "Logging Settings"
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = enableLogging,
                    onCheckedChange = { enableLogging = it }
                )
                Text("Enable Logging")
            }
            
            if (enableLogging) {
                var expanded by remember { mutableStateOf(false) }
                val logLevels = listOf("DEBUG", "INFO", "WARN", "ERROR")
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = logLevel,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Log Level") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        logLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    logLevel = level
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Status Section
        OptimusStatusCard(
            title = "status.ready".localized(),
            message = "Configuration is ready to be saved",
            icon = Icons.Default.Settings
        )
    }
}

/**
 * Configuration section container
 */
@Composable
fun ConfigurationSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            content()
        }
    }
}