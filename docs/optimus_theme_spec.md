## Dynamic Theme Configuration

### Theme Configuration System
```kotlin
// ThemeConfiguration.kt - Runtime theme customization
data class OptimusThemeConfiguration(
    val brandColors: BrandColorConfiguration = BrandColorConfiguration.Default,
    val typography: TypographyConfiguration = TypographyConfiguration.Default,
    val density: ThemeDensity = ThemeDensity.Standard,
    val accessibility: AccessibilityConfiguration = AccessibilityConfiguration.Default,
    val layout: LayoutConfiguration = LayoutConfiguration.Default,
    val animations: AnimationConfiguration = AnimationConfiguration.Default
) {
    companion object {
        val Default = OptimusThemeConfiguration()
        val HighContrast = OptimusThemeConfiguration(
            accessibility = AccessibilityConfiguration.HighContrast
        )
        val Dense = OptimusThemeConfiguration(
            density = ThemeDensity.Compact
        )
        val Enterprise = OptimusThemeConfiguration(
            brandColors = BrandColorConfiguration.Enterprise,
            typography = TypographyConfiguration.Enterprise
        )
    }
}

data class BrandColorConfiguration(
    val primaryColor: Color = OptimusColorTokens.Primary40,
    val secondaryColor: Color = OptimusColorTokens.Secondary40,
    val brandAccent: Color? = null,
    val customLogo: String? = null
) {
    companion object {
        val Default = BrandColorConfiguration()
        val Enterprise = BrandColorConfiguration(
            primaryColor = Color(0xFF0066CC),
            secondaryColor = Color(0xFF4A5568),
            brandAccent = Color(0xFF00A86B)
        )
    }
}

data class TypographyConfiguration(
    val fontFamily: FontFamily = OptimusTypographyTokens.PrimaryFontFamily,
    val scaleMultiplier: Float = 1.0f,
    val enableCustomFonts: Boolean = true
) {
    companion object {
        val Default = TypographyConfiguration()
        val Enterprise = TypographyConfiguration(
            fontFamily = FontFamily.Default, // System font for enterprise
            enableCustomFonts = false
        )
        val Large = TypographyConfiguration(
            scaleMultiplier = 1.15f
        )
    }
}

enum class ThemeDensity(val multiplier: Float) {
    Compact(0.85f),
    Standard(1.0f),
    Comfortable(1.15f)
}

data class AccessibilityConfiguration(
    val highContrast: Boolean = false,
    val reduceMotion: Boolean = false,
    val largeText: Boolean = false,
    val focusIndicatorStyle: FocusIndicatorStyle = FocusIndicatorStyle.Standard
) {
    companion object {
        val Default = AccessibilityConfiguration()
        val HighContrast = AccessibilityConfiguration(
            highContrast = true,
            focusIndicatorStyle = FocusIndicatorStyle.Bold
        )
        val ReducedMotion = AccessibilityConfiguration(
            reduceMotion = true
        )
    }
    
    enum class FocusIndicatorStyle {
        Standard, Bold, Outline, HighContrast
    }
}

data class LayoutConfiguration(
    val masterPanelWidth: Dp = 280.dp,
    val enablePanelResize: Boolean = true,
    val compactMode: Boolean = false
) {
    companion object {
        val Default = LayoutConfiguration()
        val Compact = LayoutConfiguration(
            masterPanelWidth = 240.dp,
            compactMode = true
        )
        val Wide = LayoutConfiguration(
            masterPanelWidth = 320.dp
        )
    }
}

data class AnimationConfiguration(
    val enableAnimations: Boolean = true,
    val animationDurationMultiplier: Float = 1.0f,
    val enableTransitions: Boolean = true
) {
    companion object {
        val Default = AnimationConfiguration()
        val Fast = AnimationConfiguration(
            animationDurationMultiplier = 0.75f
        )
        val Slow = AnimationConfiguration(
            animationDurationMultiplier = 1.5f
        )
        val NoAnimations = AnimationConfiguration(
            enableAnimations = false,
            enableTransitions = false
        )
    }
}
```

### Configurable Theme Provider
```kotlin
// OptimusConfigurableTheme.kt - Dynamic theme application
@Composable
fun OptimusConfigurableTheme(
    configuration: OptimusThemeConfiguration = OptimusThemeConfiguration.Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = remember(configuration.brandColors, darkTheme) {
        generateDynamicColorScheme(configuration.brandColors, darkTheme)
    }
    
    val dynamicExtendedColors = remember(configuration.brandColors, darkTheme) {
        generateExtendedColors(configuration.brandColors, darkTheme, configuration.accessibility.highContrast)
    }
    
    val adjustedTypography = remember(configuration.typography, configuration.density) {
        OptimusTypography.adjustForConfiguration(configuration.typography, configuration.density)
    }
    
    val adjustedSpacing = remember(configuration.density) {
        OptimusSpacing.adjustForDensity(configuration.density)
    }
    
    CompositionLocalProvider(
        LocalOptimusExtendedColors provides dynamicExtendedColors,
        LocalOptimusSpacing provides adjustedSpacing,
        LocalOptimusDensity provides configuration.density,
        LocalAccessibilityConfiguration provides configuration.accessibility,
        LocalThemeConfiguration provides configuration
    ) {
        MaterialTheme(
            colorScheme = dynamicColorScheme,
            typography = adjustedTypography,
            shapes = OptimusShapes,
            content = content
        )
    }
}

private fun generateDynamicColorScheme(
    brandColors: BrandColorConfiguration,
    isDark: Boolean
): ColorScheme {
    return if (isDark) {
        OptimusDarkColorScheme.copy(
            primary = brandColors.primaryColor.adjustForDarkTheme(),
            secondary = brandColors.secondaryColor.adjustForDarkTheme()
        )
    } else {
        OptimusLightColorScheme.copy(
            primary = brandColors.primaryColor,
            secondary = brandColors.secondaryColor
        )
    }
}

private fun Color.adjustForDarkTheme(): Color {
    // Lighten colors for dark theme visibility
    return this.copy(
        red = minOf(1.0f, this.red + 0.2f),
        green = minOf(1.0f, this.green + 0.2f),
        blue = minOf(1.0f, this.blue + 0.2f)
    )
}
```

## Component Theming Standards

### Atomic Component Theming
```kotlin
// Component-specific theming following atomic design
object OptimusButtonDefaults {
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    @Composable
    fun elevation(
        defaultElevation: Dp = 1.dp,
        pressedElevation: Dp = 2.dp,
        focusedElevation: Dp = 2.dp,
        hoveredElevation: Dp = 3.dp,
        disabledElevation: Dp = 0.dp
    ): ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        disabledElevation = disabledElevation
    )
    
    val ContentPadding = PaddingValues(
        horizontal = OptimusSpacing.Medium,
        vertical = OptimusSpacing.Small
    )
    
    val MinHeight = 40.dp
    val MinWidth = 64.dp
}

object OptimusTextFieldDefaults {
    @Composable
    fun colors(
        focusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        disabledTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorTextColor: Color = MaterialTheme.colorScheme.error,
        focusedContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        errorContainerColor: Color = MaterialTheme.colorScheme.errorContainer,
        focusedIndicatorColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor: Color = MaterialTheme.colorScheme.outline,
        disabledIndicatorColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorIndicatorColor: Color = MaterialTheme.colorScheme.error,
        focusedLabelColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLabelColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorLabelColor: Color = MaterialTheme.colorScheme.error,
        placeholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledPlaceholderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): TextFieldColors = TextFieldDefaults.colors(
        focusedTextColor = focusedTextColor,
        unfocusedTextColor = unfocusedTextColor,
        disabledTextColor = disabledTextColor,
        errorTextColor = errorTextColor,
        focusedContainerColor = focusedContainerColor,
        unfocusedContainerColor = unfocusedContainerColor,
        disabledContainerColor = disabledContainerColor,
        errorContainerColor = errorContainerColor,
        focusedIndicatorColor = focusedIndicatorColor,
        unfocusedIndicatorColor = unfocusedIndicatorColor,
        disabledIndicatorColor = disabledIndicatorColor,
        errorIndicatorColor = errorIndicatorColor,
        focusedLabelColor = focusedLabelColor,
        unfocusedLabelColor = unfocusedLabelColor,
        disabledLabelColor = disabledLabelColor,
        errorLabelColor = errorLabelColor,
        placeholderColor = placeholderColor,
        disabledPlaceholderColor = disabledPlaceholderColor
    )
    
    val ContentPadding = PaddingValues(
        horizontal = OptimusSpacing.Medium,
        vertical = OptimusSpacing.Small
    )
    
    val MinHeight = 48.dp
}

object OptimusCardDefaults {
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): CardColors = CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    @Composable
    fun elevation(
        defaultElevation: Dp = 1.dp,
        pressedElevation: Dp = 2.dp,
        focusedElevation: Dp = 2.dp,
        hoveredElevation: Dp = 3.dp,
        draggedElevation: Dp = 8.dp,
        disabledElevation: Dp = 0.dp
    ): CardElevation = CardDefaults.cardElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        draggedElevation = draggedElevation,
        disabledElevation = disabledElevation
    )
    
    val ContentPadding = PaddingValues(OptimusSpacing.Medium)
    val Shape = OptimusShapes.medium
}
```

