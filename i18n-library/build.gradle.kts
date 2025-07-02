/*
 * Optimus Toolshed - Internationalization Library
 * 
 * Core internationalization module providing locale management, resource loading,
 * and localization services for all application components.
 * 
 * Architecture: Foundation module supporting external file-based localization
 */

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm {
        jvmToolchain(21)
    }
    
    sourceSets {
        val jvmMain by getting {
            dependencies {
                // Core Kotlin libraries
                api(libs.bundles.kotlinx)
                
                // Internationalization support
                api(libs.bundles.i18n)
                
                // File I/O for resource loading
                implementation(libs.bundles.file.io)
                
                // Configuration management for locale settings
                implementation(libs.bundles.configuration)
                
                // Logging for debugging localization issues
                implementation(libs.bundles.logging)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.testing.unit)
                
                // Additional test resources for locale testing
                implementation(libs.apache.commons.io)
            }
        }
    }
}

// Configure resource processing for JVM target
tasks.named<ProcessResources>("jvmProcessResources") {
    from("src/jvmMain/resources") {
        include("**/*.properties")
        include("**/*.yml")
        include("**/*.yaml")
    }
}