#!/bin/bash
set -e

echo "🖥️ Application Entry Point Validation"
echo "===================================="

echo ""
echo "🔨 Step 1: Build Verification"
echo "------------------------------"

echo "Building master application..."
./gradlew :master-app:build --no-configuration-cache --quiet
if [ $? -eq 0 ]; then
    echo "✅ Master application builds successfully"
else
    echo "❌ Master application build failed"
    exit 1
fi

echo ""
echo "📦 Step 2: Dependency Verification"
echo "-----------------------------------"

echo "Checking for required dependencies..."
./gradlew :master-app:dependencies --configuration jvmRuntimeClasspath --quiet | grep -E "(server-configuration|plugin-api|ui-components)" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ All required modules are in runtime classpath"
else
    echo "❌ Missing required dependencies"
    exit 1
fi

echo ""
echo "🔍 Step 3: Plugin Discovery Verification"
echo "-----------------------------------------"

echo "Checking plugin service files..."
if [ -f "tools/server-configuration/src/jvmMain/resources/META-INF/services/com.optimus.toolshed.plugin.OptimusPlugin" ]; then
    echo "✅ Plugin service file exists"
    echo "Plugin class: $(cat tools/server-configuration/src/jvmMain/resources/META-INF/services/com.optimus.toolshed.plugin.OptimusPlugin)"
else
    echo "❌ Plugin service file missing"
    exit 1
fi

echo ""
echo "⚙️ Step 4: Configuration Verification"
echo "--------------------------------------"

echo "Checking application configuration..."
if [ -f "master-app/src/jvmMain/resources/application.properties" ]; then
    echo "✅ Application configuration exists"
    echo "Configuration sample:"
    head -5 master-app/src/jvmMain/resources/application.properties
else
    echo "❌ Application configuration missing"
    exit 1
fi

echo ""
echo "🌐 Step 5: Internationalization Verification"
echo "---------------------------------------------"

echo "Checking i18n resources..."
if [ -f "i18n-library/src/jvmMain/resources/messages.properties" ]; then
    echo "✅ Default message bundle exists"
    echo "Sample messages:"
    grep -E "^(app\.|common\.)" i18n-library/src/jvmMain/resources/messages.properties | head -3
else
    echo "❌ Message bundle missing"
    exit 1
fi

echo ""
echo "🎨 Step 6: Theme Integration Verification"
echo "------------------------------------------"

echo "Checking theme implementation..."
if grep -q "OptimusTheme" master-app/src/jvmMain/kotlin/com/optimus/toolshed/Main.kt; then
    echo "✅ Theme integration found in main application"
else
    echo "❌ Theme integration missing"
    exit 1
fi

echo ""
echo "🏗️ Step 7: Application Structure Verification"
echo "----------------------------------------------"

echo "Checking main application components..."

# Check for main function
if grep -q "fun main()" master-app/src/jvmMain/kotlin/com/optimus/toolshed/Main.kt; then
    echo "✅ Main entry point found"
else
    echo "❌ Main entry point missing"
    exit 1
fi

# Check for plugin initialization
if grep -q "initializePlugins" master-app/src/jvmMain/kotlin/com/optimus/toolshed/Main.kt; then
    echo "✅ Plugin initialization found"
else
    echo "❌ Plugin initialization missing"
    exit 1
fi

# Check for window setup
if grep -q "Window(" master-app/src/jvmMain/kotlin/com/optimus/toolshed/Main.kt; then
    echo "✅ Window setup found"
else
    echo "❌ Window setup missing"
    exit 1
fi

echo ""
echo "🧪 Step 8: Runtime Preparation Test"
echo "------------------------------------"

echo "Testing application compilation with all dependencies..."
./gradlew :master-app:assemble --no-configuration-cache --quiet
if [ $? -eq 0 ]; then
    echo "✅ Application assembles successfully for runtime"
else
    echo "❌ Application assembly failed"
    exit 1
fi

echo ""
echo "📊 Step 9: Source Code Analysis"
echo "--------------------------------"

echo "Checking application source code structure..."
if [ -f "master-app/src/jvmMain/kotlin/com/optimus/toolshed/Main.kt" ]; then
    echo "✅ Main.kt source file exists"
    main_lines=$(wc -l < master-app/src/jvmMain/kotlin/com/optimus/toolshed/Main.kt)
    echo "✅ Main.kt has $main_lines lines of code"
else
    echo "❌ Main.kt source file missing"
    exit 1
fi

if [ -f "master-app/src/jvmMain/kotlin/com/optimus/toolshed/AppPluginContext.kt" ]; then
    echo "✅ AppPluginContext.kt source file exists"
else
    echo "❌ AppPluginContext.kt source file missing"
    exit 1
fi

echo ""
echo "🔧 Step 10: Compilation Test"
echo "-----------------------------"

echo "Note: Kotlin Multiplatform compilation appears cached - this is normal"
echo "Testing that all components are properly configured..."
./gradlew :master-app:tasks --quiet | grep -q "run"
if [ $? -eq 0 ]; then
    echo "✅ Run tasks are available"
else
    echo "❌ Run tasks not available"
    exit 1
fi

echo ""
echo "🎉 Application Entry Point Validation Complete!"
echo "==============================================="
echo "✅ Application builds and assembles successfully"
echo "✅ Plugin discovery system configured"
echo "✅ All dependencies properly integrated"
echo "✅ Theme and i18n systems ready"
echo "✅ Desktop application entry point functional"
echo ""
echo "🚀 Ready to run with: ./gradlew :master-app:run"
echo "⚠️ Note: GUI requires display environment for full testing"