### Desktop-Specific Component Theming
```kotlin
// Desktop application-specific components
object OptimusMasterPanelDefaults {
    @Composable
    fun colors(
        backgroundColor: Color = MaterialTheme.colorScheme.extended.masterPanelBackground,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        dividerColor: Color = MaterialTheme.colorScheme.extended.masterPanelDivider,
        selectedColor: Color = MaterialTheme.colorScheme.extended.masterPanelSelected,
        hoverColor: Color = MaterialTheme.colorScheme.extended.masterPanelHover
    ): MasterPanelColors = MasterPanelColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        dividerColor = dividerColor,
        selectedColor = selectedColor,
        hoverColor = hoverColor
    )
    
    val Width = OptimusSpacing.MasterPanelWidth
    val ContentPadding = PaddingValues(OptimusSpacing.Medium)
    val ItemHeight = 40.dp
    val SectionSpacing = OptimusSpacing.Large
}

object OptimusDetailPanelDefaults {
    @Composable
    fun colors(
        backgroundColor: Color = MaterialTheme.colorScheme.extended.detailPanelBackground,
        headerColor: Color = MaterialTheme.colorScheme.extended.detailPanelHeader,
        toolbarColor: Color = MaterialTheme.colorScheme.extended.toolbarBackground,
        contentColor: Color = MaterialTheme.colorScheme.onSurface
    ): DetailPanelColors = DetailPanelColors(
        backgroundColor = backgroundColor,
        headerColor = headerColor,
        toolbarColor = toolbarColor,
        contentColor = contentColor
    )
    
    val HeaderHeight = OptimusSpacing.HeaderHeight
    val ToolbarHeight = OptimusSpacing.ToolbarHeight
    val ContentPadding = PaddingValues(OptimusSpacing.Large)
}

object OptimusToolbarDefaults {
    @Composable
    fun colors(
        backgroundColor: Color = MaterialTheme.colorScheme.extended.toolbarBackground,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        dividerColor: Color = MaterialTheme.colorScheme.outline
    ): ToolbarColors = ToolbarColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        dividerColor = dividerColor
    )
    
    val Height = OptimusSpacing.ToolbarHeight
    val ContentPadding = PaddingValues(
        horizontal = OptimusSpacing.Large,
        vertical = OptimusSpacing.Small
    )
    val ButtonSpacing = OptimusSpacing.Small
}

// Color data classes for custom components
data class MasterPanelColors(
    val backgroundColor: Color,
    val contentColor: Color,
    val dividerColor: Color,
    val selectedColor: Color,
    val hoverColor: Color
)

data class DetailPanelColors(
    val backgroundColor: Color,
    val headerColor: Color,
    val toolbarColor: Color,
    val contentColor: Color
)

data class ToolbarColors(
    val backgroundColor: Color,
    val contentColor: Color,
    val dividerColor: Color
)
```

## Plugin Integration System

### Plugin Theme Contract
```kotlin
// Theme contract interface for plugins
interface PluginThemeProvider {
    @Composable
    fun getColorScheme(): ColorScheme
    
    @Composable
    fun getExtendedColors(): OptimusExtendedColors
    
    @Composable
    fun getTypography(): Typography
    
    @Composable
    fun getShapes(): Shapes
    
    @Composable
    fun getSpacing(): OptimusSpacingValues
    
    @Composable
    fun getDensity(): ThemeDensity
    
    @Composable
    fun getAccessibilityConfiguration(): AccessibilityConfiguration
}

// Default implementation provided to plugins
class DefaultPluginThemeProvider : PluginThemeProvider {
    @Composable
    override fun getColorScheme(): ColorScheme = MaterialTheme.colorScheme
    
    @Composable
    override fun getExtendedColors(): OptimusExtendedColors = MaterialTheme.colorScheme.extended
    
    @Composable
    override fun getTypography(): Typography = MaterialTheme.typography
    
    @Composable
    override fun getShapes(): Shapes = MaterialTheme.shapes
    
    @Composable
    override fun getSpacing(): OptimusSpacingValues = LocalOptimusSpacing.current
    
    @Composable
    override fun getDensity(): ThemeDensity = LocalOptimusDensity.current
    
    @Composable
    override fun getAccessibilityConfiguration(): AccessibilityConfiguration = 
        LocalAccessibilityConfiguration.current
}
```

### Plugin Theme Integration
```kotlin
// PluginThemeIntegration.kt - How plugins access themes
class PluginThemeManager(
    private val themeProvider: PluginThemeProvider = DefaultPluginThemeProvider()
) {
    @Composable
    fun ProvideThemeToPlugin(
        content: @Composable () -> Unit
    ) {
        // Plugin content automatically inherits all theme properties
        // through composition local providers set up by master application
        content()
    }
    
    @Composable
    fun getComponentDefaults(): PluginComponentDefaults {
        return PluginComponentDefaults(
            buttonDefaults = OptimusButtonDefaults,
            textFieldDefaults = OptimusTextFieldDefaults,
            cardDefaults = OptimusCardDefaults,
            spacingDefaults = LocalOptimusSpacing.current,
            colorDefaults = MaterialTheme.colorScheme.extended
        )
    }
}

data class PluginComponentDefaults(
    val buttonDefaults: Any, // OptimusButtonDefaults
    val textFieldDefaults: Any, // OptimusTextFieldDefaults  
    val cardDefaults: Any, // OptimusCardDefaults
    val spacingDefaults: OptimusSpacingValues,
    val colorDefaults: OptimusExtendedColors
)

// Plugin implementation example
class ServerConfigurationTool : OptimusToolPlugin {
    private lateinit var themeManager: PluginThemeManager
    
    override fun onInitialize(context: ToolContext): InitializationResult {
        themeManager = context.getService<PluginThemeManager>()
        return InitializationResult.Success
    }
    
    @Composable
    override fun renderTool(container: ToolDetailContainer, context: ToolContext) {
        themeManager.ProvideThemeToPlugin {
            // All components automatically use master application theme
            ServerConfigurationContent()
        }
    }
    
    @Composable
    private fun ServerConfigurationContent() {
        val defaults = themeManager.getComponentDefaults()
        
        OptimusCard(
            title = "Network Settings",
            modifier = Modifier.fillMaxWidth(),
            colors = defaults.cardDefaults.colors(), // Automatically themed
            elevation = defaults.cardDefaults.elevation()
        ) {
            OptimusTextField(
                value = serverHost,
                onValueChange = { serverHost = it },
                label = "Host Address",
                colors = defaults.textFieldDefaults.colors() // Automatically themed
            )
            
            OptimusButton(
                onClick = { saveConfiguration() },
                colors = defaults.buttonDefaults.colors() // Automatically themed
            ) {
                Text("Save Configuration")
            }
        }
    }
}
```

## Theme Utilities and Extensions

### Color Utilities
```kotlin
// ColorExtensions.kt - Color manipulation utilities
fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)

fun Color.lighten(amount: Float): Color {
    val factor = 1f + amount
    return Color(
        red = (red * factor).coerceAtMost(1f),
        green = (green * factor).coerceAtMost(1f),
        blue = (blue * factor).coerceAtMost(1f),
        alpha = alpha
    )
}

fun Color.darken(amount: Float): Color {
    val factor = 1f - amount
    return Color(
        red = red * factor,
        green = green * factor,
        blue = blue * factor,
        alpha = alpha
    )
}

fun Color.contrastRatio(other: Color): Double {
    val luminance1 = this.luminance()
    val luminance2 = other.luminance()
    val lighter = maxOf(luminance1, luminance2)
    val darker = minOf(luminance1, luminance2)
    return (lighter + 0.05) / (darker + 0.05)
}

fun Color.luminance(): Double {
    val r = if (red <= 0.03928) red / 12.92 else ((red + 0.055) / 1.055).pow(2.4)
    val g = if (green <= 0.03928) green / 12.92 else ((green + 0.055) / 1.055).pow(2.4)
    val b = if (blue <= 0.03928) blue / 12.92 else ((blue + 0.055) / 1.055).pow(2.4)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

@Composable
fun Color.onColor(): Color {
    val contrastWithWhite = this.contrastRatio(Color.White)
    val contrastWithBlack = this.contrastRatio(Color.Black)
    return if (contrastWithWhite > contrastWithBlack) Color.White else Color.Black
}
```

