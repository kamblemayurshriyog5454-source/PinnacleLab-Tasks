@echo off
setlocal
cd /d "%~dp0"
if not exist "dist" mkdir dist
if not exist "out" mkdir out
echo Building JAR...
javac -d out src\com\example\flightbooking\Main.java || exit /b 1
jar cfm dist\flight-booking.jar MANIFEST.MF -C out . || exit /b 1
echo Running application...
java -jar dist\flight-booking.jar
