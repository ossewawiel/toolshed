# Optimus Toolshed - Plugin Architecture

## Plugin Architecture Overview

The Optimus Toolshed plugin architecture establishes a robust framework for extending administrative functionality through dynamically loadable modules that integrate seamlessly with the master application environment. Each plugin operates as an independent JAR-based component implementing standardized contracts that ensure both functional compatibility and visual consistency across the entire ecosystem. This architecture transforms the traditional approach of separate, disconnected administrative utilities into a cohesive platform where third-party developers and internal teams can contribute specialized tools without compromising system stability or user experience coherence.

The plugin system operates through a discovery and lifecycle management framework that handles runtime loading, dependency resolution, and version compatibility validation automatically. Plugins declare their capabilities, requirements, and integration metadata through standardized manifests, enabling the master application to perform compatibility checks and provide appropriate runtime environments before activation. This design ensures that administrative tools can be developed, tested, and deployed independently while maintaining strict adherence to the shared component library and interaction patterns that create the unified experience administrators expect from professional desktop software.

The technical implementation leverages Kotlin's reflection capabilities and Java's service provider interface mechanisms to achieve dynamic plugin discovery and instantiation without requiring compile-time dependencies between the core application and individual tools. Plugin isolation is maintained through careful classloader management and dependency injection patterns that prevent conflicts between tools while enabling shared access to common services and UI frameworks. This architecture supports both rapid prototyping of new administrative capabilities and enterprise-grade deployment scenarios where tool availability, security, and compatibility must be precisely controlled across diverse server management environments.

## Plugin Discovery and Loading

### Runtime Discovery Mechanism
```kotlin
interface PluginDiscovery {
    suspend fun discoverPlugins(pluginDirectory: Path): List<PluginCandidate>
    suspend fun validatePlugin(candidate: PluginCandidate): ValidationResult
    suspend fun loadPlugin(candidate: PluginCandidate): OptimusToolPlugin
    fun unloadPlugin(pluginId: String): UnloadResult
}

class JarBasedPluginDiscovery : PluginDiscovery {
    private val loadedPlugins = mutableMapOf<String, PluginContainer>()
    private val pluginClassLoaders = mutableMapOf<String, URLClassLoader>()
    
    override suspend fun discoverPlugins(pluginDirectory: Path): List<PluginCandidate> {
        return withContext(Dispatchers.IO) {
            Files.walk(pluginDirectory)
                .filter { it.extension == "jar" }
                .mapNotNull { jarFile ->
                    try {
                        val manifest = extractPluginManifest(jarFile)
                        PluginCandidate(
                            jarFile = jarFile,
                            manifest = manifest,
                            mainClass = manifest.pluginClass
                        )
                    } catch (e: Exception) {
                        logger.warn("Failed to read plugin manifest from $jarFile", e)
                        null
                    }
                }
                .toList()
        }
    }
}
```

### Plugin Manifest Structure
```kotlin
data class PluginManifest(
    val pluginId: String,
    val pluginClass: String,
    val version: Version,
    val requiredCoreVersion: VersionRange,
    val requiredSharedLibraryVersion: VersionRange,
    val name: String,
    val description: String,
    val author: String,
    val website: String? = null,
    val dependencies: List<ExternalDependency> = emptyList(),
    val permissions: List<Permission> = emptyList(),
    val supportedPlatforms: List<Platform> = listOf(Platform.Windows),
    val minimumJvmVersion: Int = 17
)

// Manifest extraction from JAR
private fun extractPluginManifest(jarFile: Path): PluginManifest {
    JarFile(jarFile.toFile()).use { jar ->
        val manifestEntry = jar.getEntry("META-INF/plugin.json")
            ?: throw PluginException("Missing plugin.json in META-INF")
        
        return jar.getInputStream(manifestEntry).use { stream ->
            Json.decodeFromString<PluginManifest>(stream.readBytes().decodeToString())
        }
    }
}
```

