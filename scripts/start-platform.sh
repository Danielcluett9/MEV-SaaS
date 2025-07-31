#!/bin/bash
echo "🚀 Starting MEV Analytics Platform Pro"
echo "======================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    echo "   On Ubuntu: sudo systemctl start docker"
    exit 1
fi

# Start databases
echo "1. Starting databases..."
docker-compose up -d postgres redis

# Wait for databases to be healthy
echo "2. Waiting for databases to be ready..."
echo "   Checking PostgreSQL..."
timeout=60
counter=0
while ! docker exec mev-postgres pg_isready -U mevuser -d mevplatform > /dev/null 2>&1; do
    if [ $counter -ge $timeout ]; then
        echo "❌ PostgreSQL failed to start within $timeout seconds"
        echo "   Check logs: docker logs mev-postgres"
        exit 1
    fi
    echo "   ... waiting for PostgreSQL ($counter/$timeout)"
    sleep 2
    counter=$((counter + 2))
done
echo "   ✅ PostgreSQL is ready"

echo "   Checking Redis..."
counter=0
while ! docker exec mev-redis redis-cli ping > /dev/null 2>&1; do
    if [ $counter -ge $timeout ]; then
        echo "❌ Redis failed to start within $timeout seconds"
        echo "   Check logs: docker logs mev-redis"
        exit 1
    fi
    echo "   ... waiting for Redis ($counter/$timeout)"
    sleep 2
    counter=$((counter + 2))
done
echo "   ✅ Redis is ready"

echo ""
echo "3. Starting backend..."
cd backend
./mvnw spring-boot:run > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
echo "   Backend PID: $BACKEND_PID"
cd ..

# Wait for backend to start
echo "4. Waiting for backend API..."
counter=0
while ! curl -f -s http://localhost:8080/api/v1/health > /dev/null 2>&1; do
    if [ $counter -ge 60 ]; then
        echo "❌ Backend failed to start within 60 seconds"
        echo "   Check logs: tail -f logs/backend.log"
        kill $BACKEND_PID 2>/dev/null
        exit 1
    fi
    echo "   ... waiting for backend API ($counter/60)"
    sleep 2
    counter=$((counter + 2))
done
echo "   ✅ Backend API is ready"

echo ""
echo "5. Starting frontend..."
cd frontend
npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "   Frontend PID: $FRONTEND_PID"
cd ..

# Wait for frontend
echo "6. Waiting for frontend..."
counter=0
while ! curl -f -s http://localhost:5173 > /dev/null 2>&1; do
    if [ $counter -ge 30 ]; then
        echo "❌ Frontend failed to start within 30 seconds"
        echo "   Check logs: tail -f logs/frontend.log"
        kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
        exit 1
    fi
    echo "   ... waiting for frontend ($counter/30)"
    sleep 2
    counter=$((counter + 2))
done
echo "   ✅ Frontend is ready"

echo ""
echo "🎉 MEV ANALYTICS PLATFORM STARTED SUCCESSFULLY!"
echo "=============================================="
echo ""
echo "📱 Frontend Dashboard: http://localhost:5173"
echo "🔧 Backend API:        http://localhost:8080/api/v1/health"
echo "🗄️ PostgreSQL:         localhost:5433 (mevplatform)"
echo "🔴 Redis:              localhost:6380"
echo ""
echo "📊 API Endpoints:"
echo "   Health:     GET  /api/v1/health"
echo "   Dashboard:  GET  /api/v1/analytics/dashboard"
echo "   API Keys:   POST /api/v1/api-key/generate"
echo "   Status:     GET  /api/v1/status"
echo ""
echo "📋 Process IDs:"
echo "   Backend:  $BACKEND_PID"
echo "   Frontend: $FRONTEND_PID"
echo ""
echo "🛑 To stop: ./scripts/stop-platform.sh"
echo "📝 Logs: tail -f logs/backend.log logs/frontend.log"
echo ""
echo "💰 Your $100K+ MRR platform is ready!"
echo ""

# Save PIDs for stop script
echo "$BACKEND_PID" > .backend.pid
echo "$FRONTEND_PID" > .frontend.pid

# Keep script running to show real-time status
trap 'echo "Stopping platform..."; ./scripts/stop-platform.sh; exit' INT

echo "Platform is running. Press Ctrl+C to stop all services."
echo ""

# Show real-time status
while true; do
    if kill -0 $BACKEND_PID 2>/dev/null && kill -0 $FRONTEND_PID 2>/dev/null; then
        echo "$(date '+%H:%M:%S') - ✅ All services running (Backend: $BACKEND_PID, Frontend: $FRONTEND_PID)"
    else
        echo "$(date '+%H:%M:%S') - ❌ Some services stopped"
        break
    fi
    sleep 30
done
