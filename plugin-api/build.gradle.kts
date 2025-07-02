/*
 * Optimus Toolshed - Plugin API
 * 
 * Core plugin architecture module defining contracts, interfaces, and service
 * provider framework for the plugin system.
 * 
 * Architecture: Foundation module for plugin development with DI and service contracts
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
                // Theme contracts for plugin UI consistency
                api(project(":shared-theme"))
                
                // Core Kotlin libraries
                api(libs.bundles.kotlinx)
                
                // Dependency injection framework
                api(libs.bundles.plugin.architecture)
                
                // Configuration management for plugin settings
                api(libs.bundles.configuration)
                
                // Logging framework for plugin debugging
                api(libs.bundles.logging)
                
                // File I/O for plugin loading and resource management
                implementation(libs.bundles.file.io)
                
                // Reflection for plugin discovery and instantiation
                implementation(libs.kotlin.reflect)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.testing.unit)
                
                // Plugin testing utilities
                implementation(libs.asm.util)
            }
        }
    }
}

// Plugin development configuration
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
}