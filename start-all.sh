#!/bin/bash

echo "===================================="
echo "ELM Cloud 本地启动脚本"
echo "===================================="
echo ""

echo "[1/6] 启动 Eureka 注册中心..."
gnome-terminal -- bash -c "cd elm-eureka-server && mvn spring-boot:run; exec bash" &
sleep 15

echo "[2/6] 启动 Gateway 网关..."
gnome-terminal -- bash -c "cd elm-gateway-server && mvn spring-boot:run; exec bash" &
sleep 10

echo "[3/6] 启动 User 用户服务..."
gnome-terminal -- bash -c "cd elm-user-server && mvn spring-boot:run; exec bash" &
sleep 5

echo "[4/6] 启动 Point 积分服务..."
gnome-terminal -- bash -c "cd elm-point-server && mvn spring-boot:run; exec bash" &
sleep 5

echo "[5/6] 启动 Wallet 钱包服务..."
gnome-terminal -- bash -c "cd elm-wallet-server && mvn spring-boot:run; exec bash" &
sleep 5

echo "[6/6] 启动 Frontend 前端服务..."
gnome-terminal -- bash -c "cd elmclient && npm run serve; exec bash" &

echo ""
echo "===================================="
echo "所有服务正在启动中..."
echo "===================================="
echo ""
echo "服务访问地址："
echo "- Eureka 控制台: http://localhost:13000"
echo "- Gateway 网关: http://localhost:14000"
echo "- 前端界面: http://localhost:8081"
echo ""
echo "按 Ctrl+C 退出监控..."
wait