### Typography Extensions
```kotlin
// TypographyExtensions.kt - Typography manipulation utilities
fun Typography.adjustForDensity(density: ThemeDensity): Typography {
    val multiplier = density.multiplier
    return this.copy(
        displayLarge = displayLarge.copy(fontSize = displayLarge.fontSize * multiplier),
        displayMedium = displayMedium.copy(fontSize = displayMedium.fontSize * multiplier),
        displaySmall = displaySmall.copy(fontSize = displaySmall.fontSize * multiplier),
        headlineLarge = headlineLarge.copy(fontSize = headlineLarge.fontSize * multiplier),
        headlineMedium = headlineMedium.copy(fontSize = headlineMedium.fontSize * multiplier),
        headlineSmall = headlineSmall.copy(fontSize = headlineSmall.fontSize * multiplier),
        titleLarge = titleLarge.copy(fontSize = titleLarge.fontSize * multiplier),
        titleMedium = titleMedium.copy(fontSize = titleMedium.fontSize * multiplier),
        titleSmall = titleSmall.copy(fontSize = titleSmall.fontSize * multiplier),
        bodyLarge = bodyLarge.copy(fontSize = bodyLarge.fontSize * multiplier),
        bodyMedium = bodyMedium.copy(fontSize = bodyMedium.fontSize * multiplier),
        bodySmall = bodySmall.copy(fontSize = bodySmall.fontSize * multiplier),
        labelLarge = labelLarge.copy(fontSize = labelLarge.fontSize * multiplier),
        labelMedium = labelMedium.copy(fontSize = labelMedium.fontSize * multiplier),
        labelSmall = labelSmall.copy(fontSize = labelSmall.fontSize * multiplier)
    )
}
```

### Spacing Extensions
```kotlin
// SpacingExtensions.kt - Spacing manipulation utilities
data class OptimusSpacingValues(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val extraExtraLarge: Dp = 48.dp,
    val huge: Dp = 64.dp,
    
    // Semantic spacing
    val componentPadding: Dp = 16.dp,
    val sectionSpacing: Dp = 24.dp,
    val panelSpacing: Dp = 32.dp,
    val formFieldSpacing: Dp = 16.dp,
    val buttonSpacing: Dp = 8.dp,
    val iconPadding: Dp = 4.dp,
    
    // Layout spacing
    val masterPanelWidth: Dp = 280.dp,
    val toolbarHeight: Dp = 56.dp,
    val headerHeight: Dp = 64.dp,
    val statusBarHeight: Dp = 32.dp,
    val dividerThickness: Dp = 1.dp
)

fun OptimusSpacingValues.adjustForDensity(density: ThemeDensity): OptimusSpacingValues {
    val multiplier = density.multiplier
    return this.copy(
        extraSmall = extraSmall * multiplier,
        small = small * multiplier,
        medium = medium * multiplier,
        large = large * multiplier,
        extraLarge = extraLarge * multiplier,
        extraExtraLarge = extraExtraLarge * multiplier,
        huge = huge * multiplier,
        componentPadding = componentPadding * multiplier,
        sectionSpacing = sectionSpacing * multiplier,
        panelSpacing = panelSpacing * multiplier,
        formFieldSpacing = formFieldSpacing * multiplier,
        buttonSpacing = buttonSpacing * multiplier,
        iconPadding = iconPadding * multiplier
    )
}

// Convenience extension for common spacing patterns
@Composable
fun Modifier.optimusPadding(
    horizontal: Dp = LocalOptimusSpacing.current.medium,
    vertical: Dp = LocalOptimusSpacing.current.medium
): Modifier = this.padding(horizontal = horizontal, vertical = vertical)

@Composable
fun Modifier.optimusContentPadding(): Modifier = 
    this.padding(LocalOptimusSpacing.current.componentPadding)

@Composable
fun Modifier.optimusSectionSpacing(): Modifier = 
    this.padding(bottom = LocalOptimusSpacing.current.sectionSpacing)
```

### Theme-Aware Modifier Extensions
```kotlin
// ModifierExtensions.kt - Theme-aware modifier utilities
@Composable
fun Modifier.optimusBackground(
    color: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = RectangleShape
): Modifier = this.background(color = color, shape = shape)

@Composable
fun Modifier.optimusClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    onClick: () -> Unit
): Modifier = this.clickable(
    enabled = enabled,
    onClickLabel = onClickLabel,
    indication = rememberRipple(
        color = MaterialTheme.colorScheme.primary
    ),
    interactionSource = remember { MutableInteractionSource() },
    onClick = onClick
)

@Composable
fun Modifier.optimusFocusable(): Modifier {
    val focusColor = MaterialTheme.colorScheme.primary
    val accessibilityConfig = LocalAccessibilityConfiguration.current
    
    return this.focusable().drawBehind {
        // Custom focus indicator based on accessibility configuration
        when (accessibilityConfig.focusIndicatorStyle) {
            AccessibilityConfiguration.FocusIndicatorStyle.Bold -> {
                drawRect(
                    color = focusColor,
                    style = Stroke(width = 3.dp.toPx()),
                    size = size
                )
            }
            AccessibilityConfiguration.FocusIndicatorStyle.HighContrast -> {
                drawRect(
                    color = Color.Yellow,
                    style = Stroke(width = 4.dp.toPx()),
                    size = size
                )
            }
            else -> {
                drawRect(
                    color = focusColor,
                    style = Stroke(width = 2.dp.toPx()),
                    size = size
                )
            }
        }
    }
}

@Composable
fun Modifier.optimusElevation(
    elevation: Dp = 1.dp,
    shape: Shape = OptimusShapes.medium
): Modifier = this.shadow(elevation = elevation, shape = shape)

@Composable
fun Modifier.optimusBorder(
    width: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline,
    shape: Shape = OptimusShapes.medium
): Modifier = this.border(width = width, color = color, shape = shape)
```

## Master Application Integration

### Application Theme Setup
```kotlin
// MasterApplicationTheme.kt - Complete application theme setup
class OptimusToolshedApplication {
    private var themeConfiguration by mutableStateOf(loadThemeConfiguration())
    
    @Composable
    fun App() {
        OptimusConfigurableTheme(
            configuration = themeConfiguration,
            darkTheme = isSystemInDarkTheme()
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MasterDetailLayout()
            }
        }
    }
    
    fun updateThemeConfiguration(newConfiguration: OptimusThemeConfiguration) {
        themeConfiguration = newConfiguration
        saveThemeConfiguration(newConfiguration)
    }
    
    private fun loadThemeConfiguration(): OptimusThemeConfiguration {
        return try {
            val configFile = File("config/theme.json")
            if (configFile.exists()) {
                Json.decodeFromString<OptimusThemeConfiguration>(configFile.readText())
            } else {
                OptimusThemeConfiguration.Default
            }
        } catch (e: Exception) {
            OptimusThemeConfiguration.Default
        }
    }
    
    private fun saveThemeConfiguration(configuration: OptimusThemeConfiguration) {
        try {
            val configFile = File("config/theme.json")
            configFile.parentFile.mkdirs()
            configFile.writeText(Json.encodeToString(configuration))
        } catch (e: Exception) {
            // Log error but don't fail application
        }
    }
}

@Composable
private fun MasterDetailLayout() {
    Row(modifier = Modifier.fillMaxSize()) {
        // Master panel
        Surface(
            modifier = Modifier.width(LocalOptimusSpacing.current.masterPanelWidth),
            color = MaterialTheme.colorScheme.extended.masterPanelBackground,
            tonalElevation = 1.dp
        ) {
            NavigationPanel()
        }
        
        // Divider
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(LocalOptimusSpacing.current.dividerThickness),
            color = MaterialTheme.colorScheme.extended.divider
        )
        
        // Detail panel
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.extended.detailPanelBackground
        ) {
            ActiveToolDisplay()
        }
    }
}
```

