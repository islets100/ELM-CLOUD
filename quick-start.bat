@echo off
echo Starting ELM Core Services...
echo.
echo [1/5] Starting Eureka Server (13000)...
start "Eureka-13000" cmd /k "cd elm-eureka-server && mvn spring-boot:run"
timeout /t 10 /nobreak >nul

echo [2/5] Starting Gateway Server (14000)...
start "Gateway-14000" cmd /k "cd elm-gateway-server && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo [3/5] Starting User Server (10100)...
start "User-10100" cmd /k "cd elm-user-server && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

echo [4/5] Starting Food Server (10200)...
start "Food-10200" cmd /k "cd elm-food-server && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

echo [5/5] Starting Order Server (11000)...
start "Order-11000" cmd /k "cd elm-order-server && mvn spring-boot:run"

echo.
echo All core services started! Wait 30-60 seconds for initialization.
echo.
echo Access Eureka: http://localhost:13000
echo Access Gateway: http://localhost:14000
pause
