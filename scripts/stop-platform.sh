#!/bin/bash
echo "🛑 Stopping MEV Analytics Platform"
echo "================================="

# Read PIDs if they exist
if [ -f .backend.pid ]; then
    BACKEND_PID=$(cat .backend.pid)
    echo "Stopping backend (PID: $BACKEND_PID)..."
    kill $BACKEND_PID 2>/dev/null
    rm .backend.pid
fi

if [ -f .frontend.pid ]; then
    FRONTEND_PID=$(cat .frontend.pid)
    echo "Stopping frontend (PID: $FRONTEND_PID)..."
    kill $FRONTEND_PID 2>/dev/null
    rm .frontend.pid
fi

# Stop any remaining Java/Node processes
echo "Cleaning up any remaining processes..."
pkill -f "spring-boot:run" 2>/dev/null
pkill -f "vite" 2>/dev/null

# Stop databases
echo "Stopping databases..."
docker-compose down

echo "✅ Platform stopped"