### Theme Settings Interface
```kotlin
// ThemeSettingsPanel.kt - User interface for theme configuration
@Composable
fun ThemeSettingsPanel(
    configuration: OptimusThemeConfiguration,
    onConfigurationChange: (OptimusThemeConfiguration) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .optimusContentPadding(),
        verticalArrangement = Arrangement.spacedBy(LocalOptimusSpacing.current.large)
    ) {
        item {
            BrandColorsSection(
                brandColors = configuration.brandColors,
                onBrandColorsChange = { newBrandColors ->
                    onConfigurationChange(configuration.copy(brandColors = newBrandColors))
                }
            )
        }
        
        item {
            TypographySection(
                typography = configuration.typography,
                onTypographyChange = { newTypography ->
                    onConfigurationChange(configuration.copy(typography = newTypography))
                }
            )
        }
        
        item {
            DensitySection(
                density = configuration.density,
                onDensityChange = { newDensity ->
                    onConfigurationChange(configuration.copy(density = newDensity))
                }
            )
        }
        
        item {
            AccessibilitySection(
                accessibility = configuration.accessibility,
                onAccessibilityChange = { newAccessibility ->
                    onConfigurationChange(configuration.copy(accessibility = newAccessibility))
                }
            )
        }
        
        item {
            PresetSection(
                currentConfiguration = configuration,
                onPresetSelected = onConfigurationChange
            )
        }
    }
}

@Composable
private fun BrandColorsSection(
    brandColors: BrandColorConfiguration,
    onBrandColorsChange: (BrandColorConfiguration) -> Unit
) {
    OptimusCard(
        title = "Brand Colors",
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LocalOptimusSpacing.current.medium)
        ) {
            ColorPickerField(
                label = "Primary Color",
                color = brandColors.primaryColor,
                onColorChange = { newColor ->
                    onBrandColorsChange(brandColors.copy(primaryColor = newColor))
                }
            )
            
            ColorPickerField(
                label = "Secondary Color",
                color = brandColors.secondaryColor,
                onColorChange = { newColor ->
                    onBrandColorsChange(brandColors.copy(secondaryColor = newColor))
                }
            )
            
            brandColors.brandAccent?.let { accentColor ->
                ColorPickerField(
                    label = "Accent Color",
                    color = accentColor,
                    onColorChange = { newColor ->
                        onBrandColorsChange(brandColors.copy(brandAccent = newColor))
                    }
                )
            }
            
            OptimusButton(
                onClick = {
                    onBrandColorsChange(brandColors.copy(brandAccent = Color.Blue))
                }
            ) {
                Text("Add Accent Color")
            }
        }
    }
}

@Composable
private fun DensitySection(
    density: ThemeDensity,
    onDensityChange: (ThemeDensity) -> Unit
) {
    OptimusCard(
        title = "Layout Density",
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LocalOptimusSpacing.current.small)
        ) {
            ThemeDensity.values().forEach { densityOption ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .optimusClickable { onDensityChange(densityOption) }
                        .optimusPadding(
                            horizontal = LocalOptimusSpacing.current.small,
                            vertical = LocalOptimusSpacing.current.extraSmall
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = density == densityOption,
                        onClick = { onDensityChange(densityOption) }
                    )
                    
                    Spacer(modifier = Modifier.width(LocalOptimusSpacing.current.small))
                    
                    Column {
                        Text(
                            text = densityOption.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${(densityOption.multiplier * 100).toInt()}% size",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPickerField(
    label: String,
    color: Color,
    onColorChange: (Color) -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LocalOptimusSpacing.current.small)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .optimusBackground(color = color, shape = OptimusShapes.small)
                    .optimusBorder(shape = OptimusShapes.small)
                    .optimusClickable { showColorPicker = true }
            )
            
            Text(
                text = "#${color.toHex()}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            OptimusButton(
                onClick = { showColorPicker = true }
            ) {
                Text("Change")
            }
        }
    }
    
    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = color,
            onColorSelected = { newColor ->
                onColorChange(newColor)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

private fun Color.toHex(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return "%02X%02X%02X".format(red, green, blue)
}
```

## Library Distribution and Build Configuration

### Build Configuration
```kotlin
// build.gradle.kts for theme library
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    `maven-publish`
    signing
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
    
    sourceSets {
        val jvmMain by getting {
            dependencies {
                api(compose.desktop.common)
                api(compose.material3)
                api(compose.materialIconsExtended)
                api(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(compose.desktop.uiTestJUnit4)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
                implementation("io.mockk:mockk:1.13.8")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.optimus.toolshed"
            artifactId = "ui-components"
            version = project.version.toString()
            
            from(components["kotlin"])
            
            pom {
                name.set("Optimus Toolshed UI Components")
                description.set("Material Design 3 theme system and component library for Optimus Toolshed")
                url.set("https://github.com/optimus-tools/toolshed-ui-components")
                
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
                
                scm {
                    connection.set("scm:git:git://github.com/optimus-tools/toolshed-ui-components.git")
                    developerConnection.set("scm:git:ssh://github.com/optimus-tools/toolshed-ui-components.git")
                    url.set("https://github.com/optimus-tools/toolshed-ui-components")
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

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}
```

### Version Management Strategy
```kotlin
// Version.kt - Semantic versioning for theme library
data class ThemeLibraryVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val suffix: String? = null
) {
    override fun toString(): String {
        return "$major.$minor.$patch${suffix?.let { "-$it" } ?: ""}"
    }
    
    fun isCompatibleWith(other: ThemeLibraryVersion): Boolean {
        // Major version must match for compatibility
        return this.major == other.major
    }
    
    companion object {
        val CURRENT = ThemeLibraryVersion(1, 0, 0)
        
        // Version compatibility matrix
        val MINIMUM_SUPPORTED = ThemeLibraryVersion(1, 0, 0)
        val MAXIMUM_SUPPORTED = ThemeLibraryVersion(1, 99, 99)
    }
}

// Plugin compatibility checking
interface ThemeCompatibilityChecker {
    fun checkCompatibility(
        pluginRequiredVersion: ThemeLibraryVersion,
        availableVersion: ThemeLibraryVersion
    ): CompatibilityResult
    
    fun getMigrationSteps(
        fromVersion: ThemeLibraryVersion,
        toVersion: ThemeLibraryVersion
    ): List<MigrationStep>
}

sealed class CompatibilityResult {
    object Compatible : CompatibilityResult()
    data class Incompatible(val reason: String) : CompatibilityResult()
    data class RequiresMigration(val steps: List<MigrationStep>) : CompatibilityResult()
}

data class MigrationStep(
    val description: String,
    val automated: Boolean,
    val breaking: Boolean
)
```

### Consumer Integration
```kotlin
// Plugin build.gradle.kts - How plugins consume the theme library
dependencies {
    // Theme library dependency
    implementation("com.optimus.toolshed:ui-components:1.0.0")
    
    // Plugin API (includes theme contracts)
    implementation("com.optimus.toolshed:plugin-api:1.0.0")
    
    // Compose dependencies (provided by theme library transitively)
    // implementation(compose.desktop.common) // Not needed - provided by theme library
    // implementation(compose.material3) // Not needed - provided by theme library
}

// Plugin theme integration verification
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.optimus.toolshed" && requested.name == "ui-components") {
            val requestedVersion = ThemeLibraryVersion.parse(requested.version)
            val currentVersion = ThemeLibraryVersion.CURRENT
            
            if (!currentVersion.isCompatibleWith(requestedVersion)) {
                throw GradleException(
                    "Incompatible theme library version. " +
                    "Plugin requires: ${requested.version}, " +
                    "Available: $currentVersion"
                )
            }
        }
    }
}
```

## Testing and Quality Assurance

