#!/bin/bash
echo "🧪 Testing Backend Only"
echo "======================"

cd backend

echo "1. Stopping any existing backend processes..."
pkill -f "spring-boot:run" 2>/dev/null
sleep 2

echo "2. Starting backend in foreground (so you can see errors)..."
echo "   Press Ctrl+C to stop"
echo ""

./mvnw spring-boot:run
