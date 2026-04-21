@echo off
chcp 65001 >nul
echo ====================================
echo ELM Cloud Local Startup Script
echo ====================================
echo.

echo [1/6] Starting Eureka Server...
start "Eureka Server" cmd /k "cd elm-eureka-server && mvn spring-boot:run"
timeout /t 15 /nobreak > nul

echo [2/6] Starting Gateway Server...
start "Gateway Server" cmd /k "cd elm-gateway-server && mvn spring-boot:run"
timeout /t 10 /nobreak > nul

echo [3/6] Starting User Server...
start "User Server" cmd /k "cd elm-user-server && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

echo [4/6] Starting Point Server...
start "Point Server" cmd /k "cd elm-point-server && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

echo [5/6] Starting Wallet Server...
start "Wallet Server" cmd /k "cd elm-wallet-server && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

echo [6/6] Starting Frontend Server...
start "Frontend Server" cmd /k "cd elmclient && npm run serve"

echo.
echo ====================================
echo All services are starting...
echo ====================================
echo.
echo Service URLs:
echo - Eureka Console: http://localhost:13000
echo - Gateway: http://localhost:14000
echo - Frontend: http://localhost:8081
echo.
echo Press any key to close this window...
pause > nul