### Classloader Management and Isolation
```kotlin
class PluginClassLoaderManager {
    private val isolatedClassLoaders = mutableMapOf<String, PluginClassLoader>()
    
    fun createIsolatedClassLoader(
        pluginId: String,
        jarFile: Path,
        dependencies: List<Path>
    ): PluginClassLoader {
        val urls = (listOf(jarFile) + dependencies).map { it.toUri().toURL() }.toTypedArray()
        
        return PluginClassLoader(
            urls = urls,
            parent = getSharedLibraryClassLoader(),
            pluginId = pluginId
        ).also { classLoader ->
            isolatedClassLoaders[pluginId] = classLoader
        }
    }
    
    private fun getSharedLibraryClassLoader(): ClassLoader {
        // Return classloader containing shared UI components and API
        return SharedLibraryClassLoader.instance
    }
}

class PluginClassLoader(
    urls: Array<URL>,
    parent: ClassLoader,
    private val pluginId: String
) : URLClassLoader(urls, parent) {
    
    override fun loadClass(name: String): Class<*> {
        // Delegate shared library classes to parent
        if (name.startsWith("com.optimus.toolshed.ui") || 
            name.startsWith("com.optimus.toolshed.api")) {
            return parent.loadClass(name)
        }
        
        // Load plugin-specific classes in isolation
        return super.loadClass(name)
    }
}
```

## Plugin Lifecycle Management

### Plugin Container and State Management
```kotlin
sealed class PluginState {
    object Discovered : PluginState()
    object Validating : PluginState()
    object Validated : PluginState()
    object Loading : PluginState()
    object Loaded : PluginState()
    object Activating : PluginState()
    object Active : PluginState()
    object Deactivating : PluginState()
    object Unloading : PluginState()
    object Unloaded : PluginState()
    data class Error(val exception: Throwable) : PluginState()
}

class PluginContainer(
    val manifest: PluginManifest,
    val classLoader: PluginClassLoader,
    private var plugin: OptimusToolPlugin? = null
) {
    private var _state = MutableStateFlow<PluginState>(PluginState.Discovered)
    val state: StateFlow<PluginState> = _state.asStateFlow()
    
    suspend fun load(): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            _state.value = PluginState.Loading
            
            val pluginClass = classLoader.loadClass(manifest.pluginClass)
            val constructor = pluginClass.getDeclaredConstructor()
            plugin = constructor.newInstance() as OptimusToolPlugin
            
            _state.value = PluginState.Loaded
            Result.success(Unit)
        } catch (e: Exception) {
            _state.value = PluginState.Error(e)
            Result.failure(e)
        }
    }
    
    suspend fun activate(context: ToolContext): Result<Unit> {
        return try {
            _state.value = PluginState.Activating
            
            val initResult = plugin?.onInitialize(context)
            if (initResult is InitializationResult.Success) {
                plugin?.onActivated(context)
                _state.value = PluginState.Active
                Result.success(Unit)
            } else {
                val error = (initResult as InitializationResult.Failure).exception
                _state.value = PluginState.Error(error)
                Result.failure(error)
            }
        } catch (e: Exception) {
            _state.value = PluginState.Error(e)
            Result.failure(e)
        }
    }
}
```

### Plugin Manager Orchestration
```kotlin
class PluginManager(
    private val discovery: PluginDiscovery,
    private val classLoaderManager: PluginClassLoaderManager,
    private val contextProvider: ToolContextProvider
) {
    private val _plugins = MutableStateFlow<Map<String, PluginContainer>>(emptyMap())
    val plugins: StateFlow<Map<String, PluginContainer>> = _plugins.asStateFlow()
    
    private val _activePlugin = MutableStateFlow<String?>(null)
    val activePlugin: StateFlow<String?> = _activePlugin.asStateFlow()
    
    suspend fun discoverAndLoadPlugins(pluginDirectory: Path): Result<List<String>> {
        return try {
            val candidates = discovery.discoverPlugins(pluginDirectory)
            val loadedPluginIds = mutableListOf<String>()
            
            for (candidate in candidates) {
                val validationResult = discovery.validatePlugin(candidate)
                if (validationResult.isValid) {
                    val container = createPluginContainer(candidate)
                    container.load().onSuccess {
                        _plugins.value = _plugins.value + (candidate.manifest.pluginId to container)
                        loadedPluginIds.add(candidate.manifest.pluginId)
                    }
                } else {
                    logger.warn("Plugin validation failed: ${validationResult.message}")
                }
            }
            
            Result.success(loadedPluginIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun activatePlugin(pluginId: String): Result<Unit> {
        val container = plugins.value[pluginId]
            ?: return Result.failure(PluginException("Plugin not found: $pluginId"))
        
        // Deactivate current plugin if any
        _activePlugin.value?.let { currentId ->
            deactivatePlugin(currentId)
        }
        
        return container.activate(contextProvider.createContext()).onSuccess {
            _activePlugin.value = pluginId
        }
    }
}
```

## Plugin Communication and Services

