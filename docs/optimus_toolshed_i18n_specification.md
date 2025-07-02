# Optimus Toolshed - Internationalization and Localization Specification

## Localization Strategy Overview

The Optimus Toolshed internationalization system utilizes a reusable library-based architecture that provides external file-based localization capabilities for both the core application and all plugin tools. The system is built around the Optimus I18n library, a standalone Kotlin Compose Desktop localization framework that empowers system administrators to customize language support without requiring application rebuilds or development expertise. The localization framework operates through external properties files stored in dedicated language directories, enabling dynamic locale detection based on Windows system settings while providing comprehensive fallback mechanisms to ensure application stability across diverse deployment environments.

The library-based architecture transforms language support from a development-time constraint into a deployment-time capability, allowing organizations to implement localization strategies that align with their specific operational requirements and user demographics. Each application component, including individual management tools, utilizes the same Optimus I18n library through standardized APIs while maintaining their own language resources and participating in a shared localization ecosystem that ensures coherent user experience across all administrative functions. The system implements intelligent fallback strategies that gracefully degrade from user-specific locales to language families to default English resources, preventing interface failures while maximizing language coverage based on available translation resources.

The Optimus I18n library accommodates real-time language file updates through file system monitoring capabilities, enabling administrators to deploy translation improvements or corrections without application restarts or service interruptions. This live update mechanism supports continuous localization improvement workflows where translation teams can iterate on language resources and observe results immediately within the running application. The library architecture balances dynamic flexibility with performance optimization through intelligent caching and resource management that minimizes file system access while maintaining responsiveness to translation updates and locale changes. As a standalone library, the Optimus I18n framework can be adopted by other Kotlin Compose Desktop projects, providing a standardized approach to external file-based localization across multiple applications while ensuring plugin developers work within a consistent internationalization ecosystem.

## Optimus I18n Library Architecture

### Library Structure and Distribution
```
optimus-i18n-library/
├── core/
│   ├── ExternalLocalizationManager.kt
│   ├── LocalizationProvider.kt
│   ├── LanguageDetection.kt
│   ├── FileWatcher.kt
│   └── I18nConfiguration.kt
├── compose/
│   ├── LocalizationComposables.kt
│   ├── LocalizedComponents.kt
│   ├── CompositionLocalProviders.kt
│   └── PluginProviders.kt
├── validation/
│   ├── TranslationValidator.kt
│   ├── ValidationReport.kt
│   ├── QualityMetrics.kt
│   └── ComplianceChecker.kt
├── plugin/
│   ├── PluginLocalizationApi.kt
│   ├── PluginResourceManager.kt
│   ├── PluginI18nContract.kt
│   └── JarResourceExtractor.kt
└── utils/
    ├── LocaleUtils.kt
    ├── ResourceHelpers.kt
    ├── CacheManager.kt
    └── ExtensionRegistry.kt
```

### Library API and Configuration
```kotlin
// Main library entry point
class OptimusI18n private constructor(
    private val config: I18nConfiguration
) {
    companion object {
        fun initialize(config: I18nConfiguration): OptimusI18n {
            return OptimusI18n(config)
        }
    }
    
    fun createLocalizationManager(): ExternalLocalizationManager {
        return ExternalLocalizationManager(config)
    }
    
    fun createPluginLocalization(
        pluginId: String,
        pluginDirectory: Path
    ): PluginLocalizationProvider {
        return PluginExternalLocalization(
            pluginId = pluginId,
            pluginDirectory = pluginDirectory,
            globalManager = createLocalizationManager()
        )
    }
    
    fun registerExtension(extension: I18nExtension) {
        extensionRegistry.register(extension)
    }
}

data class I18nConfiguration(
    val applicationDirectory: Path = Paths.get(System.getProperty("user.dir")),
    val languagesSubdirectory: String = "languages",
    val defaultLocale: String = "en",
    val enableFileWatching: Boolean = true,
    val enableValidation: Boolean = true,
    val cacheSize: Int = 50,
    val encoding: Charset = StandardCharsets.UTF_8,
    val localeDetectionStrategy: LocaleDetectionStrategy = DefaultLocaleDetectionStrategy(),
    val fallbackStrategy: FallbackStrategy = DefaultFallbackStrategy()
)
```

## External File Architecture

### Directory Structure and Organization
```
optimus-toolshed/
├── languages/
│   ├── en.properties (default fallback)
│   ├── en_US.properties (regional variant)
│   ├── de.properties (German)
│   ├── de_DE.properties (German - Germany)
│   ├── de_AT.properties (German - Austria)
│   ├── fr.properties (French)
│   ├── es.properties (Spanish)
│   ├── ja.properties (Japanese)
│   ├── pt_BR.properties (Portuguese - Brazil)
│   └── zh_CN.properties (Chinese - Simplified)
├── plugins/
│   ├── server-configuration-tool/
│   │   └── languages/
│   │       ├── en.properties
│   │       ├── de.properties
│   │       └── fr.properties
│   ├── monitoring-tool/
│   │   └── languages/
│   │       ├── en.properties
│   │       ├── de.properties
│   │       └── es.properties
│   └── maintenance-tool/
│       └── languages/
│           ├── en.properties
│           └── ja.properties
└── documentation/
    └── localization/
        ├── translation-guide.md
        ├── language-codes.md
        └── sample-files/
```

