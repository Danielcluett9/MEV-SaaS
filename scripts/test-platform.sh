#!/bin/bash
echo "🧪 Testing MEV Analytics Platform Pro"
echo "===================================="

# Get the absolute path to the project root
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)
echo "Project root: $PROJECT_ROOT"

# Test Node.js version
NODE_VERSION=$(node -v)
echo "Node.js version: $NODE_VERSION"
if [[ "$NODE_VERSION" < "v20" ]]; then
    echo "❌ Node.js version too old. Need v20+."
    exit 1
fi
echo "✅ Node.js version compatible"

# Test Java version
JAVA_VERSION=$(java -version 2>&1 | grep "openjdk version" | cut -d'"' -f2)
echo "Java version: $JAVA_VERSION"
echo "✅ Java version compatible"

# Test backend compilation
echo ""
echo "Testing backend compilation..."
if [ -d "$PROJECT_ROOT/backend" ]; then
    cd "$PROJECT_ROOT/backend"
    if [ -f "./mvnw" ]; then
        ./mvnw clean compile -q
        if [ $? -eq 0 ]; then
            echo "✅ Backend compiles successfully"
        else
            echo "❌ Backend compilation failed"
            exit 1
        fi
    else
        echo "❌ Maven wrapper (mvnw) not found in backend directory"
        exit 1
    fi
else
    echo "❌ Backend directory not found"
    exit 1
fi

# Test frontend build
echo ""
echo "Testing frontend build..."
if [ -d "$PROJECT_ROOT/frontend" ]; then
    cd "$PROJECT_ROOT/frontend"
    if [ -f "package.json" ]; then
        npm run build > /dev/null 2>&1
        if [ $? -eq 0 ]; then
            echo "✅ Frontend builds successfully"
        else
            echo "❌ Frontend build failed"
            exit 1
        fi
    else
        echo "❌ Frontend package.json not found"
        exit 1
    fi
else
    echo "❌ Frontend directory not found"
    exit 1
fi

echo ""
echo "🎉 ALL TESTS PASSED!"
echo "==================="
echo ""
echo "Your MEV Analytics Platform is ready to launch!"
echo ""
echo "🚀 Start platform: ./scripts/start-platform.sh"
echo "🛑 Stop platform:  ./scripts/stop-platform.sh"
echo ""
