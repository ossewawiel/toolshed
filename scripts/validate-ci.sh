#!/bin/bash
set -e

echo "🚀 Starting CI Pipeline Validation"
echo "=================================="

# Check Java version
echo "☕ Java Version:"
java --version | head -1

# Check Gradle version
echo "🔨 Gradle Version:"
./gradlew --version | grep "Gradle"

echo ""
echo "📦 Step 1: Build Validation"
echo "----------------------------"
./gradlew build --no-configuration-cache
echo "✅ Build completed successfully"

echo ""
echo "🧪 Step 2: Test Execution"
echo "--------------------------"
./gradlew test --no-configuration-cache
echo "✅ Tests completed successfully"

echo ""
echo "🔍 Step 3: Code Quality Analysis"
echo "---------------------------------"
./gradlew detekt --no-configuration-cache
echo "✅ Detekt analysis completed"

echo ""
echo "📚 Step 4: Documentation Generation"
echo "------------------------------------"
./gradlew dokkaHtml --no-configuration-cache > /dev/null 2>&1
echo "✅ Documentation generated successfully"

echo ""
echo "🔧 Step 5: Individual Module Validation"
echo "----------------------------------------"
modules=("shared-theme" "i18n-library" "ui-components" "plugin-api" "master-app" "tools:server-configuration")

for module in "${modules[@]}"; do
    echo "Testing module: $module"
    ./gradlew :$module:build --no-configuration-cache > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "✅ Module $module built successfully"
    else
        echo "❌ Module $module build failed"
        exit 1
    fi
done

echo ""
echo "🎉 CI Pipeline Validation Complete!"
echo "===================================="
echo "✅ All checks passed successfully"
echo "✅ Ready for GitHub Actions CI/CD"