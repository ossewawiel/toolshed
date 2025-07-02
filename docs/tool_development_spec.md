# Optimus Toolshed - Tool Development Standards and Requirements

## Introduction

The Optimus Toolshed tool development framework establishes a standardized approach for creating management utilities that integrate seamlessly within the master application environment. Each tool operates as an independent application packaged as a JAR file, implementing a well-defined contract that ensures both functional integration and visual consistency across the entire administrative ecosystem. This plugin-based architecture enables tool developers to focus exclusively on business logic and specialized administrative workflows while relying on the master application to provide navigation, UI framework, and common services that create a unified user experience.

Tool development follows the principle of constrained creativity, where developers work within established boundaries that guarantee consistency while providing sufficient flexibility to address diverse server management requirements. Tools receive a standardized detail container from the master application and must utilize only the shared component library for all user interface elements, ensuring that administrators encounter familiar interaction patterns regardless of which specific management function they are accessing. This approach transforms what could be a collection of disparate utilities into a cohesive administrative suite that feels like a single, well-designed application.

The technical architecture supports both rapid tool development and long-term maintainability through clear separation of concerns and robust lifecycle management. Tools declare their capabilities, dependencies, and integration requirements through standardized manifests, enabling the master application to perform compatibility validation and provide appropriate runtime environments. This design ensures that new administrative capabilities can be developed and deployed independently while maintaining system stability and user experience consistency. The framework accommodates tools of varying complexity, from simple configuration editors to sophisticated monitoring dashboards, all operating within the same architectural foundation.

## Tool Architecture Requirements

### Plugin Structure and Packaging
```
tool-server-configuration/
├── src/main/kotlin/
│   ├── com/optimus/tools/serverconfig/
│   │   ├── ServerConfigurationTool.kt
│   │   ├── ui/
│   │   │   ├── ServerConfigurationContent.kt
│   │   │   ├── NetworkSettingsPanel.kt
│   │   │   └── SecuritySettingsPanel.kt
│   │   ├── services/
│   │   │   ├── ConfigurationService.kt
│   │   │   └── ValidationService.kt
│   │   ├── models/
│   │   │   ├── ServerConfiguration.kt
│   │   │   └── ValidationResult.kt
│   │   └── resources/
│   │       ├── icons/
│   │       ├── templates/
│   │       └── defaults/
├── src/main/resources/
│   ├── META-INF/
│   │   ├── MANIFEST.MF
│   │   └── services/
│   │       └── com.optimus.toolshed.OptimusToolPlugin
│   └── plugin.properties
└── build.gradle.kts
```

### Build Configuration Standards
```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    `maven-publish`
}

dependencies {
    // Required: Shared component library
    implementation("com.optimus.toolshed:ui-components:${Versions.sharedLibrary}")
    
    // Required: Plugin API
    implementation("com.optimus.toolshed:plugin-api:${Versions.pluginApi}")
    
    // Optional: Tool-specific dependencies
    implementation("org.apache.commons:commons-lang3:3.12.0")
    
    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.compose.ui:ui-test-junit4")
}

kotlin {
    jvmToolchain(17)
}

jar {
    manifest {
        attributes(
            "Plugin-Class" to "com.optimus.tools.serverconfig.ServerConfigurationTool",
            "Plugin-Version" to project.version,
            "Required-Shared-Library" to "${Versions.sharedLibraryMin}-${Versions.sharedLibraryMax}",
            "Plugin-Dependencies" to listOf("commons-lang3:3.12.0").joinToString(",")
        )
    }
}
```

## Plugin Implementation Contract

### Core Plugin Interface
```kotlin
// Required implementation for all tools
interface OptimusToolPlugin {
    // Plugin metadata and identification
    val pluginInfo: PluginInfo
    val menuContribution: MenuContribution
    
    // Main entry point for tool functionality
    @Composable
    fun renderTool(container: ToolDetailContainer, context: ToolContext)
    
    // Lifecycle management hooks
    fun onInitialize(context: ToolContext): InitializationResult
    fun onActivated(context: ToolContext)
    fun onDeactivated()
    fun onCleanup()
    
    // Resource and dependency management
    fun validateDependencies(): List<DependencyIssue>
    fun getRequiredPermissions(): List<Permission>
}

data class PluginInfo(
    val id: String,
    val name: String,
    val description: String,
    val version: Version,
    val author: String,
    val requiredSharedLibraryVersion: VersionRange,
    val minimumJvmVersion: Int = 17,
    val supportedPlatforms: List<Platform> = listOf(Platform.Windows)
)
```

