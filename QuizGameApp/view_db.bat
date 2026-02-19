@echo off
echo Compiling DB Viewer...
javac -cp "lib/mysql-connector-j-9.5.0.jar;." ShowDB.java

if %errorlevel% neq 0 (
    echo Compilation Failed!
    pause
    exit /b %errorlevel%
)

echo Displaying Database Content...
java -cp "lib/mysql-connector-j-9.5.0.jar;." ShowDB
pause
