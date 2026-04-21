@echo off
chcp 65001 >nul
echo ====================================
echo Testing MySQL Connection
echo ====================================
echo.

set MYSQL_USER=root
set MYSQL_PASSWORD=123456
set MYSQL_HOST=localhost
set MYSQL_PORT=3306

echo Testing MySQL connection...
echo Username: %MYSQL_USER%
echo Host: %MYSQL_HOST%
echo Port: %MYSQL_PORT%
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u %MYSQL_USER% -p%MYSQL_PASSWORD% -h %MYSQL_HOST% -P %MYSQL_PORT% -e "SELECT 1 AS test_connection; SHOW DATABASES;"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====================================
    echo MySQL Connection Successful!
    echo ====================================
) else (
    echo.
    echo ====================================
    echo MySQL Connection Failed!
    echo Please check:
    echo 1. MySQL service is running
    echo 2. Username and password are correct
    echo ====================================
)

echo.
pause