### Menu Integration Requirements
```kotlin
// Tool must declare its menu placement and metadata
data class MenuContribution(
    val parentPath: String,              // e.g., "Configuration/Server"
    val menuItem: MenuItem,
    val sortOrder: Int = 100,
    val requiresPermissions: List<Permission> = emptyList(),
    val enabledCondition: (() -> Boolean)? = null
)

data class MenuItem(
    val label: String,
    val icon: String,                    // Icon identifier from shared library
    val description: String,
    val keywords: List<String> = emptyList(),  // For search functionality
    val shortcut: KeyboardShortcut? = null
)

// Example implementation
override val menuContribution = MenuContribution(
    parentPath = "Configuration",
    menuItem = MenuItem(
        label = "Server Settings",
        icon = "settings",
        description = "Configure core Optimus Server parameters and networking",
        keywords = listOf("server", "config", "network", "ssl", "port")
    ),
    sortOrder = 10,
    requiresPermissions = listOf(Permission.CONFIGURATION_WRITE)
)
```

## UI Development Standards

### Container Usage Requirements
```kotlin
// Tools must work within the provided container structure
@Composable
override fun renderTool(container: ToolDetailContainer, context: ToolContext) {
    // Required: Set up header information
    LaunchedEffect(Unit) {
        container.setHeader(
            title = "Server Configuration",
            description = "Manage core server settings and network parameters",
            breadcrumbs = listOf("Configuration", "Server Settings"),
            helpUrl = "https://docs.optimus.com/server-config"
        )
        
        // Required: Define toolbar actions
        container.setToolbar(listOf(
            ToolbarAction.Primary(
                label = "Save Changes",
                icon = "save",
                action = ::saveConfiguration,
                enabled = hasUnsavedChanges
            ),
            ToolbarAction.Secondary(
                label = "Reset to Defaults",
                icon = "refresh",
                action = ::resetToDefaults
            ),
            ToolbarAction.Secondary(
                label = "Export Configuration",
                icon = "download",
                action = ::exportConfiguration
            )
        ))
        
        // Optional: Set status information
        container.setStatus(
            message = "Configuration loaded successfully",
            type = StatusType.Success
        )
    }
    
    // Required: Provide main content using shared components only
    container.setContent {
        ServerConfigurationContent()
    }
}
```

### Shared Component Usage
```kotlin
// Tools must use only shared components for UI
@Composable
private fun ServerConfigurationContent() {
    OptimusScrollableColumn {
        // Network configuration section
        OptimusCard(
            title = "Network Settings",
            modifier = Modifier.fillMaxWidth()
        ) {
            OptimusFormSection {
                OptimusTextField(
                    value = serverHost,
                    onValueChange = { serverHost = it },
                    label = "Host Address",
                    placeholder = "localhost",
                    validator = ::validateHostAddress,
                    helpText = "IP address or hostname for server binding"
                )
                
                OptimusNumberField(
                    value = serverPort,
                    onValueChange = { serverPort = it },
                    label = "Port",
                    range = 1024..65535,
                    validator = ::validatePort
                )
                
                OptimusCheckbox(
                    checked = enableSSL,
                    onCheckedChange = { enableSSL = it },
                    label = "Enable SSL/TLS",
                    description = "Encrypt communications using TLS certificates"
                )
            }
        }
        
        // Security configuration section
        OptimusCard(
            title = "Security Settings",
            modifier = Modifier.fillMaxWidth()
        ) {
            OptimusFormSection {
                OptimusDropdown(
                    selectedValue = authenticationMode,
                    onValueSelected = { authenticationMode = it },
                    options = AuthenticationMode.values().toList(),
                    label = "Authentication Mode",
                    displayTransform = { it.displayName }
                )
                
                OptimusPasswordField(
                    value = adminPassword,
                    onValueChange = { adminPassword = it },
                    label = "Administrator Password",
                    validator = ::validatePassword,
                    strengthIndicator = true
                )
            }
        }
    }
}
```

## Data Management and Persistence

### Configuration Storage Standards
```kotlin
// Tools must use provided data services for persistence
interface ToolDataService {
    suspend fun saveConfiguration(toolId: String, key: String, data: Any): Result<Unit>
    suspend fun loadConfiguration(toolId: String, key: String): Result<Any?>
    suspend fun deleteConfiguration(toolId: String, key: String): Result<Unit>
    suspend fun listConfigurations(toolId: String): Result<List<String>>
    
    // Backup and restore capabilities
    suspend fun exportConfiguration(toolId: String): Result<ByteArray>
    suspend fun importConfiguration(toolId: String, data: ByteArray): Result<Unit>
}

// Example usage in tool
class ServerConfigurationTool : OptimusToolPlugin {
    private lateinit var dataService: ToolDataService
    
    override fun onInitialize(context: ToolContext): InitializationResult {
        dataService = context.getService(ToolDataService::class)
        return InitializationResult.Success
    }
    
    private suspend fun saveConfiguration() {
        val config = ServerConfiguration(
            host = serverHost,
            port = serverPort,
            sslEnabled = enableSSL,
            authMode = authenticationMode
        )
        
        dataService.saveConfiguration(
            toolId = pluginInfo.id,
            key = "server-config",
            data = config
        ).onFailure { error ->
            showError("Failed to save configuration: ${error.message}")
        }
    }
}
```

