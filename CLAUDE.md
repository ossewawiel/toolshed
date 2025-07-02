# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Optimus Toolshed is a desktop application built with Kotlin Compose Multiplatform that provides a unified master/detail interface for managing Optimus Server operations. The application consolidates scattered command-line tools into a single, visually consistent desktop interface while preserving the full power of underlying utilities.

## Technology Stack

- **Language**: Kotlin 1.9.24
- **UI Framework**: Compose Multiplatform 1.6.11 for Desktop
- **Runtime**: OpenJDK 21 LTS
- **Build System**: Gradle 8.8
- **Target Platform**: Windows 10/11 (x64)

## Architecture

The application follows a **plugin-based modular architecture** where individual management tools are implemented as separate libraries with standardized integration APIs. Key architectural principles:

- **Master/Detail Interface**: Hierarchical view of management functions (master) with dynamic detail panels
- **Plugin System**: Runtime plugin discovery and loading through Java Service Provider Interface
- **Service Discovery**: Plugins expose capabilities through common interfaces
- **Dependency Injection**: Koin 3.5.6 for service registration and plugin context management

## Development Commands

Based on the tech stack specification, the following commands should be available:

### Build Commands
```bash
# Build the application
./gradlew build

# Run the application
./gradlew run

# Create distributable package
./gradlew jpackage
```

### Code Quality
```bash
# Static analysis
./gradlew detekt

# Code formatting
./gradlew ktlintFormat

# Check formatting
./gradlew ktlintCheck

# Generate documentation
./gradlew dokkaHtml
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew jacocoTestReport

# Run UI tests
./gradlew testClasses
```

## Key Libraries and Dependencies

### Core Framework
- **Kotlinx Coroutines 1.8.1**: Async operations and structured concurrency
- **Kotlinx Serialization 1.7.1**: Configuration persistence and plugin metadata
- **Compose Multiplatform 1.6.11**: Declarative UI with Material Design 3

### Plugin Architecture
- **Java Service Provider Interface**: Plugin discovery mechanism
- **Koin 3.5.6**: Lightweight dependency injection
- **ASM 9.7**: Bytecode analysis for plugin validation

### Testing Framework
- **JUnit 5.10.2**: Unit testing
- **MockK 1.13.11**: Kotlin-native mocking
- **Kotest 5.9.1**: Kotlin-idiomatic assertions
- **Compose UI Test 1.6.11**: UI testing

### Utilities
- **Okio 3.9.0**: Efficient I/O operations
- **Typesafe Config 1.4.3**: Hierarchical configuration management
- **ICU4J 75.1**: Internationalization and locale support
- **SLF4J 2.0.13 + Logback 1.5.6**: Logging framework

## Performance Requirements

- **Startup Time**: <3 seconds from launch to usable UI
- **Memory Usage**: <100MB baseline, <50MB per plugin
- **UI Responsiveness**: 60 FPS rendering target

## Security Requirements

- Plugin signature verification required for production
- OWASP Dependency Check on every build
- No HIGH+ severity vulnerabilities allowed
- Only trusted repositories (Maven Central, Google, JetBrains)

## Plugin Development

Plugins must:
1. Implement standardized integration APIs
2. Provide Compose UI components following design system guidelines
3. Register through Java Service Provider Interface
4. Be signed for production deployment
5. Adhere to memory and performance constraints

## Current Development Status

**Current Phase**: Phase 1 - Project Foundation  
**Next Step**: Initialize Git repository with .gitignore (Task 1)  
**Last Completed**: Project structure analysis and documentation setup

### Phase 1 Task List (Current)
1. **Initialize Git repository with .gitignore** - PENDING
2. **Create basic project directory structure** - PENDING  
3. **Create settings.gradle.kts with module definitions** - PENDING
4. **Create root build.gradle.kts with version catalog** - PENDING
5. **Create gradle.properties with JVM and build settings** - PENDING
6. **Create gradle/libs.versions.toml with dependency versions** - PENDING
7. **Verify Gradle wrapper functionality** - PENDING
8. **Create all module directories and basic build files** - PENDING
9. **Verify all modules compile successfully** - PENDING
10. **Create basic GitHub Actions CI pipeline** - PENDING
11. **Configure inter-module dependencies** - PENDING
12. **Create minimal source structure for each module** - PENDING
13. **Create basic Compose application entry point** - PENDING
14. **Test complete Phase 1 integration and verification** - PENDING

### Quick Start Prompts
Use these specific prompts to guide implementation:

**For Task 1:**
```
Initialize Git repository with .gitignore for Kotlin/Gradle/IntelliJ project. 
Reference: docs/project_steps_breakdown.md:21-24
Create appropriate .gitignore file for Kotlin Compose Multiplatform desktop application.
```

**For Task 2:**
```
Create basic project directory structure for Optimus Toolshed.
Reference: docs/project_steps_breakdown.md:35-45
Create the following module structure: shared-theme/, i18n-library/, ui-components/, plugin-api/, master-app/, tools/server-configuration/
```

## Documentation Location

Project documentation is located in `/docs/` directory:
- `optimus_architecture.md`: Core architecture overview
- `tech_stack_spec.md`: Complete technical specifications
- `optimus_system_introduction.md`: System overview and user flows
- `project_steps_breakdown.md`: Detailed implementation steps with Claude prompts
- Additional specifications for UI, testing, and plugin development

## Development Workflow

1. Check current development status in this file
2. Use the provided Claude prompts for specific tasks
3. Update task status as work progresses
4. Move to next phase when all Phase 1 tasks complete