### Theme Testing Strategy
```kotlin
// ThemeTest.kt - Comprehensive theme testing
class OptimusThemeTest {
    @Test
    fun `color contrast ratios meet accessibility standards`() {
        val lightColors = OptimusLightColorScheme
        val darkColors = OptimusDarkColorScheme
        
        // Test primary color contrast
        val lightPrimaryContrast = lightColors.primary.contrastRatio(lightColors.onPrimary)
        assertTrue("Light primary contrast too low: $lightPrimaryContrast") {
            lightPrimaryContrast >= 4.5 // WCAG AA standard
        }
        
        val darkPrimaryContrast = darkColors.primary.contrastRatio(darkColors.onPrimary)
        assertTrue("Dark primary contrast too low: $darkPrimaryContrast") {
            darkPrimaryContrast >= 4.5
        }
        
        // Test all color pairs for minimum contrast
        validateColorSchemeContrast(lightColors)
        validateColorSchemeContrast(darkColors)
    }
    
    @Test
    fun `typography scales maintain readability`() {
        val typography = OptimusTypography
        
        // Minimum font sizes for readability
        assertTrue("Body text too small") {
            typography.bodyMedium.fontSize.value >= 14f
        }
        
        assertTrue("Label text too small") {
            typography.labelMedium.fontSize.value >= 12f
        }
        
        // Line height ratios for readability
        val bodyLineHeightRatio = typography.bodyMedium.lineHeight.value / typography.bodyMedium.fontSize.value
        assertTrue("Body line height ratio too small: $bodyLineHeightRatio") {
            bodyLineHeightRatio >= 1.4f
        }
    }
    
    @Test
    fun `density adjustments maintain usability`() {
        val baseSpacing = OptimusSpacing
        val compactSpacing = baseSpacing.adjustForDensity(ThemeDensity.Compact)
        val comfortableSpacing = baseSpacing.adjustForDensity(ThemeDensity.Comfortable)
        
        // Ensure minimum touch target sizes
        assertTrue("Compact button spacing too small") {
            compactSpacing.componentPadding.value >= 8f // Minimum 8dp
        }
        
        assertTrue("Comfortable spacing increases appropriately") {
            comfortableSpacing.componentPadding > baseSpacing.componentPadding
        }
    }
    
    @Test
    fun `theme configuration serialization works correctly`() {
        val config = OptimusThemeConfiguration(
            brandColors = BrandColorConfiguration(
                primaryColor = Color.Blue,
                secondaryColor = Color.Gray
            ),
            density = ThemeDensity.Comfortable
        )
        
        val json = Json.encodeToString(config)
        val deserializedConfig = Json.decodeFromString<OptimusThemeConfiguration>(json)
        
        assertEquals(config, deserializedConfig)
    }
    
    private fun validateColorSchemeContrast(colorScheme: ColorScheme) {
        val colorPairs = listOf(
            colorScheme.primary to colorScheme.onPrimary,
            colorScheme.secondary to colorScheme.onSecondary,
            colorScheme.surface to colorScheme.onSurface,
            colorScheme.background to colorScheme.onBackground,
            colorScheme.error to colorScheme.onError
        )
        
        colorPairs.forEach { (background, foreground) ->
            val contrast = background.contrastRatio(foreground)
            assertTrue("Insufficient contrast: $contrast") {
                contrast >= 4.5 // WCAG AA standard
            }
        }
    }
}

// Visual regression testing
@Composable
class ThemeVisualTest {
    @Test
    fun `component appearance matches design specifications`() = runComposeUiTest {
        setContent {
            OptimusTheme {
                ThemeShowcaseScreen()
            }
        }
        
        // Capture screenshots for visual regression testing
        onRoot().captureToImage().assertAgainstGolden("theme_showcase_light")
    }
    
    @Test
    fun `dark theme appearance matches design specifications`() = runComposeUiTest {
        setContent {
            OptimusTheme(darkTheme = true) {
                ThemeShowcaseScreen()
            }
        }
        
        onRoot().captureToImage().assertAgainstGolden("theme_showcase_dark")
    }
}

@Composable
private fun ThemeShowcaseScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .optimusContentPadding(),
        verticalArrangement = Arrangement.spacedBy(LocalOptimusSpacing.current.medium)
    ) {
        // Show all component variations for visual testing
        Row(horizontalArrangement = Arrangement.spacedBy(LocalOptimusSpacing.current.small)) {
            OptimusButton(onClick = {}) { Text("Primary") }
            OptimusButton(onClick = {}, variant = ButtonVariant.Secondary) { Text("Secondary") }
            OptimusButton(onClick = {}, variant = ButtonVariant.Outline) { Text("Outline") }
        }
        
        OptimusTextField(
            value = "Sample text",
            onValueChange = {},
            label = "Text Field"
        )
        
        OptimusCard(title = "Sample Card") {
            Text("Card content with themed colors and typography")
        }
    }
}
```

### Performance Testing
```kotlin
// ThemePerformanceTest.kt - Theme performance validation
class ThemePerformanceTest {
    @Test
    fun `theme switching performance is acceptable`() {
        val startTime = System.currentTimeMillis()
        
        // Simulate theme switching
        repeat(100) {
            val lightTheme = OptimusLightColorScheme
            val darkTheme = OptimusDarkColorScheme
            // Measure color scheme generation time
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        assertTrue("Theme switching too slow: ${duration}ms") {
            duration < 1000 // Should complete in under 1 second
        }
    }
    
    @Test
    fun `color scheme memory usage is reasonable`() {
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        // Create multiple color schemes
        val colorSchemes = (1..1000).map {
            OptimusLightColorScheme
        }
        
        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = finalMemory - initialMemory
        
        assertTrue("Excessive memory usage: ${memoryIncrease / 1024}KB") {
            memoryIncrease < 10 * 1024 * 1024 // Less than 10MB
        }
    }
}
```

## Documentation and Migration Guidelines

### Theme Implementation Guide
The theme system provides comprehensive support for Material Design 3 principles while accommodating desktop application requirements and plugin architecture needs. Implementation follows established patterns for design token management, component theming, and runtime configuration.

### Migration from Legacy Themes
Organizations migrating from existing theme systems should:
1. **Audit existing color schemes** for Material Design 3 compatibility
2. **Map typography scales** to Material Design type system
3. **Update component implementations** to use theme-aware defaults
4. **Test accessibility compliance** with new color contrast requirements
5. **Validate plugin integration** with updated theme contracts

### Best Practices for Plugin Developers
Plugin developers should:
1. **Always use theme-provided components** rather than custom styling
2. **Respect user accessibility preferences** through theme configuration
3. **Test with multiple density settings** to ensure usability
4. **Follow semantic color usage** (success, warning, error) consistently
5. **Implement proper focus indicators** for keyboard navigation

### Performance Optimization Guidelines
- **Cache theme calculations** for expensive operations
- **Use composition locals efficiently** to avoid unnecessary recomposition
- **Minimize color object creation** through token reuse
- **Implement lazy evaluation** for complex theme configurations
- **Monitor memory usage** in long-running applications

---

*Document Version: 1.0*  
*Last Updated: July 02, 2025*  
*Purpose: Complete technical specification for Optimus Toolshed theme system implementation* = bodyMedium.fontSize * multiplier),
        bodySmall = bodySmall.copy(fontSize = bodySmall.fontSize * multiplier),
        labelLarge = labelLarge.copy(fontSize = labelLarge.fontSize * multiplier),
        labelMedium = labelMedium.copy(fontSize = labelMedium.fontSize * multiplier),
        labelSmall = labelSmall.copy(fontSize = labelSmall.fontSize * multiplier)
    )
}

fun Typography.adjustForConfiguration(
    typographyConfig: TypographyConfiguration,
    density: ThemeDensity
): Typography {
    val baseTypography = if (typographyConfig.fontFamily != OptimusTypographyTokens.PrimaryFontFamily) {
        this.withFontFamily(typographyConfig.fontFamily)
    } else {
        this
    }
    
    val scaledTypography = if (typographyConfig.scaleMultiplier != 1.0f) {
        baseTypography.scale(typographyConfig.scaleMultiplier)
    } else {
        baseTypography
    }
    
    return scaledTypography.adjustForDensity(density)
}

private fun Typography.withFontFamily(fontFamily: FontFamily): Typography {
    return this.copy(
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = titleLarge.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
        labelSmall = labelSmall.copy(fontFamily = fontFamily)
    )
}

