@echo off
chcp 65001 >nul
echo ====================================
echo Stopping All ELM Cloud Services
echo ====================================
echo.

echo Stopping all Java and Node.js processes...
taskkill /F /IM java.exe 2>nul
taskkill /F /IM node.exe 2>nul

echo.
echo ====================================
echo All services stopped
echo ====================================
echo.
timeout /t 2 /nobreak > nul