### Properties File Format Standards
```properties
# Core Application - en.properties
# Application Identity
app.title=Optimus Toolshed
app.description=Comprehensive server management and administrative tools
app.version=Version {0}

# Master Interface Navigation
nav.configuration=Configuration
nav.monitoring=Monitoring
nav.troubleshooting=Troubleshooting
nav.maintenance=Maintenance
nav.settings=Settings
nav.help=Help

# Common Actions and Controls
action.save=Save
action.cancel=Cancel
action.reset=Reset to Defaults
action.export=Export
action.import=Import
action.refresh=Refresh
action.delete=Delete
action.edit=Edit
action.create=Create New
action.copy=Copy
action.apply=Apply Changes

# Status Messages and Feedback
msg.save_success=Settings saved successfully
msg.save_error=Failed to save settings: {0}
msg.load_error=Unable to load configuration: {0}
msg.validation_error=Validation failed: {0}
msg.operation_complete=Operation completed successfully
msg.operation_failed=Operation failed: {0}

# Form Labels and Descriptions
form.required_field=Required field
form.optional_field=Optional
form.invalid_input=Invalid input format
form.field_too_long=Input exceeds maximum length of {0} characters
form.field_too_short=Input must be at least {0} characters

# Dialog Titles and Content
dialog.confirm_save=Confirm Save Changes
dialog.confirm_delete=Confirm Deletion
dialog.unsaved_changes=You have unsaved changes. Do you want to save before continuing?
dialog.error_title=Error
dialog.warning_title=Warning
dialog.info_title=Information

# Accessibility Labels
accessibility.menu_button=Main menu button
accessibility.close_button=Close dialog
accessibility.expand_section=Expand section
accessibility.collapse_section=Collapse section
accessibility.required_field=Required field: {0}
```

## Core Application Integration

### Optimus Toolshed Initialization
```kotlin
// Application startup with library integration
class OptimusToolshedApplication {
    private val i18n = OptimusI18n.initialize(
        I18nConfiguration(
            applicationDirectory = getApplicationDirectory(),
            defaultLocale = "en",
            enableFileWatching = true,
            enableValidation = true,
            cacheSize = 100
        )
    )
    
    @Composable
    fun App() {
        val localizationManager = remember { i18n.createLocalizationManager() }
        
        OptimusI18nProvider(localizationManager) {
            OptimusTheme {
                MasterDetailLayout()
            }
        }
    }
    
    fun getI18nLibrary(): OptimusI18n = i18n
}

// Service registration for plugin access
class ToolContextProvider {
    fun createContext(): ToolContext {
        return DefaultToolContext().apply {
            registerService(OptimusI18n::class, application.getI18nLibrary())
        }
    }
}
```

## Plugin Library Integration

### Plugin Tool Implementation
```kotlin
// Server Configuration Tool using library
class ServerConfigurationTool : OptimusToolPlugin, LocalizablePlugin {
    private lateinit var pluginI18n: PluginLocalizationProvider
    
    override fun onInitialize(context: ToolContext): InitializationResult {
        val i18nLibrary = context.getService<OptimusI18n>()
        
        pluginI18n = i18nLibrary.createPluginLocalization(
            pluginId = pluginInfo.id,
            pluginDirectory = context.getPluginDirectory()
        )
        
        return InitializationResult.Success
    }
    
    @Composable
    override fun renderTool(container: ToolDetailContainer, context: ToolContext) {
        OptimusPluginI18nProvider(pluginI18n) {
            LaunchedEffect(Unit) {
                container.setHeader(
                    title = pluginI18n.getString("tool.title"),
                    description = pluginI18n.getString("tool.description"),
                    breadcrumbs = listOf(
                        getString("nav.configuration"), // Falls back to core app
                        pluginI18n.getString("tool.title")
                    )
                )
            }
            
            container.setContent {
                ServerConfigurationContent()
            }
        }
    }
    
    override fun createLocalizationProvider(
        pluginDirectory: Path,
        globalLocalizationManager: ExternalLocalizationManager
    ): PluginLocalizationProvider {
        return pluginI18n
    }
}

@Composable
private fun ServerConfigurationContent() {
    // Uses plugin's localization context with library components
    LocalizedCard("settings.network") {
        LocalizedTextField(
            value = hostAddress,
            onValueChange = { hostAddress = it },
            labelKey = "field.host_address",
            placeholderKey = "field.host_placeholder"
        )
        
        LocalizedCheckbox(
            checked = enableSSL,
            onCheckedChange = { enableSSL = it },
            labelKey = "field.enable_ssl"
        )
    }
}
```

### Plugin Build Configuration
```kotlin
// Plugin build.gradle.kts
dependencies {
    // Required: Optimus I18n Library
    implementation("com.optimus.tools:i18n-compose-desktop:1.0.0")
    
    // Required: Shared component library (depends on i18n library)
    implementation("com.optimus.toolshed:ui-components:${Versions.sharedLibrary}")
    
    // Required: Plugin API
    implementation("com.optimus.toolshed:plugin-api:${Versions.pluginApi}")
}
```

## Localization Management Implementation

