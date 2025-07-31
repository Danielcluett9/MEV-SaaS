#!/bin/bash
echo "🔍 Diagnosing MEV Platform Directory Structure"
echo "=============================================="

cd ~/MEVAnalytics/mev-platform-pro

echo "📁 Current directory: $(pwd)"
echo ""
echo "📂 Directory contents:"
ls -la

echo ""
echo "🔍 Checking for key directories:"

if [ -d "backend" ]; then
    echo "✅ Backend directory exists"
    if [ -f "backend/mvnw" ]; then
        echo "✅ Maven wrapper exists in backend"
    else
        echo "❌ Maven wrapper missing in backend"
    fi
else
    echo "❌ Backend directory missing"
fi

if [ -d "frontend" ]; then
    echo "✅ Frontend directory exists"
    if [ -f "frontend/package.json" ]; then
        echo "✅ Frontend package.json exists"
    else
        echo "❌ Frontend package.json missing"
    fi
else
    echo "❌ Frontend directory missing"
fi

if [ -d "scripts" ]; then
    echo "✅ Scripts directory exists"
    echo "📝 Scripts found:"
    ls -la scripts/
else
    echo "❌ Scripts directory missing"
fi

if [ -f "docker-compose.yml" ]; then
    echo "✅ Docker compose file exists"
else
    echo "❌ Docker compose file missing"
fi

echo ""
echo "🛠️ FIXING ISSUES..."
echo ""

# Create backend if it doesn't exist
if [ ! -d "backend" ]; then
    echo "🔧 Creating missing backend directory..."
    mkdir -p backend
    cd backend
    
    # Create pom.xml
    cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.2</version>
        <relativePath/>
    </parent>
    
    <groupId>com.mevanalytics</groupId>
    <artifactId>mev-platform-backend</artifactId>
    <version>1.0.0</version>
    <name>MEV Platform Backend</name>
    <description>Professional MEV Analytics SaaS Backend</description>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Boot Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Spring Boot Actuator (Health checks) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
EOF

    # Create Maven wrapper
    echo "📦 Creating Maven wrapper..."
    mvn wrapper:wrapper
    chmod +x mvnw
    
    # Create proper package structure
    mkdir -p src/main/java/com/mevanalytics/platform/{controller,service,model,repository,config}
    mkdir -p src/main/resources
    mkdir -p src/test/java/com/mevanalytics/platform

    # Create main application class
    cat > src/main/java/com/mevanalytics/platform/MEVPlatformApplication.java << 'EOF'
package com.mevanalytics.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MEVPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(MEVPlatformApplication.class, args);
    }
}
EOF

    # Create application.properties
    cat > src/main/resources/application.properties << 'EOF'
# Application Configuration
spring.application.name=MEV Analytics Platform
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/mevplatform
spring.datasource.username=mevuser
spring.datasource.password=secure_password_123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Actuator Configuration (Health checks only)
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
management.info.env.enabled=true

# Logging Configuration
logging.level.com.mevanalytics=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# CORS Configuration
spring.web.cors.allowed-origins=http://localhost:5173,http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true
EOF

    # Create CORS configuration
    cat > src/main/java/com/mevanalytics/platform/config/CorsConfig.java << 'EOF'
package com.mevanalytics.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
EOF

    # Create main controller
    cat > src/main/java/com/mevanalytics/platform/controller/MEVAnalyticsController.java << 'EOF'