### State Management Requirements
```kotlin
// Tools must follow Compose state management best practices
@Composable
private fun ServerConfigurationContent() {
    // Use remember for expensive computations
    val validationResults by remember(serverHost, serverPort) {
        derivedStateOf {
            validateConfiguration(serverHost, serverPort)
        }
    }
    
    // Use LaunchedEffect for side effects
    LaunchedEffect(Unit) {
        loadConfiguration()
    }
    
    // Use mutableStateOf for UI state
    var isLoading by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    
    // State hoisting for complex components
    ServerConfigForm(
        configuration = currentConfiguration,
        onConfigurationChange = { newConfig ->
            currentConfiguration = newConfig
            hasUnsavedChanges = true
        },
        validationResults = validationResults,
        isLoading = isLoading
    )
}
```

## Error Handling and Validation

### Standardized Error Management
```kotlin
// Tools must use consistent error handling patterns
sealed class ToolError {
    data class ValidationError(
        val field: String,
        val message: String,
        val severity: Severity = Severity.Error
    ) : ToolError()
    
    data class OperationError(
        val operation: String,
        val cause: Throwable,
        val recoverable: Boolean = true
    ) : ToolError()
    
    data class DependencyError(
        val dependency: String,
        val reason: String
    ) : ToolError()
}

interface ErrorReporter {
    fun reportError(error: ToolError)
    fun reportWarning(message: String)
    fun reportInfo(message: String)
    fun clearErrors()
}

// Example error handling in tool
private suspend fun saveConfiguration() {
    try {
        validateConfiguration()?.let { validationError ->
            errorReporter.reportError(validationError)
            return
        }
        
        isLoading = true
        dataService.saveConfiguration(pluginInfo.id, "config", currentConfig)
            .onSuccess {
                errorReporter.reportInfo("Configuration saved successfully")
                hasUnsavedChanges = false
            }
            .onFailure { error ->
                errorReporter.reportError(
                    ToolError.OperationError(
                        operation = "Save Configuration",
                        cause = error,
                        recoverable = true
                    )
                )
            }
    } finally {
        isLoading = false
    }
}
```

### Input Validation Standards
```kotlin
// Consistent validation patterns across all tools
interface FieldValidator<T> {
    fun validate(value: T): ValidationResult
}

data class ValidationResult(
    val isValid: Boolean,
    val message: String? = null,
    val severity: Severity = Severity.Error
)

// Common validators provided by shared library
object CommonValidators {
    val hostAddress: FieldValidator<String> = HostAddressValidator()
    val portNumber: FieldValidator<Int> = PortNumberValidator()
    val password: FieldValidator<String> = PasswordValidator()
    val email: FieldValidator<String> = EmailValidator()
    val filePath: FieldValidator<String> = FilePathValidator()
}

// Example usage in component
OptimusTextField(
    value = serverHost,
    onValueChange = { serverHost = it },
    label = "Host Address",
    validator = CommonValidators.hostAddress,
    errorDisplay = ValidationErrorDisplay.Inline
)
```

## External Dependencies and Resources

### Dependency Declaration
```kotlin
// Tools must declare all external dependencies
data class ExternalDependency(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val scope: DependencyScope = DependencyScope.Runtime,
    val optional: Boolean = false,
    val description: String
)

enum class DependencyScope {
    Runtime, Provided, Test
}

// In plugin implementation
override fun validateDependencies(): List<DependencyIssue> {
    val required = listOf(
        ExternalDependency(
            groupId = "org.apache.commons",
            artifactId = "commons-lang3",
            version = "3.12.0",
            description = "String utilities for configuration processing"
        )
    )
    
    return DependencyValidator.checkDependencies(required)
}
```