### Library-Based Localization Manager
```kotlin
class ExternalLocalizationManager(
    private val applicationDirectory: Path = Paths.get(System.getProperty("user.dir")),
    private val languagesSubdirectory: String = "languages",
    private val defaultLocale: String = "en",
    private val enableFileWatching: Boolean = true
) {
    private val languagesDirectory = applicationDirectory.resolve(languagesSubdirectory)
    private val loadedBundles = ConcurrentHashMap<String, Properties>()
    private val currentLocale = mutableStateOf(detectSystemLocale())
    private val watchService: WatchService? = if (enableFileWatching) createFileWatcher() else null
    
    @Composable
    fun currentLocaleState(): State<String> = currentLocale
    
    fun getString(key: String, locale: String = currentLocale.value): String {
        return getLocalizedString(key, locale)
            ?: getLocalizedString(key, extractLanguageCode(locale))
            ?: getLocalizedString(key, defaultLocale)
            ?: key
    }
    
    fun getFormattedString(key: String, vararg args: Any, locale: String = currentLocale.value): String {
        val template = getString(key, locale)
        return try {
            MessageFormat(template, Locale.forLanguageTag(locale.replace('_', '-'))).format(args)
        } catch (e: Exception) {
            template
        }
    }
    
    private fun getLocalizedString(key: String, locale: String): String? {
        return getOrLoadBundle(locale).getProperty(key)
    }
    
    private fun getOrLoadBundle(locale: String): Properties {
        return loadedBundles.getOrPut(locale) {
            loadLanguageFile(locale)
        }
    }
    
    private fun loadLanguageFile(locale: String): Properties {
        val file = languagesDirectory.resolve("${locale}.properties")
        return Properties().apply {
            if (Files.exists(file)) {
                try {
                    Files.newBufferedReader(file, StandardCharsets.UTF_8).use { reader ->
                        load(reader)
                    }
                } catch (e: Exception) {
                    // Log error but continue with empty properties
                    System.err.println("Failed to load language file for $locale: ${e.message}")
                }
            }
        }
    }
    
    private fun detectSystemLocale(): String {
        val systemLocale = Locale.getDefault()
        val fullLocaleCode = "${systemLocale.language}_${systemLocale.country}"
        val languageCode = systemLocale.language
        
        return when {
            hasLanguageFile(fullLocaleCode) -> fullLocaleCode
            hasLanguageFile(languageCode) -> languageCode
            else -> defaultLocale
        }
    }
    
    private fun hasLanguageFile(locale: String): Boolean {
        return Files.exists(languagesDirectory.resolve("${locale}.properties"))
    }
    
    private fun extractLanguageCode(locale: String): String {
        return locale.substringBefore('_')
    }
    
    fun getAvailableLanguages(): List<LanguageInfo> {
        return try {
            if (!Files.exists(languagesDirectory)) {
                return listOf(LanguageInfo(defaultLocale, "English", true))
            }
            
            Files.list(languagesDirectory)
                .filter { it.toString().endsWith(".properties") }
                .map { path ->
                    val filename = path.fileName.toString()
                    val localeCode = filename.substringBefore(".properties")
                    LanguageInfo(
                        code = localeCode,
                        displayName = getLanguageDisplayName(localeCode),
                        isDefault = localeCode == defaultLocale
                    )
                }
                .sorted { a, b -> a.displayName.compareTo(b.displayName) }
                .toList()
        } catch (e: Exception) {
            listOf(LanguageInfo(defaultLocale, "English", true))
        }
    }
    
    private fun getLanguageDisplayName(localeCode: String): String {
        return try {
            val locale = Locale.forLanguageTag(localeCode.replace('_', '-'))
            locale.getDisplayName(locale).replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(locale) else it.toString() 
            }
        } catch (e: Exception) {
            localeCode
        }
    }
    
    fun setLocale(locale: String) {
        if (hasLanguageFile(locale) || locale == defaultLocale) {
            currentLocale.value = locale
            persistLocalePreference(locale)
        }
    }
    
    fun reloadLanguageFiles() {
        loadedBundles.clear()
        // Trigger recomposition by updating state
        val current = currentLocale.value
        currentLocale.value = current
    }
    
    private fun createFileWatcher(): WatchService? {
        return try {
            val watchService = FileSystems.getDefault().newWatchService()
            startFileWatching(watchService)
            watchService
        } catch (e: Exception) {
            null
        }
    }
    
    private fun startFileWatching(watchService: WatchService) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (Files.exists(languagesDirectory)) {
                    languagesDirectory.register(
                        watchService,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE
                    )
                }
                
                while (true) {
                    val key = watchService.take()
                    key.pollEvents().forEach { event ->
                        val filename = event.context().toString()
                        if (filename.endsWith(".properties")) {
                            delay(100) // Brief delay to ensure file write is complete
                            reloadLanguageFiles()
                        }
                    }
                    key.reset()
                }
            } catch (e: Exception) {
                // File watching failed, continue without live updates
            }
        }
    }
    
    private fun persistLocalePreference(locale: String) {
        // Implementation depends on application preferences system
        // Could use Java Preferences API or custom configuration file
    }
}

data class LanguageInfo(
    val code: String,
    val displayName: String,
    val isDefault: Boolean
)
```

