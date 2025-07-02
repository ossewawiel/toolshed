/*
 * Optimus Toolshed - Server Configuration Plugin
 * 
 * Default plugin implementation providing server configuration management
 * functionality within the Optimus Toolshed ecosystem.
 * 
 * Architecture: Plugin implementation depending on plugin-api and ui-components
 */

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm {
        jvmToolchain(21)
    }
    
    sourceSets {
        val jvmMain by getting {
            dependencies {
                // Plugin architecture
                api(project(":plugin-api"))
                
                // UI component library for plugin interface
                implementation(project(":ui-components"))
                
                // Direct theme access for custom components
                implementation(project(":shared-theme"))
                
                // I18n for plugin-specific text
                implementation(project(":i18n-library"))
                
                // Compose for plugin UI
                implementation(libs.bundles.compose.desktop)
                
                // Core Kotlin libraries
                implementation(libs.bundles.kotlinx)
                
                // Configuration management for server settings
                implementation(libs.bundles.configuration)
                
                // File I/O for configuration persistence
                implementation(libs.bundles.file.io)
                
                // Validation and text processing
                implementation(libs.apache.commons.text)
                
                // Logging
                implementation(libs.bundles.logging)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.testing.unit)
                implementation(libs.bundles.testing.ui)
                
                // Plugin testing support
                implementation(project(":plugin-api"))
            }
        }
    }
}

// Plugin-specific configuration
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
}

// Plugin manifest generation
tasks.named<ProcessResources>("jvmProcessResources") {
    from("src/jvmMain/resources") {
        include("META-INF/plugin.json")
        expand(project.properties)
    }
}