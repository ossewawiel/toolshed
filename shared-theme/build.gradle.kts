/*
 * Optimus Toolshed - Shared Theme Module
 * 
 * Foundation module providing Material Design 3 theme system, design tokens,
 * and theming contracts for the entire application ecosystem.
 * 
 * Architecture: Core foundation module with no dependencies on other app modules
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
                // Compose foundation for theming
                api(libs.bundles.compose.desktop)
                
                // Serialization for theme configuration persistence
                implementation(libs.bundles.kotlinx)
                
                // Color manipulation and theme utilities
                implementation(libs.kotlin.reflect)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.testing.unit)
            }
        }
    }
}

// Module-specific configuration
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }
}