### Library Plugin Localization Integration
```kotlin
interface PluginLocalizationProvider {
    fun getString(key: String, locale: String? = null): String
    fun getFormattedString(key: String, vararg args: Any, locale: String? = null): String
    fun getAvailableLanguages(): List<String>
    fun hasTranslation(key: String, locale: String): Boolean
}

class PluginExternalLocalization(
    private val pluginDirectory: Path,
    private val globalLocalizationManager: ExternalLocalizationManager,
    private val pluginId: String
) : PluginLocalizationProvider {
    
    private val languagesDirectory = pluginDirectory.resolve("languages")
    private val pluginBundles = ConcurrentHashMap<String, Properties>()
    
    override fun getString(key: String, locale: String?): String {
        val targetLocale = locale ?: globalLocalizationManager.currentLocaleState().value
        
        return getPluginString(key, targetLocale)
            ?: getPluginString(key, extractLanguageCode(targetLocale))
            ?: getPluginString(key, "en")
            ?: globalLocalizationManager.getString(key, targetLocale)
            ?: key
    }
    
    override fun getFormattedString(key: String, vararg args: Any, locale: String?): String {
        val template = getString(key, locale)
        val targetLocale = locale ?: globalLocalizationManager.currentLocaleState().value
        
        return try {
            MessageFormat(template, Locale.forLanguageTag(targetLocale.replace('_', '-'))).format(args)
        } catch (e: Exception) {
            template
        }
    }
    
    private fun getPluginString(key: String, locale: String): String? {
        return getOrLoadPluginBundle(locale).getProperty(key)
    }
    
    private fun getOrLoadPluginBundle(locale: String): Properties {
        return pluginBundles.getOrPut(locale) {
            loadPluginLanguageFile(locale)
        }
    }
    
    private fun loadPluginLanguageFile(locale: String): Properties {
        val file = languagesDirectory.resolve("${locale}.properties")
        return Properties().apply {
            if (Files.exists(file)) {
                try {
                    Files.newBufferedReader(file, StandardCharsets.UTF_8).use { reader ->
                        load(reader)
                    }
                } catch (e: Exception) {
                    System.err.println("Failed to load plugin language file for $pluginId/$locale: ${e.message}")
                }
            }
        }
    }
    
    override fun getAvailableLanguages(): List<String> {
        return try {
            if (!Files.exists(languagesDirectory)) return emptyList()
            
            Files.list(languagesDirectory)
                .filter { it.toString().endsWith(".properties") }
                .map { path ->
                    path.fileName.toString().substringBefore(".properties")
                }
                .toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override fun hasTranslation(key: String, locale: String): Boolean {
        return getPluginString(key, locale) != null
    }
    
    private fun extractLanguageCode(locale: String): String {
        return locale.substringBefore('_')
    }
    
    fun reloadLanguageFiles() {
        pluginBundles.clear()
    }
}
```

## Library Compose Integration Framework

### Composition Local Providers
```kotlin
// Core library providers
val LocalLocalizationManager = compositionLocalOf<ExternalLocalizationManager> {
    error("LocalizationManager not provided")
}

val LocalPluginLocalization = compositionLocalOf<PluginLocalizationProvider?> {
    null
}

val LocalI18nLibrary = compositionLocalOf<OptimusI18n> {
    error("OptimusI18n library not provided")
}

// Main application provider
@Composable
fun OptimusI18nProvider(
    localizationManager: ExternalLocalizationManager,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLocalizationManager provides localizationManager) {
        content()
    }
}

// Plugin-specific provider
@Composable
fun OptimusPluginI18nProvider(
    pluginLocalization: PluginLocalizationProvider,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPluginLocalization provides pluginLocalization) {
        content()
    }
}

// Library instance provider for advanced usage
@Composable
fun OptimusI18nLibraryProvider(
    i18nLibrary: OptimusI18n,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalI18nLibrary provides i18nLibrary) {
        content()
    }
}
```

### Library Localized Component Extensions
```kotlin
@Composable
fun LocalizedText(
    key: String,
    vararg formatArgs: Any,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fallbackText: String? = null
) {
    val localizationManager = LocalLocalizationManager.current
    val pluginLocalization = LocalPluginLocalization.current
    
    val text = remember(key, formatArgs) {
        val localization = pluginLocalization ?: localizationManager
        if (formatArgs.isNotEmpty()) {
            localization.getFormattedString(key, *formatArgs)
        } else {
            localization.getString(key)
        }.let { result ->
            if (result == key && fallbackText != null) fallbackText else result
        }
    }
    
    Text(
        text = text,
        modifier = modifier,
        style = style
    )
}

@Composable
fun LocalizedButton(
    textKey: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    vararg textArgs: Any
) {
    OptimusButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        LocalizedText(textKey, *textArgs)
    }
}

@Composable
fun LocalizedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelKey: String,
    modifier: Modifier = Modifier,
    placeholderKey: String? = null,
    helperTextKey: String? = null,
    isError: Boolean = false,
    errorMessageKey: String? = null
) {
    val localization = LocalLocalizationManager.current
    
    OptimusTextField(
        value = value,
        onValueChange = onValueChange,
        label = localization.getString(labelKey),
        placeholder = placeholderKey?.let { localization.getString(it) },
        helperText = helperTextKey?.let { localization.getString(it) },
        isError = isError,
        errorMessage = errorMessageKey?.let { localization.getString(it) },
        modifier = modifier
    )
}

@Composable
fun LocalizedCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    labelKey: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    vararg labelArgs: Any
) {
    val localizationManager = LocalLocalizationManager.current
    val pluginLocalization = LocalPluginLocalization.current
    
    val localization = pluginLocalization ?: localizationManager
    val label = remember(labelKey, labelArgs) {
        if (labelArgs.isNotEmpty()) {
            localization.getFormattedString(labelKey, *labelArgs)
        } else {
            localization.getString(labelKey)
        }
    }
    
    OptimusCheckbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        label = label,
        modifier = modifier,
        enabled = enabled
    )
}

// Convenience function for quick string access
@Composable
fun getString(key: String, vararg args: Any): String {
    val localizationManager = LocalLocalizationManager.current
    val pluginLocalization = LocalPluginLocalization.current
    
    return remember(key, args) {
        val localization = pluginLocalization ?: localizationManager
        if (args.isNotEmpty()) {
            localization.getFormattedString(key, *args)
        } else {
            localization.getString(key)
        }
    }
}
```