private fun Typography.scale(multiplier: Float): Typography {
    return this.copy(
        displayLarge = displayLarge.copy(fontSize = displayLarge.fontSize * multiplier),
        displayMedium = displayMedium.copy(fontSize = displayMedium.fontSize * multiplier),
        displaySmall = displaySmall.copy(fontSize = displaySmall.fontSize * multiplier),
        headlineLarge = headlineLarge.copy(fontSize = headlineLarge.fontSize * multiplier),
        headlineMedium = headlineMedium.copy(fontSize = headlineMedium.fontSize * multiplier),
        headlineSmall = headlineSmall.copy(fontSize = headlineSmall.fontSize * multiplier),
        titleLarge = titleLarge.copy(fontSize = titleLarge.fontSize * multiplier),
        titleMedium = titleMedium.copy(fontSize = titleMedium.fontSize * multiplier),
        titleSmall = titleSmall.copy(fontSize = titleSmall.fontSize * multiplier),
        bodyLarge = bodyLarge.copy(fontSize = bodyLarge.fontSize * multiplier),
        bodyMedium = bodyMedium.copy(fontSize# Optimus Toolshed - Theme System Technical Specification

## Theme System Overview

The Optimus Toolshed theme system establishes a comprehensive Material Design 3 foundation that ensures visual consistency and professional aesthetics across the master application and all plugin tools. Built as a reusable library, the theme system abstracts design decisions into configurable tokens while maintaining strict adherence to Material Design principles adapted for desktop applications. The system operates through a layered architecture where design tokens feed into component specifications, which then provide standardized styling for both core application elements and plugin-contributed interfaces, creating a unified visual experience that administrators can depend on throughout their server management workflows.

The theme implementation leverages Kotlin Compose Desktop's native theming capabilities while extending Material Design 3 specifications to accommodate desktop-specific interaction patterns and information density requirements. Rather than creating custom styling approaches that could fragment the user experience, the theme system builds upon established Material Design foundations while adding desktop-optimized enhancements for professional administrative software. This approach ensures that the application feels familiar to users of modern design systems while providing the sophisticated appearance and interaction patterns expected in enterprise desktop applications.

The library architecture supports both immediate deployment and long-term evolution through a modular design token system that separates color schemes, typography, spacing, and component specifications into independently configurable layers. Organizations can customize brand colors, typography choices, and density preferences without requiring code modifications, while the underlying component library maintains design consistency and accessibility standards. The theme system accommodates future design evolution through versioned design token updates and provides clear migration paths for visual refreshes, ensuring that the administrative interface can evolve with changing design standards while preserving user familiarity and workflow efficiency.

## Visual Design Foundation

### Color System Specification

#### Primary Color Palette
```
Primary Colors (Blue Family):
- Primary/40: #1976D2 (Main brand color)
- Primary/80: #90CAF9 (Light variant for containers)
- Primary/20: #0D47A1 (Dark variant for high contrast)
- Primary/10: #0D47A1 (Darkest variant)
- Primary/90: #E3F2FD (Lightest container)

On-Primary Colors:
- On-Primary: #FFFFFF (Text on primary colors)
- On-Primary-Container: #0D47A1 (Text on primary containers)
```

#### Secondary Color Palette
```
Secondary Colors (Blue-Grey Family):
- Secondary/40: #455A64 (Supporting actions)
- Secondary/80: #B0BEC5 (Light containers)
- Secondary/20: #263238 (Dark containers)
- Secondary/10: #1C2427 (Darkest variant)
- Secondary/90: #F5F7F8 (Lightest container)

On-Secondary Colors:
- On-Secondary: #FFFFFF (Text on secondary colors)
- On-Secondary-Container: #263238 (Text on secondary containers)
```

#### Surface Color System
```
Surface Colors:
- Surface: #FCFCFC (Main background)
- Surface-Dim: #F5F5F5 (Slightly dimmed surface)
- Surface-Bright: #FFFFFF (Elevated surface)
- Surface-Container-Lowest: #FFFFFF (Cards, dialogs)
- Surface-Container: #F7F7F7 (Default containers)
- Surface-Container-High: #F0F0F0 (Elevated containers)
- Surface-Container-Highest: #E8E8E8 (Highest elevation)

On-Surface Colors:
- On-Surface: #1C1B1F (Primary text)
- On-Surface-Variant: #49454F (Secondary text)
- Outline: #79747E (Borders, dividers)
- Outline-Variant: #CAC4D0 (Subtle borders)
```

#### Extended Semantic Colors
```
Success Colors:
- Success: #2E7D32 (Success actions, positive states)
- On-Success: #FFFFFF (Text on success)
- Success-Container: #C8E6C9 (Success backgrounds)
- On-Success-Container: #1B5E20 (Text on success containers)

Warning Colors:
- Warning: #F57C00 (Warning states, caution)
- On-Warning: #FFFFFF (Text on warning)
- Warning-Container: #FFE0B2 (Warning backgrounds)
- On-Warning-Container: #E65100 (Text on warning containers)

Error Colors:
- Error: #D32F2F (Error states, destructive actions)
- On-Error: #FFFFFF (Text on error)
- Error-Container: #FFCDD2 (Error backgrounds)
- On-Error-Container: #B71C1C (Text on error containers)

Info Colors:
- Info: #1976D2 (Information, neutral alerts)
- On-Info: #FFFFFF (Text on info)
- Info-Container: #E3F2FD (Info backgrounds)
- On-Info-Container: #0D47A1 (Text on info containers)
```

#### Desktop-Specific Interface Colors
```
Master Panel Colors:
- Master-Panel-Background: #F8F9FA (Navigation area background)
- Master-Panel-Header: #E9ECEF (Header section)
- Master-Panel-Hover: #E3F2FD (Navigation item hover)
- Master-Panel-Selected: #1976D2 (Selected navigation item)
- Master-Panel-Divider: #DEE2E6 (Section separators)

Detail Panel Colors:
- Detail-Panel-Background: #FFFFFF (Main content area)
- Detail-Panel-Header: #F5F5F5 (Header section)
- Toolbar-Background: #FAFAFA (Toolbar area)
- Content-Area-Background: #FFFFFF (Main content)
- Status-Bar-Background: #F8F9FA (Status information)
```

### Typography System

#### Font Family Specification
```
Primary Font Family: Inter
- Rationale: Excellent readability, professional appearance, comprehensive character set
- Fallback Chain: Inter, "Segoe UI", Roboto, Arial, sans-serif
- Web Font URL: https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700

Alternative Options:
- System Default: Use if Inter unavailable
- Custom Enterprise Font: Configurable via theme settings
```

#### Typography Scale (Material Design 3)
```
Display Styles:
- Display Large: Inter 57sp/64sp, Regular, -0.25sp letter spacing
- Display Medium: Inter 45sp/52sp, Regular, 0sp letter spacing
- Display Small: Inter 36sp/44sp, Regular, 0sp letter spacing

Headline Styles:
- Headline Large: Inter 32sp/40sp, Regular, 0sp letter spacing
- Headline Medium: Inter 28sp/36sp, Regular, 0sp letter spacing
- Headline Small: Inter 24sp/32sp, Regular, 0sp letter spacing

Title Styles:
- Title Large: Inter 22sp/28sp, Regular, 0sp letter spacing
- Title Medium: Inter 16sp/24sp, Medium, 0.15sp letter spacing
- Title Small: Inter 14sp/20sp, Medium, 0.1sp letter spacing

Body Styles:
- Body Large: Inter 16sp/24sp, Regular, 0.5sp letter spacing
- Body Medium: Inter 14sp/20sp, Regular, 0.25sp letter spacing
- Body Small: Inter 12sp/16sp, Regular, 0.4sp letter spacing

Label Styles:
- Label Large: Inter 14sp/20sp, Medium, 0.1sp letter spacing
- Label Medium: Inter 12sp/16sp, Medium, 0.5sp letter spacing
- Label Small: Inter 11sp/16sp, Medium, 0.5sp letter spacing
```

#### Desktop-Optimized Typography Usage
```
Application Usage Mapping:
- App Title: Title Large (22sp, Master panel header)
- Tool Titles: Title Medium (16sp, Detail panel headers)
- Section Headers: Title Small (14sp, Card headers, form sections)
- Body Text: Body Medium (14sp, Form labels, descriptions)
- Input Text: Body Medium (14sp, Text fields, dropdown content)
- Button Text: Label Large (14sp, Action buttons)
- Caption Text: Label Medium (12sp, Helper text, footnotes)
- Menu Items: Body Medium (14sp, Navigation text)
- Status Text: Label Medium (12sp, Status bar, notifications)
```

### Spacing and Layout System

#### Spacing Scale
```
Base Unit: 4dp

Spacing Tokens:
- Space-None: 0dp
- Space-ExtraSmall: 4dp (Icon padding, small gaps)
- Space-Small: 8dp (Component internal spacing)
- Space-Medium: 16dp (Standard component margins)
- Space-Large: 24dp (Section spacing, card margins)
- Space-ExtraLarge: 32dp (Major layout spacing)
- Space-ExtraExtraLarge: 48dp (Panel spacing, major sections)

Responsive Modifiers:
- Compact: 0.75x multiplier (Dense layouts)
- Standard: 1.0x multiplier (Default)
- Comfortable: 1.25x multiplier (Spacious layouts)
```

#### Layout Grid System
```
Master/Detail Layout:
- Master Panel Width: 280dp (fixed)
- Detail Panel: Remaining space (flexible)
- Panel Divider: 1dp
- Master Panel Padding: 16dp
- Detail Panel Padding: 24dp

Component Spacing:
- Card Padding: 16dp
- Form Field Spacing: 16dp vertical
- Button Spacing: 8dp horizontal
- Toolbar Height: 56dp
- Header Height: 64dp
- Status Bar Height: 32dp
```

### Shape and Elevation System

#### Corner Radius Scale
```
Shape Tokens:
- Shape-None: 0dp (No rounding)
- Shape-ExtraSmall: 4dp (Small buttons, chips)
- Shape-Small: 8dp (Cards, text fields)
- Shape-Medium: 12dp (Dialogs, larger cards)
- Shape-Large: 16dp (Bottom sheets, large containers)
- Shape-ExtraLarge: 28dp (Floating action buttons)

Desktop-Specific Shapes:
- Window-Corner: 8dp (Application window)
- Panel-Corner: 0dp (Master/detail panels)
- Card-Corner: 8dp (Standard cards)
- Dialog-Corner: 12dp (Modal dialogs)
- Button-Corner: 8dp (Standard buttons)
```

#### Elevation System
```
Elevation Levels:
- Level-0: 0dp (Flat surfaces, background)
- Level-1: 1dp (Cards, text fields)
- Level-2: 3dp (Floating buttons, hover states)
- Level-3: 6dp (Dialogs, drawers)
- Level-4: 8dp (Navigation drawers)
- Level-5: 12dp (Modal dialogs, tooltips)

Shadow Specifications:
- Color: rgba(0, 0, 0, 0.14) for ambient shadow
- Color: rgba(0, 0, 0, 0.12) for key shadow
- Blur Radius: 2x elevation value
- Offset Y: 1x elevation value
```

## Theme Architecture Implementation

### Core Theme Structure

#### Theme Library Organization
```kotlin
optimus-ui-components/
├── core/
│   ├── foundation/
│   │   ├── ColorTokens.kt           // Design token definitions
│   │   ├── TypographyTokens.kt      // Font and text style tokens
│   │   ├── SpacingTokens.kt         // Spacing scale definitions
│   │   ├── ShapeTokens.kt           // Corner radius and shape tokens
│   │   └── ElevationTokens.kt       // Shadow and elevation specs
│   ├── theme/
│   │   ├── OptimusTheme.kt          // Main theme composition
│   │   ├── OptimusMaterialTheme.kt  // Material Design integration
│   │   ├── OptimusExtendedColors.kt // Extended color system
│   │   └── ThemeDefaults.kt         // Default configurations
│   ├── configuration/
│   │   ├── ThemeConfiguration.kt    // Runtime theme config
│   │   ├── DensityConfiguration.kt  // Layout density options
│   │   └── AccessibilityTheme.kt    // Accessibility enhancements
│   └── extensions/
│       ├── ColorExtensions.kt       // Color utility functions
│       ├── TypographyExtensions.kt  // Typography helpers
│       └── ModifierExtensions.kt    // Theme-aware modifiers
```

#### Design Token Implementation
```kotlin
// ColorTokens.kt - Complete color system
object OptimusColorTokens {
    // Primary color family
    val Primary10 = Color(0xFF002171)
    val Primary20 = Color(0xFF0D47A1)
    val Primary30 = Color(0xFF1565C0)
    val Primary40 = Color(0xFF1976D2)
    val Primary50 = Color(0xFF1E88E5)
    val Primary60 = Color(0xFF42A5F5)
    val Primary70 = Color(0xFF64B5F6)
    val Primary80 = Color(0xFF90CAF9)
    val Primary90 = Color(0xFFE3F2FD)
    val Primary95 = Color(0xFFF1F8FF)
    val Primary99 = Color(0xFFFCFDFF)
    
    // Secondary color family
    val Secondary10 = Color(0xFF1C2427)
    val Secondary20 = Color(0xFF263238)
    val Secondary30 = Color(0xFF37474F)
    val Secondary40 = Color(0xFF455A64)
    val Secondary50 = Color(0xFF546E7A)
    val Secondary60 = Color(0xFF78909C)
    val Secondary70 = Color(0xFF90A4AE)
    val Secondary80 = Color(0xFFB0BEC5)
    val Secondary90 = Color(0xFFE1F5FE)
    val Secondary95 = Color(0xFFF0F9FF)
    val Secondary99 = Color(0xFFFCFEFF)
    
    // Neutral color family
    val Neutral10 = Color(0xFF1C1B1F)
    val Neutral20 = Color(0xFF313033)
    val Neutral30 = Color(0xFF484649)
    val Neutral40 = Color(0xFF605D62)
    val Neutral50 = Color(0xFF787579)
    val Neutral60 = Color(0xFF939094)
    val Neutral70 = Color(0xFFAEAAAE)
    val Neutral80 = Color(0xFFCAC4D0)
    val Neutral90 = Color(0xFFE6E1E5)
    val Neutral95 = Color(0xFFF4EFF4)
    val Neutral99 = Color(0xFFFFFBFE)
    
    // Extended semantic colors
    val Success = Color(0xFF2E7D32)
    val SuccessContainer = Color(0xFFC8E6C9)
    val Warning = Color(0xFFF57C00)
    val WarningContainer = Color(0xFFFFE0B2)
    val Error = Color(0xFFD32F2F)
    val ErrorContainer = Color(0xFFFFCDD2)
    val Info = Color(0xFF1976D2)
    val InfoContainer = Color(0xFFE3F2FD)
    
    // Desktop interface colors
    val MasterPanelBackground = Color(0xFFF8F9FA)
    val MasterPanelHover = Color(0xFFE3F2FD)
    val DetailPanelBackground = Color(0xFFFFFFFF)
    val ToolbarBackground = Color(0xFFFAFAFA)
    val DividerColor = Color(0xFFDEE2E6)
}
```

#### Typography Implementation
```kotlin
// TypographyTokens.kt - Complete typography system
object OptimusTypographyTokens {
    // Font family configuration
    val PrimaryFontFamily = FontFamily(
        Font(resource = "fonts/Inter-Regular.ttf", FontWeight.Normal),
        Font(resource = "fonts/Inter-Medium.ttf", FontWeight.Medium),
        Font(resource = "fonts/Inter-SemiBold.ttf", FontWeight.SemiBold),
        Font(resource = "fonts/Inter-Bold.ttf", FontWeight.Bold)
    )
    
    // Fallback font family
    val FallbackFontFamily = FontFamily.Default
    
    // Font weights
    val Light = FontWeight.Light
    val Regular = FontWeight.Normal
    val Medium = FontWeight.Medium
    val SemiBold = FontWeight.SemiBold
    val Bold = FontWeight.Bold
    
    // Line height multipliers
    val TightLineHeight = 1.2f
    val StandardLineHeight = 1.4f
    val RelaxedLineHeight = 1.6f
}

// Complete Material 3 typography scale
val OptimusTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Regular,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Regular,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Regular,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Regular,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Regular,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Regular,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Regular,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Regular,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Regular,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Regular,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = OptimusTypographyTokens.PrimaryFontFamily,
        fontWeight = OptimusTypographyTokens.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

#### Spacing and Shape Systems
```kotlin
// SpacingTokens.kt - Comprehensive spacing system
object OptimusSpacing {
    // Base spacing scale
    val None = 0.dp
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
    val ExtraLarge = 32.dp
    val ExtraExtraLarge = 48.dp
    val Huge = 64.dp
    
    // Semantic spacing (component-specific)
    val ComponentPadding = Medium
    val SectionSpacing = Large
    val PanelSpacing = ExtraLarge
    val FormFieldSpacing = Medium
    val ButtonSpacing = Small
    val IconPadding = ExtraSmall
    
    // Layout-specific spacing
    val MasterPanelWidth = 280.dp
    val ToolbarHeight = 56.dp
    val HeaderHeight = 64.dp
    val StatusBarHeight = 32.dp
    val DividerThickness = 1.dp
}

// ShapeTokens.kt - Complete shape system
val OptimusShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Extended shapes for desktop components
object OptimusExtendedShapes {
    val WindowCorner = RoundedCornerShape(8.dp)
    val PanelCorner = RoundedCornerShape(0.dp)
    val CardCorner = RoundedCornerShape(8.dp)
    val DialogCorner = RoundedCornerShape(12.dp)
    val ButtonCorner = RoundedCornerShape(8.dp)
    val TextFieldCorner = RoundedCornerShape(8.dp)
    val DropdownCorner = RoundedCornerShape(8.dp)
}
```

### Main Theme Composition

#### Core Theme Implementation
```kotlin
// OptimusTheme.kt - Main theme composition
@Composable
fun OptimusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    density: ThemeDensity = ThemeDensity.Standard,
    accessibility: AccessibilityConfiguration = AccessibilityConfiguration.Default,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> OptimusDarkColorScheme
        else -> OptimusLightColorScheme
    }
    
    val extendedColors = when {
        darkTheme -> OptimusExtendedDarkColors
        else -> OptimusExtendedLightColors
    }
    
    val adjustedTypography = OptimusTypography.adjustForDensity(density)
    val adjustedSpacing = OptimusSpacing.adjustForDensity(density)
    
    CompositionLocalProvider(
        LocalOptimusExtendedColors provides extendedColors,
        LocalOptimusSpacing provides adjustedSpacing,
        LocalOptimusDensity provides density,
        LocalAccessibilityConfiguration provides accessibility
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = adjustedTypography,
            shapes = OptimusShapes,
            content = content
        )
    }
}

