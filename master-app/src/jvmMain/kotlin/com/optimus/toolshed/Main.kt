/*
 * Optimus Toolshed - Master Application
 * 
 * Main desktop application implementing the master/detail interface and
 * orchestrating the complete plugin ecosystem.
 */

package com.optimus.toolshed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.optimus.toolshed.i18n.LocalizationManager
import com.optimus.toolshed.i18n.localized
import com.optimus.toolshed.plugin.*
import com.optimus.toolshed.theme.OptimusTheme
import com.optimus.toolshed.ui.*
import java.util.ServiceLoader

/**
 * Initialize the plugin system by discovering and registering available plugins
 */
fun initializePlugins() {
    val pluginRegistry = PluginRegistry.getInstance()
    val pluginContext = AppPluginContext()
    
    try {
        // Use ServiceLoader to discover plugins
        val serviceLoader = ServiceLoader.load(OptimusPlugin::class.java)
        
        for (plugin in serviceLoader) {
            try {
                plugin.initialize(pluginContext)
                pluginRegistry.registerPlugin(plugin)
                pluginContext.log(LogLevel.INFO, "Registered plugin: ${plugin.metadata.name}")
            } catch (e: Exception) {
                pluginContext.log(LogLevel.ERROR, "Failed to initialize plugin: ${plugin.metadata.name}", e)
            }
        }
        
        pluginContext.log(LogLevel.INFO, "Plugin system initialized with ${pluginRegistry.getAllPlugins().size} plugins")
    } catch (e: Exception) {
        println("❌ Failed to initialize plugin system: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Application entry point
 */
fun main() {
    // Initialize services
    LocalizationManager.getInstance().initialize()
    
    // Initialize plugin system
    initializePlugins()
    
    application {
        val windowState = rememberWindowState(
            width = 1200.dp,
            height = 800.dp
        )
        
        Window(
            onCloseRequest = ::exitApplication,
            title = "app.title".localized(),
            state = windowState
        ) {
            OptimusTheme {
                OptimusToolshedApp()
            }
        }
    }
}

/**
 * Main application composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimusToolshedApp() {
    var selectedPlugin by remember { mutableStateOf<OptimusPlugin?>(null) }
    val pluginRegistry = remember { PluginRegistry.getInstance() }
    val availablePlugins by remember { 
        mutableStateOf(pluginRegistry.getAllPlugins().filter { it.isAvailable() })
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("app.title".localized()) 
                },
                actions = {
                    IconButton(onClick = { /* Settings action */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        OptimusMasterDetailLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            masterContent = {
                MasterPanel(
                    plugins = availablePlugins,
                    selectedPlugin = selectedPlugin,
                    onPluginSelected = { selectedPlugin = it }
                )
            },
            detailContent = {
                DetailPanel(
                    selectedPlugin = selectedPlugin
                )
            }
        )
    }
}

/**
 * Master panel showing available plugins
 */
@Composable
fun MasterPanel(
    plugins: List<OptimusPlugin>,
    selectedPlugin: OptimusPlugin?,
    onPluginSelected: (OptimusPlugin) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "menu.tools".localized(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (plugins.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No plugins available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(plugins) { plugin ->
                    PluginListItem(
                        plugin = plugin,
                        isSelected = plugin == selectedPlugin,
                        onClick = { onPluginSelected(plugin) }
                    )
                }
            }
        }
    }
}

/**
 * Individual plugin list item
 */
@Composable
fun PluginListItem(
    plugin: OptimusPlugin,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = plugin.metadata.name,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = plugin.metadata.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * Detail panel showing selected plugin content
 */
@Composable
fun DetailPanel(
    selectedPlugin: OptimusPlugin?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (selectedPlugin != null) {
            Column {
                // Plugin toolbar (if any)
                selectedPlugin.PluginToolbar()
                
                // Plugin content
                selectedPlugin.PluginContent(
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Welcome screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "app.title".localized(),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "app.description".localized(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Select a tool from the left panel to begin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}