### Resource Management
```kotlin
// Tools must package resources within JAR and access via standard methods
class ResourceManager(private val pluginClass: Class<*>) {
    fun getIcon(name: String): ByteArray? {
        return pluginClass.getResourceAsStream("/icons/$name.png")?.readBytes()
    }
    
    fun getTemplate(name: String): String? {
        return pluginClass.getResourceAsStream("/templates/$name.xml")?.bufferedReader()?.readText()
    }
    
    fun getDefaultConfiguration(): String? {
        return pluginClass.getResourceAsStream("/defaults/config.json")?.bufferedReader()?.readText()
    }
}

// Example usage
class ServerConfigurationTool : OptimusToolPlugin {
    private val resources = ResourceManager(this::class.java)
    
    private suspend fun loadDefaults() {
        resources.getDefaultConfiguration()?.let { defaultJson ->
            val defaultConfig = Json.decodeFromString<ServerConfiguration>(defaultJson)
            applyConfiguration(defaultConfig)
        }
    }
}
```

## Testing Requirements

### Unit Testing Standards
```kotlin
// All tools must include comprehensive unit tests
class ServerConfigurationToolTest {
    private lateinit var tool: ServerConfigurationTool
    private lateinit var mockContext: ToolContext
    private lateinit var mockContainer: ToolDetailContainer
    
    @BeforeEach
    fun setup() {
        tool = ServerConfigurationTool()
        mockContext = mockk<ToolContext>()
        mockContainer = mockk<ToolDetailContainer>()
    }
    
    @Test
    fun `should initialize successfully with valid context`() {
        // Given
        every { mockContext.getService(ToolDataService::class) } returns mockk<ToolDataService>()
        
        // When
        val result = tool.onInitialize(mockContext)
        
        // Then
        assertTrue(result is InitializationResult.Success)
    }
    
    @Test
    fun `should validate host address correctly`() {
        // Given
        val validator = CommonValidators.hostAddress
        
        // When & Then
        assertTrue(validator.validate("192.168.1.1").isValid)
        assertTrue(validator.validate("localhost").isValid)
        assertFalse(validator.validate("").isValid)
        assertFalse(validator.validate("256.256.256.256").isValid)
    }
}
```

### Integration Testing Standards
```kotlin
// Tools must include integration tests with master application
@Composable
class ServerConfigurationIntegrationTest {
    @Test
    fun `should render correctly in tool container`() = runComposeUiTest {
        // Given
        val tool = ServerConfigurationTool()
        val container = TestToolDetailContainer()
        val context = TestToolContext()
        
        // When
        setContent {
            tool.renderTool(container, context)
        }
        
        // Then
        onNodeWithText("Server Configuration").assertIsDisplayed()
        onNodeWithText("Host Address").assertIsDisplayed()
        onNode(hasTestTag("save-button")).assertIsDisplayed()
    }
}
```

## Performance and Resource Management

### Resource Constraints
- Maximum JAR file size: 50MB (excluding shared dependencies)
- Maximum memory usage during normal operation: 256MB heap
- UI responsiveness: All operations must complete within 5 seconds or show progress
- Startup time: Tool activation must complete within 2 seconds

### Performance Guidelines
```kotlin
// Tools must follow performance best practices
class PerformantToolImplementation : OptimusToolPlugin {
    
    @Composable
    override fun renderTool(container: ToolDetailContainer, context: ToolContext) {
        // Use remember for expensive operations
        val expensiveData by remember {
            mutableStateOf(computeExpensiveData())
        }
        
        // Use LaunchedEffect for async operations
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                loadConfigurationAsync()
            }
        }
        
        // Use derivedStateOf for computed values
        val isValid by remember(configurationData) {
            derivedStateOf { validateConfiguration(configurationData) }
        }
        
        // Implement proper cleanup
        DisposableEffect(Unit) {
            onDispose {
                cleanupResources()
            }
        }
    }
    
    private fun cleanupResources() {
        // Release any held resources
        backgroundTasks.cancel()
        fileWatchers.close()
        networkConnections.close()
    }
}
```

## Documentation and Deployment Standards

### Required Documentation
Each tool must provide:
1. **README.md** - Installation and basic usage
2. **API Documentation** - KDoc for all public interfaces
3. **User Guide** - End-user functionality documentation
4. **Configuration Reference** - All configurable parameters
5. **Troubleshooting Guide** - Common issues and solutions

### Deployment Package Requirements
```
tool-server-configuration-2.1.0.jar
├── META-INF/
│   ├── MANIFEST.MF (with plugin metadata)
│   └── LICENSE.txt
├── com/optimus/tools/serverconfig/ (compiled classes)
├── icons/ (tool-specific icons)
├── templates/ (configuration templates)
├── defaults/ (default configurations)
└── documentation/
    ├── README.md
    ├── user-guide.pdf
    └── api-docs/
```

---

*Document Version: 1.0*  
*Last Updated: July 01, 2025*  
*Purpose: Technical standards and requirements for Optimus Toolshed tool development*