// Color scheme definitions
val OptimusLightColorScheme = lightColorScheme(
    primary = OptimusColorTokens.Primary40,
    onPrimary = Color.White,
    primaryContainer = OptimusColorTokens.Primary90,
    onPrimaryContainer = OptimusColorTokens.Primary10,
    
    secondary = OptimusColorTokens.Secondary40,
    onSecondary = Color.White,
    secondaryContainer = OptimusColorTokens.Secondary90,
    onSecondaryContainer = OptimusColorTokens.Secondary10,
    
    tertiary = OptimusColorTokens.Primary60,
    onTertiary = Color.White,
    tertiaryContainer = OptimusColorTokens.Primary90,
    onTertiaryContainer = OptimusColorTokens.Primary20,
    
    error = OptimusColorTokens.Error,
    onError = Color.White,
    errorContainer = OptimusColorTokens.ErrorContainer,
    onErrorContainer = OptimusColorTokens.Error,
    
    background = OptimusColorTokens.Neutral99,
    onBackground = OptimusColorTokens.Neutral10,
    
    surface = OptimusColorTokens.Neutral99,
    onSurface = OptimusColorTokens.Neutral10,
    surfaceVariant = OptimusColorTokens.Neutral90,
    onSurfaceVariant = OptimusColorTokens.Neutral30,
    
    outline = OptimusColorTokens.Neutral50,
    outlineVariant = OptimusColorTokens.Neutral80,
    
    scrim = Color.Black,
    inverseSurface = OptimusColorTokens.Neutral20,
    inverseOnSurface = OptimusColorTokens.Neutral95,
    inversePrimary = OptimusColorTokens.Primary80,
    
    surfaceDim = OptimusColorTokens.Neutral95,
    surfaceBright = OptimusColorTokens.Neutral99,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = OptimusColorTokens.Neutral95,
    surfaceContainer = OptimusColorTokens.Neutral90,
    surfaceContainerHigh = OptimusColorTokens.Neutral80,
    surfaceContainerHighest = OptimusColorTokens.Neutral70
)