## Library Plugin Integration Requirements

### Plugin Localization Contract
```kotlin
// Required interface for localizable plugins using library
interface LocalizablePlugin {
    fun createLocalizationProvider(
        pluginDirectory: Path,
        globalLocalizationManager: ExternalLocalizationManager
    ): PluginLocalizationProvider
    
    fun getRequiredTranslationKeys(): List<String>
    fun validateTranslations(locale: String): List<TranslationIssue>
    fun extractDefaultLanguageResources(): Result<Unit>
}

// Enhanced plugin localization provider from library
interface PluginLocalizationProvider {
    fun getString(key: String, locale: String? = null): String
    fun getFormattedString(key: String, vararg args: Any, locale: String? = null): String
    fun getAvailableLanguages(): List<String>
    fun hasTranslation(key: String, locale: String): Boolean
    fun reloadLanguageFiles()
    fun validateTranslations(): ValidationReport
    fun getTranslationCompleteness(): Map<String, Double>
}

// Library-provided resource extraction for plugins
interface PluginResourceManager {
    fun extractEmbeddedResources(pluginJar: Path, targetDirectory: Path): Result<List<Path>>
    fun validateResourceStructure(pluginDirectory: Path): ValidationResult
    fun getLanguageFile(pluginDirectory: Path, locale: String): Path?
    fun createDefaultLanguageFiles(pluginDirectory: Path, keys: List<String>): Result<Unit>
}

data class TranslationIssue(
    val key: String,
    val locale: String,
    val issue: IssueType,
    val message: String,
    val severity: IssueSeverity = IssueSeverity.Error
)

enum class IssueType {
    MISSING_KEY,
    EMPTY_VALUE,
    FORMATTING_ERROR,
    ENCODING_ISSUE,
    DUPLICATE_KEY,
    INVALID_INTERPOLATION
}

enum class IssueSeverity {
    Error, Warning, Info
}

// Plugin implementation example
class ServerConfigurationTool : OptimusToolPlugin, LocalizablePlugin {
    private lateinit var localization: PluginLocalizationProvider
    
    override fun createLocalizationProvider(
        pluginDirectory: Path,
        globalLocalizationManager: ExternalLocalizationManager
    ): PluginLocalizationProvider {
        return PluginExternalLocalization(
            pluginDirectory = pluginDirectory,
            globalLocalizationManager = globalLocalizationManager,
            pluginId = pluginInfo.id
        ).also { localization = it }
    }
    
    override fun getRequiredTranslationKeys(): List<String> {
        return listOf(
            "plugin.server_config.title",
            "plugin.server_config.description",
            "plugin.server_config.network_settings",
            "plugin.server_config.host_address",
            "plugin.server_config.port_number",
            "plugin.server_config.enable_ssl"
        )
    }
    
    @Composable
    override fun renderTool(container: ToolDetailContainer, context: ToolContext) {
        PluginLocalizationProvider(localization) {
            LaunchedEffect(Unit) {
                container.setHeader(
                    title = localization.getString("plugin.server_config.title"),
                    description = localization.getString("plugin.server_config.description"),
                    breadcrumbs = listOf(
                        LocalLocalizationManager.current.getString("nav.configuration"),
                        localization.getString("plugin.server_config.title")
                    )
                )
            }
            
            container.setContent {
                ServerConfigurationContent()
            }
        }
    }
}
```

