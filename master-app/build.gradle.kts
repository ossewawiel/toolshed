/*
 * Optimus Toolshed - Master Application
 * 
 * Main desktop application implementing the master/detail interface and
 * orchestrating the complete plugin ecosystem.
 * 
 * Architecture: Top-level application depending on all shared libraries
 */

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm {
        jvmToolchain(21)
        
        withJava()
        
        // Application main class configuration
        mainRun {
            mainClass.set("com.optimus.toolshed.MainKt")
        }
    }
    
    sourceSets {
        val jvmMain by getting {
            dependencies {
                // All shared modules
                implementation(project(":shared-theme"))
                implementation(project(":i18n-library"))
                implementation(project(":ui-components"))
                implementation(project(":plugin-api"))
                
                // Complete desktop application stack
                implementation(libs.bundles.compose.desktop)
                implementation(libs.bundles.kotlinx)
                
                // Plugin architecture and DI
                implementation(libs.bundles.plugin.architecture)
                
                // Configuration management
                implementation(libs.bundles.configuration)
                
                // File I/O for plugin discovery
                implementation(libs.bundles.file.io)
                
                // Logging framework
                implementation(libs.bundles.logging)
                
                // Development tools (debug configuration not available in multiplatform)
                implementation(libs.bundles.compose.tooling)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.testing.unit)
                implementation(libs.bundles.testing.ui)
                
                // Integration testing for plugin system
                implementation(libs.testng)
            }
        }
    }
}

// Application run configuration (will be configured after first successful run)
// tasks.named<JavaExec>("jvmRun") {
//     jvmArgs = listOf(
//         "-Xms512m",
//         "-Xmx2g", 
//         "-XX:+UseG1GC",
//         "-Dfile.encoding=UTF-8"
//     )
// }

// Desktop application configuration
compose.desktop {
    application {
        mainClass = "com.optimus.toolshed.MainKt"
        
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            
            packageName = "Optimus Toolshed"
            packageVersion = "1.0.0"
            description = "Unified administrative interface for Optimus Server management"
            copyright = "© 2025 Optimus Project"
            vendor = "Optimus Project"
            
            windows {
                iconFile.set(file("src/jvmMain/resources/icons/app-icon.ico"))
                dirChooser = true
                perUserInstall = true
            }
            
            linux {
                iconFile.set(file("src/jvmMain/resources/icons/app-icon.png"))
            }
            
            macOS {
                iconFile.set(file("src/jvmMain/resources/icons/app-icon.icns"))
                bundleID = "com.optimus.toolshed"
            }
        }
    }
}

// Compose-specific compiler options
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
}