val OptimusDarkColorScheme = darkColorScheme(
    primary = OptimusColorTokens.Primary80,
    onPrimary = OptimusColorTokens.Primary20,
    primaryContainer = OptimusColorTokens.Primary30,
    onPrimaryContainer = OptimusColorTokens.Primary90,
    
    secondary = OptimusColorTokens.Secondary80,
    onSecondary = OptimusColorTokens.Secondary20,
    secondaryContainer = OptimusColorTokens.Secondary30,
    onSecondaryContainer = OptimusColorTokens.Secondary90,
    
    tertiary = OptimusColorTokens.Primary70,
    onTertiary = OptimusColorTokens.Primary30,
    tertiaryContainer = OptimusColorTokens.Primary40,
    onTertiaryContainer = OptimusColorTokens.Primary90,
    
    error = OptimusColorTokens.Error,
    onError = Color.White,
    errorContainer = OptimusColorTokens.ErrorContainer,
    onErrorContainer = OptimusColorTokens.Error,
    
    background = OptimusColorTokens.Neutral10,
    onBackground = OptimusColorTokens.Neutral90,
    
    surface = OptimusColorTokens.Neutral10,
    onSurface = OptimusColorTokens.Neutral90,
    surfaceVariant = OptimusColorTokens.Neutral30,
    onSurfaceVariant = OptimusColorTokens.Neutral80,
    
    outline = OptimusColorTokens.Neutral60,
    outlineVariant = OptimusColorTokens.Neutral30,
    
    scrim = Color.Black,
    inverseSurface = OptimusColorTokens.Neutral90,
    inverseOnSurface = OptimusColorTokens.Neutral20,
    inversePrimary = OptimusColorTokens.Primary40,
    
    surfaceDim = OptimusColorTokens.Neutral10,
    surfaceBright = OptimusColorTokens.Neutral20,
    surfaceContainerLowest = OptimusColorTokens.Neutral10,
    surfaceContainerLow = OptimusColorTokens.Neutral20,
    surfaceContainer = OptimusColorTokens.Neutral30,
    surfaceContainerHigh = OptimusColorTokens.Neutral40,
    surfaceContainerHighest = OptimusColorTokens.Neutral50
)
```

### Extended Color System

#### Extended Colors Implementation
```kotlin
// OptimusExtendedColors.kt - Extended color system
data class OptimusExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    
    // Desktop-specific interface colors
    val masterPanelBackground: Color,
    val masterPanelHover: Color,
    val masterPanelSelected: Color,
    val masterPanelDivider: Color,
    val detailPanelBackground: Color,
    val detailPanelHeader: Color,
    val toolbarBackground: Color,
    val statusBarBackground: Color,
    val contentAreaBackground: Color,
    val divider: Color,
    val focusRing: Color,
    val dropShadow: Color
)

val OptimusExtendedLightColors = OptimusExtendedColors(
    success = OptimusColorTokens.Success,
    onSuccess = Color.White,
    successContainer = OptimusColorTokens.SuccessContainer,
    onSuccessContainer = OptimusColorTokens.Success,
    
    warning = OptimusColorTokens.Warning,
    onWarning = Color.White,
    warningContainer = OptimusColorTokens.WarningContainer,
    onWarningContainer = OptimusColorTokens.Warning,
    
    info = OptimusColorTokens.Info,
    onInfo = Color.White,
    infoContainer = OptimusColorTokens.InfoContainer,
    onInfoContainer = OptimusColorTokens.Info,
    
    masterPanelBackground = OptimusColorTokens.MasterPanelBackground,
    masterPanelHover = OptimusColorTokens.MasterPanelHover,
    masterPanelSelected = OptimusColorTokens.Primary40,
    masterPanelDivider = OptimusColorTokens.DividerColor,
    detailPanelBackground = OptimusColorTokens.DetailPanelBackground,
    detailPanelHeader = Color(0xFFF5F5F5),
    toolbarBackground = OptimusColorTokens.ToolbarBackground,
    statusBarBackground = Color(0xFFF8F9FA),
    contentAreaBackground = Color.White,
    divider = OptimusColorTokens.DividerColor,
    focusRing = OptimusColorTokens.Primary40,
    dropShadow = Color.Black.copy(alpha = 0.14f)
)

val OptimusExtendedDarkColors = OptimusExtendedColors(
    success = Color(0xFF4CAF50),
    onSuccess = Color.Black,
    successContainer = Color(0xFF1B5E20),
    onSuccessContainer = Color(0xFFC8E6C9),
    
    warning = Color(0xFFFF9800),
    onWarning = Color.Black,
    warningContainer = Color(0xFFE65100),
    onWarningContainer = Color(0xFFFFE0B2),
    
    info = OptimusColorTokens.Primary70,
    onInfo = Color.Black,
    infoContainer = OptimusColorTokens.Primary30,
    onInfoContainer = OptimusColorTokens.Primary90,
    
    masterPanelBackground = Color(0xFF1E1E1E),
    masterPanelHover = Color(0xFF2A2A2A),
    masterPanelSelected = OptimusColorTokens.Primary60,
    masterPanelDivider = Color(0xFF3E3E3E),
    detailPanelBackground = Color(0xFF121212),
    detailPanelHeader = Color(0xFF1E1E1E),
    toolbarBackground = Color(0xFF1A1A1A),
    statusBarBackground = Color(0xFF1E1E1E),
    contentAreaBackground = Color(0xFF121212),
    divider = Color(0xFF3E3E3E),
    focusRing = OptimusColorTokens.Primary60,
    dropShadow = Color.Black.copy(alpha = 0.25f)
)

// Composition local providers
val LocalOptimusExtendedColors = staticCompositionLocalOf<OptimusExtendedColors> {
    error("No extended colors provided")
}

val LocalOptimusSpacing = staticCompositionLocalOf<OptimusSpacingValues> {
    error("No spacing provided")
}

val LocalOptimusDensity = staticCompositionLocalOf<ThemeDensity> {
    ThemeDensity.Standard
}

val LocalAccessibilityConfiguration = staticCompositionLocalOf<AccessibilityConfiguration> {
    AccessibilityConfiguration.Default
}

// Extension property for easy access
val ColorScheme.extended: OptimusExtendedColors
    @Composable
    get() = LocalOptimusExtendedColors.current
```