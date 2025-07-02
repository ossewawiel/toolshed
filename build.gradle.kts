/*
 * Optimus Toolshed - Root Build Configuration
 * 
 * Multi-module Kotlin Compose Multiplatform Desktop Application
 * Reference: docs/tech_stack_spec.md:54-62, docs/tech_stack_spec.md:399-408
 */

plugins {
    // Apply to all subprojects
    kotlin("multiplatform") version "1.9.24" apply false
    id("org.jetbrains.compose") version "1.6.11" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24" apply false
    
    // Code quality and documentation
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false
    
    // Security scanning
    id("org.owasp.dependencycheck") version "9.2.0"
    
    // Dependency management
    id("com.github.ben-manes.versions") version "0.51.0"
}

// Configure all subprojects
allprojects {
    group = "com.optimus.toolshed"
    version = "1.0.0-SNAPSHOT"
    
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://repo1.maven.org/maven2/")
    }
}

// Common configuration for all subprojects
subprojects {
    // Apply JVM toolchain to all projects
    afterEvaluate {
        if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
                jvmToolchain(21)
            }
        }
    }
    
    // Apply common plugin configurations
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.dokka")
    
    // Configure Detekt for all modules
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
    }
    
    // Configure Dokka for all modules (will be configured per module)
    tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
        moduleName.set(project.name)
        moduleVersion.set(project.version.toString())
    }
    
    // Configure common test settings
    tasks.withType<Test> {
        useJUnitPlatform()
        
        // Test JVM arguments for performance
        jvmArgs = listOf(
            "-XX:+UseG1GC",
            "-XX:MaxMetaspaceSize=512m",
            "-Xmx2g"
        )
        
        // Test reporting
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = false
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
        
        // Parallel test execution
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    }
    
    // Configure compilation settings
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs += listOf(
                "-Xjsr305=strict",
                "-Xcontext-receivers",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
            )
        }
    }
}

// OWASP Dependency Check configuration
configure<org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension> {
    formats = listOf("ALL")
    failBuildOnCVSS = 7.0f  // Fail on HIGH (7.0+) and CRITICAL (9.0+) vulnerabilities
    
    // Skip non-Java analyzers for better performance
    skipConfigurations = listOf("detekt")
}

// Root project tasks
tasks.register("cleanAll") {
    description = "Clean all modules including build caches"
    group = "build"
    
    subprojects.forEach { project ->
        dependsOn("${project.path}:clean")
    }
    
    doLast {
        delete(layout.buildDirectory)
        println("✓ All modules cleaned successfully")
    }
}

tasks.register("buildAll") {
    description = "Build all modules"
    group = "build"
    
    subprojects.forEach { project ->
        dependsOn("${project.path}:build")
    }
    
    doLast {
        println("✓ All modules built successfully")
    }
}

tasks.register("checkAll") {
    description = "Run all quality checks across all modules"
    group = "verification"
    
    subprojects.forEach { project ->
        dependsOn("${project.path}:check")
        dependsOn("${project.path}:detekt")
    }
    
    doLast {
        println("✓ All quality checks passed")
    }
}

// Performance and build optimizations
tasks.withType<org.gradle.api.tasks.wrapper.Wrapper> {
    gradleVersion = "8.8"
    distributionType = org.gradle.api.tasks.wrapper.Wrapper.DistributionType.BIN
}