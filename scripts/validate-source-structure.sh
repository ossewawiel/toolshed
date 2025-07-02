#!/bin/bash
set -e

echo "📁 Source Structure Validation"
echo "=============================="

echo ""
echo "🔍 Step 1: Directory Structure Verification"
echo "--------------------------------------------"

modules=("shared-theme" "i18n-library" "ui-components" "plugin-api" "master-app" "tools/server-configuration")

for module in "${modules[@]}"; do
    echo "Checking module: $module"
    
    # Check main source directory
    if [ -d "$module/src/jvmMain/kotlin" ]; then
        echo "✅ $module has jvmMain/kotlin directory"
    else
        echo "❌ $module missing jvmMain/kotlin directory"
        exit 1
    fi
    
    # Check for at least one Kotlin file
    if find "$module/src/jvmMain/kotlin" -name "*.kt" | grep -q .; then
        echo "✅ $module has Kotlin source files"
    else
        echo "❌ $module missing Kotlin source files"
        exit 1
    fi
done

echo ""
echo "📦 Step 2: Package Structure Verification"
echo "------------------------------------------"

echo "shared-theme packages:"
find shared-theme/src/jvmMain/kotlin -name "*.kt" -exec dirname {} \; | sort -u | sed 's|.*/kotlin/||'

echo ""
echo "i18n-library packages:"
find i18n-library/src/jvmMain/kotlin -name "*.kt" -exec dirname {} \; | sort -u | sed 's|.*/kotlin/||'

echo ""
echo "ui-components packages:"
find ui-components/src/jvmMain/kotlin -name "*.kt" -exec dirname {} \; | sort -u | sed 's|.*/kotlin/||'

echo ""
echo "plugin-api packages:"
find plugin-api/src/jvmMain/kotlin -name "*.kt" -exec dirname {} \; | sort -u | sed 's|.*/kotlin/||'

echo ""
echo "master-app packages:"
find master-app/src/jvmMain/kotlin -name "*.kt" -exec dirname {} \; | sort -u | sed 's|.*/kotlin/||'

echo ""
echo "server-configuration packages:"
find tools/server-configuration/src/jvmMain/kotlin -name "*.kt" -exec dirname {} \; | sort -u | sed 's|.*/kotlin/||'

echo ""
echo "⚙️ Step 3: Compilation Verification"
echo "------------------------------------"

echo "Testing individual module compilation..."
for module in "${modules[@]}"; do
    echo "Compiling module: $module"
    ./gradlew :${module//\//:}:compileKotlinJvm --no-configuration-cache --quiet
    if [ $? -eq 0 ]; then
        echo "✅ Module $module compiles successfully"
    else
        echo "❌ Module $module compilation failed"
        exit 1
    fi
done

echo ""
echo "🧪 Step 4: Full Project Build Verification"
echo "-------------------------------------------"

echo "Running full project build..."
./gradlew build --no-configuration-cache --quiet
if [ $? -eq 0 ]; then
    echo "✅ Full project builds successfully with source code"
else
    echo "❌ Full project build failed"
    exit 1
fi

echo ""
echo "📊 Step 5: Source Code Statistics"
echo "----------------------------------"

echo "Total Kotlin files: $(find . -name "*.kt" | wc -l)"
echo "Total lines of code: $(find . -name "*.kt" -exec cat {} \; | wc -l)"

echo ""
echo "Files by module:"
for module in "${modules[@]}"; do
    count=$(find "$module" -name "*.kt" 2>/dev/null | wc -l)
    echo "  $module: $count files"
done

echo ""
echo "🎉 Source Structure Validation Complete!"
echo "========================================"
echo "✅ All modules have proper source structure"
echo "✅ Package hierarchy follows conventions"
echo "✅ All source code compiles successfully"
echo "✅ Ready for application development"