### Library Translation Validation Framework
```kotlin
```kotlin
// Enhanced validation framework provided by library
class LibraryTranslationValidator(
    private val i18nLibrary: OptimusI18n
) {
    fun validateAllTranslations(): ComprehensiveValidationReport {
        val coreReport = validateCoreApplication()
        val pluginReports = validateAllPlugins()
        
        return ComprehensiveValidationReport(
            coreValidation = coreReport,
            pluginValidations = pluginReports,
            totalLanguages = getTotalLanguageCount(),
            totalIssues = coreReport.totalIssues + pluginReports.sumOf { it.totalIssues },
            overallCompleteness = calculateOverallCompleteness()
        )
    }
    
    fun validateCoreApplication(): ValidationReport {
        val localizationManager = i18nLibrary.createLocalizationManager()
        return performValidation(localizationManager, "core")
    }
    
    fun validatePlugin(pluginId: String, pluginDirectory: Path): ValidationReport {
        val pluginLocalization = i18nLibrary.createPluginLocalization(pluginId, pluginDirectory)
        return performValidation(pluginLocalization, pluginId)
    }
    
    private fun performValidation(
        localization: Any, // LocalizationProvider interface
        componentId: String
    ): ValidationReport {
        val availableLanguages = getAvailableLanguages(localization)
        val issues = mutableListOf<TranslationIssue>()
        
        availableLanguages.forEach { language ->
            issues.addAll(validateLanguage(localization, language.code, componentId))
        }
        
        return ValidationReport(
            componentId = componentId,
            totalLanguages = availableLanguages.size,
            totalIssues = issues.size,
            issues = issues,
            completenessMetrics = calculateCompleteness(localization, availableLanguages)
        )
    }
    
    private fun validateLanguage(
        localization: Any,
        locale: String,
        componentId: String
    ): List<TranslationIssue> {
        val issues = mutableListOf<TranslationIssue>()
        val referenceKeys = getReferenceKeys(localization, componentId)
        
        referenceKeys.forEach { key ->
            val translation = getTranslationSafely(localization, key, locale)
            
            when {
                translation == key -> issues.add(
                    TranslationIssue(
                        key = key,
                        locale = locale,
                        issue = IssueType.MISSING_KEY,
                        message = "Translation not found for key '$key'",
                        severity = IssueSeverity.Error
                    )
                )
                translation.isBlank() -> issues.add(
                    TranslationIssue(
                        key = key,
                        locale = locale,
                        issue = IssueType.EMPTY_VALUE,
                        message = "Empty translation for key '$key'",
                        severity = IssueSeverity.Warning
                    )
                )
                !isValidFormatting(translation) -> issues.add(
                    TranslationIssue(
                        key = key,
                        locale = locale,
                        issue = IssueType.FORMATTING_ERROR,
                        message = "Invalid message formatting in translation for key '$key'",
                        severity = IssueSeverity.Error
                    )
                )
            }
        }
        
        return issues
    }
    
    private fun getReferenceKeys(localization: Any, componentId: String): Set<String> {
        // Extract all keys from default language file for the component
        return when (localization) {
            is ExternalLocalizationManager -> extractCoreKeys(localization)
            is PluginLocalizationProvider -> extractPluginKeys(localization)
            else -> emptySet()
        }
    }
    
    private fun isValidFormatting(text: String): Boolean {
        return try {
            MessageFormat(text)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun calculateCompleteness(
        localization: Any,
        languages: List<LanguageInfo>
    ): Map<String, Double> {
        return languages.associate { language ->
            val totalKeys = getReferenceKeys(localization, "").size
            val translatedKeys = countTranslatedKeys(localization, language.code)
            language.code to (translatedKeys.toDouble() / totalKeys * 100)
        }
    }
}

data class ComprehensiveValidationReport(
    val coreValidation: ValidationReport,
    val pluginValidations: List<ValidationReport>,
    val totalLanguages: Int,
    val totalIssues: Int,
    val overallCompleteness: Double
)

data class ValidationReport(
    val componentId: String,
    val totalLanguages: Int,
    val totalIssues: Int,
    val issues: List<TranslationIssue>,
    val completenessMetrics: Map<String, Double>
)ue(key, locale, IssueType.FORMATTING_ERROR, "Invalid message formatting")
                )
            }
        }
        
        return issues
    }
    
    private fun getReferenceKeys(): Set<String> {
        // Implementation to extract all keys from default language file
        return emptySet()
    }
    
    private fun isValidFormatting(text: String): Boolean {
        return try {
            MessageFormat(text)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun calculateCompleteness(languages: List<LanguageInfo>): Map<String, Double> {
        // Implementation to calculate translation completeness percentages
        return emptyMap()
    }
}

data class ValidationReport(
    val totalLanguages: Int,
    val totalIssues: Int,
    val issues: List<TranslationIssue>,
    val completenessMetrics: Map<String, Double>
)
```

## Library Extension and Customization

### Extension Points and Registry
```kotlin
// Library extension interfaces
interface LocaleDetectionStrategy {
    fun detectSystemLocale(): String
    fun detectUserPreference(): String?
    fun getSupportedLocales(): List<String>
}

interface TranslationFileLoader {
    fun canHandle(file: Path): Boolean
    fun loadTranslations(file: Path): Map<String, String>
    fun getSupportedExtensions(): List<String>
    fun validateFile(file: Path): ValidationResult
}

interface FallbackStrategy {
    fun getFallbackChain(targetLocale: String): List<String>
    fun shouldUseFallback(key: String, locale: String, translation: String?): Boolean
}

// Extension registry for customization
class I18nExtensionRegistry {
    private val localeDetectors = mutableListOf<LocaleDetectionStrategy>()
    private val fileLoaders = mutableListOf<TranslationFileLoader>()
    private val validators = mutableListOf<TranslationValidator>()
    private val fallbackStrategies = mutableListOf<FallbackStrategy>()
    
    fun registerLocaleDetector(strategy: LocaleDetectionStrategy) {
        localeDetectors.add(strategy)
    }
    
    fun registerFileLoader(loader: TranslationFileLoader) {
        fileLoaders.add(loader)
    }
    
    fun registerValidator(validator: TranslationValidator) {
        validators.add(validator)
    }
    
    fun registerFallbackStrategy(strategy: FallbackStrategy) {
        fallbackStrategies.add(strategy)
    }
    
    // Registry access methods
    fun getLocaleDetectors(): List<LocaleDetectionStrategy> = localeDetectors.toList()
    fun getFileLoaders(): List<TranslationFileLoader> = fileLoaders.toList()
    fun getValidators(): List<TranslationValidator> = validators.toList()
    fun getFallbackStrategies(): List<FallbackStrategy> = fallbackStrategies.toList()
}

// Predefined configurations for common scenarios
object I18nDefaults {
    val enterprise = I18nConfiguration(
        defaultLocale = "en",
        enableFileWatching = true,
        enableValidation = true,
        cacheSize = 100,
        localeDetectionStrategy = EnterpriseLocaleDetectionStrategy(),
        fallbackStrategy = StrictFallbackStrategy()
    )
    
    val development = I18nConfiguration(
        defaultLocale = "en",
        enableFileWatching = true,
        enableValidation = false,
        cacheSize = 10,
        localeDetectionStrategy = DefaultLocaleDetectionStrategy(),
        fallbackStrategy = PermissiveFallbackStrategy()
    )
    
    val embedded = I18nConfiguration(
        enableFileWatching = false,
        enableValidation = false,
        cacheSize = 20,
        localeDetectionStrategy = StaticLocaleDetectionStrategy(),
        fallbackStrategy = EmbeddedFallbackStrategy()
    )
}
```

### External Project Integration
```kotlin
// Example usage in other Kotlin Compose Desktop projects
class MyDesktopApplication {
    private val i18n = OptimusI18n.initialize(
        I18nDefaults.enterprise.copy(
            applicationDirectory = Paths.get("./my-app"),
            defaultLocale = "fr" // French as default
        )
    )
    
    @Composable
    fun MyApp() {
        val localizationManager = remember { i18n.createLocalizationManager() }
        
        OptimusI18nProvider(localizationManager) {
            MaterialTheme {
                MyAppContent()
            }
        }
    }
}

// Using library components in external projects
@Composable
fun MyAppContent() {
    Column {
        LocalizedText("app.welcome_message")
        
        LocalizedButton(
            textKey = "action.get_started",
            onClick = { startApplication() }
        )
        
        LocalizedTextField(
            value = userName,
            onValueChange = { userName = it },
            labelKey = "form.username",
            placeholderKey = "form.username_hint"
        )
    }
}
```

## Library Distribution and Versioning

### Gradle Publishing Configuration
```kotlin
// Library build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    `maven-publish`
    signing
}

