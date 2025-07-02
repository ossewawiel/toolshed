/*
 * Optimus Toolshed - Gradle Settings Configuration
 * 
 * Multi-module Kotlin Compose Multiplatform Desktop Application
 * Cross-platform compatible: Windows 10/11, Linux, macOS
 */

// Enable Gradle features
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Plugin management for version catalogs and plugin resolution
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

// Dependency resolution management
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

// Root project configuration
rootProject.name = "optimus-toolshed"

// Core shared modules
include(":shared-theme")
include(":i18n-library") 
include(":ui-components")
include(":plugin-api")

// Main application module
include(":master-app")

// Plugin modules (nested under tools)
include(":tools:server-configuration")

// Project structure validation
gradle.projectsLoaded {
    val expectedModules = setOf(
        ":shared-theme",
        ":i18n-library", 
        ":ui-components",
        ":plugin-api",
        ":master-app",
        ":tools:server-configuration"
    )
    
    val actualModules = rootProject.allprojects
        .filter { it != rootProject }
        .map { it.path }
        .toSet()
    
    val missingModules = expectedModules - actualModules
    if (missingModules.isNotEmpty()) {
        throw GradleException("Missing modules: $missingModules")
    }
    
    println("✓ All ${expectedModules.size} modules loaded successfully")
}