### Dependency Injection Framework
```kotlin
interface ServiceProvider {
    fun <T : Any> getService(serviceType: KClass<T>): T?
    fun <T : Any> registerService(serviceType: KClass<T>, implementation: T)
    fun <T : Any> registerFactory(serviceType: KClass<T>, factory: () -> T)
}

class PluginServiceProvider : ServiceProvider {
    private val services = mutableMapOf<KClass<*>, Any>()
    private val factories = mutableMapOf<KClass<*>, () -> Any>()
    
    override fun <T : Any> getService(serviceType: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return services[serviceType] as? T ?: run {
            factories[serviceType]?.let { factory ->
                val instance = factory() as T
                services[serviceType] = instance
                instance
            }
        }
    }
    
    // Core services registration
    init {
        registerService(ToolDataService::class, DefaultToolDataService())
        registerService(NotificationService::class, DefaultNotificationService())
        registerService(ConfigurationService::class, DefaultConfigurationService())
        registerService(LoggingService::class, DefaultLoggingService())
        registerService(SecurityService::class, DefaultSecurityService())
    }
}

// Service access for plugins
interface ToolContext {
    fun <T : Any> getService(serviceType: KClass<T>): T
    fun getCurrentUser(): UserContext
    fun getApplicationInfo(): ApplicationInfo
    fun createDetailContainer(): ToolDetailContainer
}

inline fun <reified T : Any> ToolContext.getService(): T = getService(T::class)
```

### Inter-Plugin Communication
```kotlin
interface EventBus {
    fun <T : Any> publish(event: T)
    fun <T : Any> subscribe(eventType: KClass<T>, handler: (T) -> Unit): Subscription
    fun <T : Any> subscribeAsync(eventType: KClass<T>, handler: suspend (T) -> Unit): Subscription
}

// Plugin events
sealed class PluginEvent {
    data class PluginActivated(val pluginId: String) : PluginEvent()
    data class PluginDeactivated(val pluginId: String) : PluginEvent()
    data class ConfigurationChanged(val pluginId: String, val key: String) : PluginEvent()
    data class DataUpdated(val source: String, val dataType: String, val data: Any) : PluginEvent()
}

// Usage in plugins
class ServerMonitoringTool : OptimusToolPlugin {
    private lateinit var eventBus: EventBus
    
    override fun onInitialize(context: ToolContext): InitializationResult {
        eventBus = context.getService<EventBus>()
        
        // Subscribe to configuration changes
        eventBus.subscribe<PluginEvent.ConfigurationChanged> { event ->
            if (event.key.startsWith("server.")) {
                refreshServerList()
            }
        }
        
        return InitializationResult.Success
    }
    
    private fun publishServerStatus(serverId: String, status: ServerStatus) {
        eventBus.publish(PluginEvent.DataUpdated(
            source = pluginInfo.id,
            dataType = "server-status",
            data = ServerStatusData(serverId, status)
        ))
    }
}
```

## Version Compatibility and Dependency Management

### Version Range Validation
```kotlin
data class VersionRange(
    val minimum: Version,
    val maximum: Version,
    val exclusions: List<Version> = emptyList()
) {
    fun contains(version: Version): Boolean {
        return version >= minimum && 
               version <= maximum && 
               version !in exclusions
    }
    
    fun isCompatibleWith(other: VersionRange): Boolean {
        return maximum >= other.minimum && minimum <= other.maximum
    }
}

class CompatibilityValidator {
    fun validatePluginCompatibility(
        manifest: PluginManifest,
        coreVersion: Version,
        sharedLibraryVersion: Version
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Validate core version compatibility
        if (!manifest.requiredCoreVersion.contains(coreVersion)) {
            errors.add(
                "Plugin requires core version ${manifest.requiredCoreVersion}, " +
                "but current version is $coreVersion"
            )
        }
        
        // Validate shared library compatibility
        if (!manifest.requiredSharedLibraryVersion.contains(sharedLibraryVersion)) {
            errors.add(
                "Plugin requires shared library version ${manifest.requiredSharedLibraryVersion}, " +
                "but current version is $sharedLibraryVersion"
            )
        }
        
        // Validate JVM version
        val currentJvmVersion = System.getProperty("java.version").substringBefore('.').toInt()
        if (currentJvmVersion < manifest.minimumJvmVersion) {
            errors.add(
                "Plugin requires JVM version ${manifest.minimumJvmVersion}, " +
                "but current version is $currentJvmVersion"
            )
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(errors)
        }
    }
}
```