kotlin {
    jvm {
        jvmToolchain(17)
    }
    
    sourceSets {
        val jvmMain by getting {
            dependencies {
                api(compose.desktop.common)
                api(compose.runtime)
                api(compose.material3)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(compose.desktop.uiTestJUnit4)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.optimus.tools"
            artifactId = "i18n-compose-desktop"
            version = project.version.toString()
            
            from(components["kotlin"])
            
            pom {
                name.set("Optimus I18n Library")
                description.set("External file-based internationalization library for Kotlin Compose Desktop")
                url.set("https://github.com/optimus-tools/i18n-compose-desktop")
                
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                
                developers {
                    developer {
                        id.set("optimus-tools")
                        name.set("Optimus Tools Team")
                        email.set("dev@optimus-tools.com")
                    }
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "OptimusNexus"
            url = uri("https://nexus.optimus-tools.com/repository/maven-public/")
            credentials {
                username = project.findProperty("nexus.username") as String?
                password = project.findProperty("nexus.password") as String?
            }
        }
    }
}
```

### Consumer Integration
```kotlin
// In consuming projects (Optimus Toolshed tools, external projects)
dependencies {
    // Core library
    implementation("com.optimus.tools:i18n-compose-desktop:1.0.0")
    
    // Optional: Additional file format support
    implementation("com.optimus.tools:i18n-yaml-support:1.0.0")
    implementation("com.optimus.tools:i18n-json-support:1.0.0")
}

// Version compatibility checking
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.optimus.tools" && requested.name.startsWith("i18n-")) {
            if (requested.version != "1.0.0") {
                throw GradleException(
                    "All Optimus I18n library components must use the same version. " +
                    "Found: ${requested.version}, expected: 1.0.0"
                )
            }
        }
    }
}
```

## User Interface Integration

### Library-Enhanced Language Selection Interface
```kotlin
@Composable
fun LibraryLanguageSettingsPanel() {
    val i18nLibrary = LocalI18nLibrary.current
    val localizationManager = LocalLocalizationManager.current
    val availableLanguages = remember { localizationManager.getAvailableLanguages() }
    val currentLanguage by localizationManager.currentLocaleState()
    
    LocalizedCard("settings.language_preferences") {
        OptimusFormSection {
            OptimusDropdown(
                selectedValue = currentLanguage,
                onValueSelected = { newLocale ->
                    localizationManager.setLocale(newLocale)
                },
                options = availableLanguages.map { it.code },
                label = getString("settings.interface_language"),
                displayTransform = { code ->
                    availableLanguages.find { it.code == code }?.displayName ?: code
                }
            )
            
            OptimusInfoBox(
                message = getString("settings.language_restart_note"),
                type = InfoBoxType.Information
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocalizedButton(
                    textKey = "action.reload_languages",
                    onClick = { localizationManager.reloadLanguageFiles() }
                )
                
                LocalizedButton(
                    textKey = "action.open_language_folder",
                    onClick = { openLanguageFolder() }
                )
                
                LocalizedButton(
                    textKey = "action.validate_translations",
                    onClick = { validateAllTranslations(i18nLibrary) }
                )
                
                LocalizedButton(
                    textKey = "action.export_template",
                    onClick = { exportTranslationTemplate(i18nLibrary) }
                )
            }
        }
    }
}

@Composable
fun LibraryTranslationStatusPanel() {
    val i18nLibrary = LocalI18nLibrary.current
    val validator = remember { LibraryTranslationValidator(i18nLibrary) }
    var validationReport by remember { mutableStateOf<ComprehensiveValidationReport?>(null) }
    var isValidating by remember { mutableStateOf(false) }
    
    LocalizedCard("settings.translation_status") {
        LaunchedEffect(Unit) {
            isValidating = true
            validationReport = withContext(Dispatchers.IO) {
                validator.validateAllTranslations()
            }
            isValidating = false
        }
        
        if (isValidating) {
            OptimusProgressIndicator(
                message = getString("msg.validating_translations")
            )
        } else {
            validationReport?.let { report ->
                // Core application status
                OptimusExpandableSection(
                    title = getString("status.core_application"),
                    subtitle = getString("status.issues_count", report.coreValidation.totalIssues)
                ) {
                    TranslationReportDisplay(report.coreValidation)
                }
                
                // Plugin status
                OptimusExpandableSection(
                    title = getString("status.plugins"),
                    subtitle = getString("status.plugins_count", report.pluginValidations.size)
                ) {
                    report.pluginValidations.forEach { pluginReport ->
                        OptimusCard(
                            title = pluginReport.componentId,
                            subtitle = getString("status.issues_count", pluginReport.totalIssues)
                        ) {
                            TranslationReportDisplay(pluginReport)
                        }
                    }
                }
                
                // Overall metrics
                OptimusMetricsPanel {
                    MetricItem(
                        label = getString("metric.total_languages"),
                        value = report.totalLanguages.toString()
                    )
                    MetricItem(
                        label = getString("metric.total_issues"),
                        value = report.totalIssues.toString()
                    )
                    MetricItem(
                        label = getString("metric.overall_completeness"),
                        value = "${report.overallCompleteness.roundToInt()}%"
                    )
                }
            }
        }
    }
}

