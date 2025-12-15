#!/bin/bash

# Script để dừng tất cả các dịch vụ của dự án
# Sử dụng: ./stop-all.sh

echo "=========================================="
echo "  Dừng tất cả dịch vụ"
echo "=========================================="
echo ""

# Dừng Keycloak
echo "1. Đang dừng Keycloak..."
KEYCLOAK_PIDS=$(ps aux | grep '[k]c.sh start-dev' | awk '{print $2}')
if [ ! -z "$KEYCLOAK_PIDS" ]; then
    echo "$KEYCLOAK_PIDS" | xargs kill 2>/dev/null || true
    echo "✓ Keycloak đã dừng"
else
    echo "  Keycloak không chạy"
fi

# Dừng Spring Boot
echo ""
echo "2. Đang dừng Spring Boot..."
SPRING_BOOT_PIDS=$(lsof -ti :8082 2>/dev/null || ps aux | grep '[s]pring-boot:run' | awk '{print $2}')
if [ ! -z "$SPRING_BOOT_PIDS" ]; then
    echo "$SPRING_BOOT_PIDS" | xargs kill 2>/dev/null || true
    echo "✓ Spring Boot đã dừng"
else
    echo "  Spring Boot không chạy"
fi

# Dừng Angular
echo ""
echo "3. Đang dừng Angular..."
ANGULAR_PIDS=$(lsof -ti :4200 2>/dev/null || ps aux | grep '[n]g serve' | awk '{print $2}')
if [ ! -z "$ANGULAR_PIDS" ]; then
    echo "$ANGULAR_PIDS" | xargs kill 2>/dev/null || true
    echo "✓ Angular đã dừng"
else
    echo "  Angular không chạy"
fi

echo ""
echo "=========================================="
echo "  Tất cả dịch vụ đã được dừng!"
echo "=========================================="

