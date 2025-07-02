/*
 * Optimus Toolshed - UI Component Library
 * 
 * Shared component library implementing Material Design 3 components with
 * consistent theming and internationalization for desktop applications.
 * 
 * Architecture: Depends on shared-theme and i18n-library for foundation services
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
                // Theme foundation
                api(project(":shared-theme"))
                
                // Internationalization support
                api(project(":i18n-library"))
                
                // Complete Compose desktop stack
                api(libs.bundles.compose.desktop)
                
                // Core Kotlin libraries
                implementation(libs.bundles.kotlinx)
                
                // Validation and form handling
                implementation(libs.apache.commons.text)
                
                // Logging for component debugging
                implementation(libs.bundles.logging)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.testing.unit)
                implementation(libs.bundles.testing.ui)
                
                // Compose testing tools
                implementation(libs.bundles.compose.tooling)
            }
        }
    }
}

// Compose-specific configuration
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"
        )
    }
}