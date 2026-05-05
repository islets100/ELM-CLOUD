@echo off
chcp 65001 >nul
echo ====================================
echo Initializing ELM Project Databases
echo ====================================
echo.

set MYSQL_USER=root
set MYSQL_PASSWORD=123456
set MYSQL_HOST=localhost
set MYSQL_PORT=3306

echo Connecting to MySQL and creating databases...
echo Username: %MYSQL_USER%
echo Host: %MYSQL_HOST%
echo Port: %MYSQL_PORT%
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u %MYSQL_USER% -p%MYSQL_PASSWORD% -h %MYSQL_HOST% -P %MYSQL_PORT% -e "CREATE DATABASE IF NOT EXISTS elm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; CREATE DATABASE IF NOT EXISTS elm2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====================================
    echo Databases Created Successfully!
    echo ====================================
    echo.
    echo Created databases:
    echo - elm (main business database)
    echo - elm2 (user service database)
    echo.
    echo Database list:
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u %MYSQL_USER% -p%MYSQL_PASSWORD% -h %MYSQL_HOST% -P %MYSQL_PORT% -e "SHOW DATABASES;"
) else (
    echo.
    echo ====================================
    echo Database Creation Failed!
    echo Please check:
    echo 1. MySQL service is running
    echo 2. Username and password are correct
    echo 3. MySQL is installed in default path
    echo ====================================
)

echo.
pause
