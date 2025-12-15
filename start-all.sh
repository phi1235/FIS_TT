#!/bin/bash

# Script để khởi động tất cả các dịch vụ của dự án
# Sử dụng: ./start-all.sh

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYCLOAK_DIR="$PROJECT_DIR/keycloak-18.0.2"
SPRING_BOOT_DIR="$PROJECT_DIR/spring-boot-keycloak"
ANGULAR_DIR="$PROJECT_DIR/my-angular-app"

echo "=========================================="
echo "  Khởi động dự án Keycloak Integration"
echo "=========================================="
echo ""

# Kiểm tra Keycloak đã chạy chưa
if curl -s http://localhost:8080/realms/master > /dev/null 2>&1; then
    echo "✓ Keycloak đã đang chạy"
else
    echo "1. Đang khởi động Keycloak..."
    cd "$KEYCLOAK_DIR"
    ./bin/kc.sh start-dev --http-port=8080 > /tmp/keycloak.log 2>&1 &
    KEYCLOAK_PID=$!
    echo "   Keycloak đang khởi động (PID: $KEYCLOAK_PID)..."
    
    # Đợi Keycloak sẵn sàng
    echo "   Đang đợi Keycloak sẵn sàng..."
    for i in {1..60}; do
        if curl -s http://localhost:8080/realms/master > /dev/null 2>&1; then
            echo "✓ Keycloak đã sẵn sàng!"
            break
        fi
        if [ $i -eq 60 ]; then
            echo "✗ Keycloak không khởi động được sau 60 giây"
            exit 1
        fi
        sleep 1
    done
    
    # Chạy setup script để tạo realm và clients
    echo ""
    echo "2. Đang thiết lập realm và clients..."
    cd "$PROJECT_DIR"
    node setup-keycloak.js
    echo "✓ Setup hoàn tất"
fi

# Kiểm tra Spring Boot đã chạy chưa
if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1 || lsof -Pi :8082 -sTCP:LISTEN -t > /dev/null 2>&1; then
    echo ""
    echo "✓ Spring Boot đã đang chạy"
else
    echo ""
    echo "3. Đang khởi động Spring Boot application..."
    cd "$SPRING_BOOT_DIR"
    mvn spring-boot:run > /tmp/spring-boot.log 2>&1 &
    SPRING_BOOT_PID=$!
    echo "   Spring Boot đang khởi động (PID: $SPRING_BOOT_PID)..."
    echo "   Logs: /tmp/spring-boot.log"
    
    # Đợi Spring Boot sẵn sàng
    echo "   Đang đợi Spring Boot sẵn sàng..."
    for i in {1..60}; do
        if lsof -Pi :8082 -sTCP:LISTEN -t > /dev/null 2>&1; then
            echo "✓ Spring Boot đã sẵn sàng!"
            break
        fi
        if [ $i -eq 60 ]; then
            echo "✗ Spring Boot không khởi động được sau 60 giây"
            exit 1
        fi
        sleep 1
    done
fi

# Khởi động Angular
echo ""
echo "4. Đang khởi động Angular application..."
cd "$ANGULAR_DIR"
npm start > /tmp/angular.log 2>&1 &
ANGULAR_PID=$!
echo "   Angular đang khởi động (PID: $ANGULAR_PID)..."
echo "   Logs: /tmp/angular.log"

echo ""
echo "=========================================="
echo "  Tất cả dịch vụ đã được khởi động!"
echo "=========================================="
echo ""
echo "URLs:"
echo "  - Keycloak Admin: http://localhost:8080"
echo "    Username: admin"
echo "    Password: admin"
echo "  - Spring Boot API: http://localhost:8082"
echo "  - Angular App: http://localhost:4200"
echo ""
echo "Để dừng tất cả dịch vụ, chạy: ./stop-all.sh"
echo "Hoặc kill các process:"
if [ ! -z "$KEYCLOAK_PID" ]; then
    echo "  Keycloak: kill $KEYCLOAK_PID"
fi
if [ ! -z "$SPRING_BOOT_PID" ]; then
    echo "  Spring Boot: kill $SPRING_BOOT_PID"
fi
if [ ! -z "$ANGULAR_PID" ]; then
    echo "  Angular: kill $ANGULAR_PID"
fi
echo ""

