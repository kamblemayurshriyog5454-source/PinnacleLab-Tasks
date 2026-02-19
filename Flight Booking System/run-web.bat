@echo off
setlocal
cd /d "%~dp0"
if not exist "out" mkdir out
echo Compiling web server...
javac -d out src\com\example\flightbooking\WebServer.java src\com\example\flightbooking\Main.java || exit /b 1
echo Starting web server at http://localhost:8080 ...
java -cp out com.example.flightbooking.WebServer