@Composable
private fun TranslationReportDisplay(report: ValidationReport) {
    OptimusDataTable(
        columns = listOf(
            TableColumn(getString("column.language"), weight = 0.25f),
            TableColumn(getString("column.completeness"), weight = 0.25f),
            TableColumn(getString("column.errors"), weight = 0.25f),
            TableColumn(getString("column.warnings"), weight = 0.25f)
        ),
        data = report.completenessMetrics.map { (locale, completeness) ->
            val errors = report.issues.count { it.locale == locale && it.severity == IssueSeverity.Error }
            val warnings = report.issues.count { it.locale == locale && it.severity == IssueSeverity.Warning }
            
            TranslationStatusRow(
                language = locale,
                completeness = "${completeness.roundToInt()}%",
                errors = errors.toString(),
                warnings = warnings.toString()
            )
        }
    )
}

private fun validateAllTranslations(i18nLibrary: OptimusI18n) {
    // Trigger comprehensive validation and show results
    GlobalScope.launch {
        val validator = LibraryTranslationValidator(i18nLibrary)
        val report = validator.validateAllTranslations()
        // Handle validation results
    }
}

private fun exportTranslationTemplate(i18nLibrary: OptimusI18n) {
    // Export translation template files for translators
    GlobalScope.launch {
        val exporter = i18nLibrary.createTemplateExporter()
        exporter.exportTranslationTemplate()
    }
}

private fun openLanguageFolder() {
    try {
        Desktop.getDesktop().open(File("languages"))
    } catch (e: Exception) {
        // Handle error opening folder
    }
}
```

## Performance and Resource Management

### Library-Optimized Caching and Performance
- **Shared Cache Management**: Library maintains global cache shared across core app and all plugins
- **Lazy Loading**: Language files loaded on-demand when first accessed by any component
- **Memory Management**: Configurable LRU cache with size limits and automatic cleanup
- **File Watching Optimization**: Single file watcher instance shared across all library consumers
- **Startup Performance**: Critical default language loaded synchronously, others loaded asynchronously
- **Plugin Isolation**: Plugin-specific caches with controlled memory usage per plugin
- **Resource Cleanup**: Automatic cleanup of unused language resources during idle periods

### Library Error Handling and Recovery
- **Graceful Degradation**: Library ensures application continues functioning with missing translations
- **Fallback Chain**: Configurable fallback strategies (specific locale → language family → default → key display)
- **File System Errors**: Robust handling of missing directories, corrupted files, and permission issues
- **Encoding Issues**: UTF-8 enforcement with intelligent fallback to system encoding
- **Plugin Isolation**: Plugin translation failures don't affect core application or other plugins
- **Validation Integration**: Real-time error detection and reporting through validation framework
- **Recovery Mechanisms**: Automatic retry logic for transient file system issues

### Library Deployment and Distribution Considerations
- **Versioned Distribution**: Library follows semantic versioning for compatibility management
- **Default Language Bundle**: Core English translations embedded in library as ultimate fallback
- **Optional Language Packs**: Additional languages can be distributed as separate packages
- **Update Mechanisms**: Language file updates independent of library and application updates
- **Validation Tools**: Command-line utilities for translation validation and testing included with library
- **Documentation**: Comprehensive translation guides and API documentation for developers and translators
- **Migration Support**: Tools and guides for migrating from other i18n solutions to Optimus I18n library
- **Community Support**: Open-source components to encourage broader adoption and contribution

## Library Documentation and Developer Support

### API Documentation Requirements
- **Complete KDoc coverage** for all public interfaces and classes
- **Usage examples** for common integration scenarios
- **Migration guides** from popular i18n libraries (Android Resources, Java ResourceBundle)
- **Performance tuning guides** for enterprise deployments
- **Plugin development tutorials** with step-by-step integration examples
- **Troubleshooting guides** for common issues and error resolution

### Testing and Quality Assurance
- **Comprehensive unit tests** for all library components
- **Integration tests** with sample applications and plugins  
- **Performance benchmarks** for large-scale deployments
- **Compatibility testing** across different Kotlin and Compose versions
- **Memory leak detection** and resource cleanup validation
- **File system stress testing** for concurrent access scenarios

### Community and Ecosystem Development
- **Open source components** for broader community adoption
- **Plugin marketplace integration** for sharing localized tools
- **Translation community tools** for collaborative translation efforts
- **IDE integration** for development-time translation validation
- **CI/CD pipeline integration** for automated translation quality checks

---

*Document Version: 1.0*  
*Last Updated: July 02, 2025*  
*Purpose: Technical specification for external file-based internationalization and localization system*