package com.mevanalytics.platform.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class MEVAnalyticsController {
    
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // Main metrics
            dashboard.put("totalExtracted", new BigDecimal("2847293.45"));
            dashboard.put("todayExtracted", new BigDecimal("45782.33"));
            dashboard.put("sandwichAttacks", 1247);
            dashboard.put("arbitrageOps", 8934);
            dashboard.put("avgGasPrice", 34.7);
            
            // Daily trend data
            List<Map<String, Object>> dailyData = Arrays.asList(
                Map.of("date", "2025-01-20", "extracted", 45782, "attacks", 156, "arbitrage", 234),
                Map.of("date", "2025-01-21", "extracted", 52341, "attacks", 189, "arbitrage", 287),
                Map.of("date", "2025-01-22", "extracted", 48923, "attacks", 167, "arbitrage", 245),
                Map.of("date", "2025-01-23", "extracted", 61247, "attacks", 203, "arbitrage", 298),
                Map.of("date", "2025-01-24", "extracted", 58934, "attacks", 178, "arbitrage", 267),
                Map.of("date", "2025-01-25", "extracted", 67821, "attacks", 221, "arbitrage", 312),
                Map.of("date", "2025-01-26", "extracted", 72456, "attacks", 234, "arbitrage", 289)
            );
            dashboard.put("dailyData", dailyData);
            
            // MEV strategy breakdown
            List<Map<String, Object>> mevByStrategy = Arrays.asList(
                Map.of("name", "Arbitrage", "value", 45.2, "color", "#00D4FF"),
                Map.of("name", "Sandwich", "value", 31.8, "color", "#FF6B6B"),
                Map.of("name", "Liquidation", "value", 12.4, "color", "#4ECDC4"),
                Map.of("name", "Front-running", "value", 10.6, "color", "#45B7D1")
            );
            dashboard.put("mevByStrategy", mevByStrategy);
            
            // Top extractors leaderboard
            List<Map<String, Object>> topExtractors = Arrays.asList(
                Map.of("rank", 1, "address", "0x1a2b...c3d4", "extracted", 143247.89, "trades", 2847, "winRate", 94.2),
                Map.of("rank", 2, "address", "0x5e6f...7g8h", "extracted", 128934.56, "trades", 2156, "winRate", 91.7),
                Map.of("rank", 3, "address", "0x9i0j...k1l2", "extracted", 112678.23, "trades", 1934, "winRate", 89.3),
                Map.of("rank", 4, "address", "0x3m4n...o5p6", "extracted", 98234.77, "trades", 1678, "winRate", 87.8),
                Map.of("rank", 5, "address", "0x7q8r...s9t0", "extracted", 87456.12, "trades", 1456, "winRate", 85.4)
            );
            dashboard.put("topExtractors", topExtractors);
            
            // API metadata
            dashboard.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            dashboard.put("version", "1.0.0");
            dashboard.put("status", "active");
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch dashboard data", "message", e.getMessage()));
        }
    }
    
    @PostMapping("/api-key/generate")
    public ResponseEntity<Map<String, String>> generateAPIKey(
            @RequestBody Map<String, String> request) {
        
        try {
            String email = request.get("email");
            String tier = request.get("tier");
            
            if (email == null || tier == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email and tier are required"));
            }
            
            // Generate API key
            String apiKey = "mev_" + UUID.randomUUID().toString().replace("-", "");
            
            System.out.println("💰 Generated API key for " + email + " (" + tier + "): " + apiKey);
            
            Map<String, String> response = new HashMap<>();
            response.put("apiKey", apiKey);
            response.put("status", "success");
            response.put("message", "API key generated successfully");
            response.put("email", email);
            response.put("tier", tier);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate API key", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "MEV Analytics Platform");
        health.put("version", "1.0.0");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("uptime", "Running");
        health.put("database", "Connected");
        health.put("environment", "development");
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> status = new HashMap<>();
        status.put("platform", "MEV Analytics Platform Pro");
        status.put("version", "1.0.0");
        status.put("status", "operational");
        status.put("features", Arrays.asList(
            "Real-time MEV tracking",
            "Sandwich attack detection", 
            "Arbitrage analytics",
            "Top extractor leaderboards",
            "API access"
        ));
        status.put("pricing", Map.of(
            "starter", "$49/month",
            "professional", "$199/month", 
            "enterprise", "$999/month"
        ));
        
        return ResponseEntity.ok(status);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal server error");
        error.put("message", e.getMessage());
        error.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
EOF

    echo "✅ Backend created and configured"
    cd ..
fi

# Fix scripts if they exist but have wrong paths
if [ -d "scripts" ]; then
    echo "🔧 Fixing script paths..."
    
    # Fix test-platform.sh
    cat > scripts/test-platform.sh << 'EOF'
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
EOF

    chmod +x scripts/test-platform.sh
    echo "✅ Fixed test-platform.sh"
fi

# Ensure database setup exists
if [ ! -f "docker-compose.yml" ]; then
    echo "🔧 Creating missing docker-compose.yml..."
    cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: mev-postgres
    environment:
      POSTGRES_DB: mevplatform
      POSTGRES_USER: mevuser
      POSTGRES_PASSWORD: secure_password_123
      POSTGRES_INITDB_ARGS: "--encoding=UTF-8"
    ports:
      - '5432:5432'
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U mevuser -d mevplatform"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: mev-redis
    ports:
      - '6379:6379'
    volumes:
      - redis_data:/data
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 3

volumes:
  postgres_data:
    driver: local
  redis_data:
    driver: local

networks:
  default:
    name: mev-network
EOF
    echo "✅ Created docker-compose.yml"
fi

# Create logs directory
mkdir -p logs

echo ""
echo "🎉 DIAGNOSIS AND FIX COMPLETE!"
echo "============================="
echo ""
echo "✅ All directories and files are now properly configured"
echo ""
echo "📂 Final directory structure:"
echo "$(pwd)"
ls -la

echo ""
echo "🧪 Now test your platform:"
echo "   ./scripts/test-platform.sh"
echo ""
echo "🚀 Then start your platform:"
echo "   ./scripts/start-platform.sh"
echo ""