### Dependency Resolution
```kotlin
data class ExternalDependency(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val scope: DependencyScope = DependencyScope.Runtime,
    val optional: Boolean = false
)

enum class DependencyScope {
    Runtime, Provided, Test
}

class DependencyResolver {
    private val dependencyCache = mutableMapOf<String, Path>()
    
    suspend fun resolveDependencies(
        dependencies: List<ExternalDependency>
    ): Result<List<Path>> = withContext(Dispatchers.IO) {
        try {
            val resolvedPaths = dependencies.mapNotNull { dependency ->
                val cacheKey = "${dependency.groupId}:${dependency.artifactId}:${dependency.version}"
                dependencyCache[cacheKey] ?: run {
                    downloadDependency(dependency)?.also { path ->
                        dependencyCache[cacheKey] = path
                    }
                }
            }
            Result.success(resolvedPaths)
        } catch (e: Exception) {
            Result.failure(DependencyResolutionException("Failed to resolve dependencies", e))
        }
    }
    
    private suspend fun downloadDependency(dependency: ExternalDependency): Path? {
        // Implementation would download from Maven Central or configured repositories
        // For now, check if already available in local cache
        val localPath = getDependencyPath(dependency)
        return if (Files.exists(localPath)) localPath else null
    }
}
```

## Security and Sandboxing

### Permission System
```kotlin
enum class Permission {
    // File system access
    FILE_READ,
    FILE_WRITE,
    FILE_EXECUTE,
    
    // Network access
    NETWORK_CLIENT,
    NETWORK_SERVER,
    
    // System access
    SYSTEM_PROPERTIES,
    ENVIRONMENT_VARIABLES,
    
    // Application access
    CONFIGURATION_READ,
    CONFIGURATION_WRITE,
    USER_DATA_ACCESS,
    
    // Inter-plugin communication
    PLUGIN_COMMUNICATION,
    EVENT_PUBLISHING
}

class SecurityManager {
    private val pluginPermissions = mutableMapOf<String, Set<Permission>>()
    
    fun checkPermission(pluginId: String, permission: Permission): Boolean {
        val grantedPermissions = pluginPermissions[pluginId] ?: emptySet()
        return permission in grantedPermissions
    }
    
    fun grantPermissions(pluginId: String, permissions: Set<Permission>) {
        pluginPermissions[pluginId] = permissions
    }
    
    fun enforcePermission(pluginId: String, permission: Permission) {
        if (!checkPermission(pluginId, permission)) {
            throw SecurityException(
                "Plugin $pluginId does not have permission $permission"
            )
        }
    }
}

// Security-aware service implementations
class SecureDataService(
    private val securityManager: SecurityManager,
    private val delegate: ToolDataService
) : ToolDataService {
    
    override suspend fun saveConfiguration(
        toolId: String, 
        key: String, 
        data: Any
    ): Result<Unit> {
        securityManager.enforcePermission(toolId, Permission.CONFIGURATION_WRITE)
        return delegate.saveConfiguration(toolId, key, data)
    }
    
    override suspend fun loadConfiguration(
        toolId: String, 
        key: String
    ): Result<Any?> {
        securityManager.enforcePermission(toolId, Permission.CONFIGURATION_READ)
        return delegate.loadConfiguration(toolId, key)
    }
}
```

### Resource Monitoring and Limits
```kotlin
class ResourceMonitor {
    private val pluginResourceUsage = mutableMapOf<String, ResourceUsage>()
    private val resourceLimits = ResourceLimits(
        maxMemoryMB = 256,
        maxCpuPercent = 10.0,
        maxFileHandles = 100,
        maxNetworkConnections = 50
    )
    
    fun monitorPlugin(pluginId: String) {
        // Start monitoring thread for plugin resource usage
        GlobalScope.launch {
            while (isPluginActive(pluginId)) {
                val usage = collectResourceUsage(pluginId)
                pluginResourceUsage[pluginId] = usage
                
                if (usage.exceedsLimits(resourceLimits)) {
                    handleResourceViolation(pluginId, usage)
                }
                
                delay(5000) // Check every 5 seconds
            }
        }
    }
    
    private fun handleResourceViolation(pluginId: String, usage: ResourceUsage) {
        logger.warn("Plugin $pluginId exceeds resource limits: $usage")
        // Could trigger warning, throttling, or plugin suspension
        eventBus.publish(PluginEvent.ResourceViolation(pluginId, usage))
    }
}
```

---

*Document Version: 1.0*  
*Last Updated: July 02, 2025*  
*Purpose: Technical specification for plugin architecture implementation and integration*