@echo off
echo Compiling...
javac -cp "lib/mysql-connector-j-9.5.0.jar;." backend/*.java frontend/*.java Main.java

if %errorlevel% neq 0 (
    echo Compilation Failed!
    pause
    exit /b %errorlevel%
)

echo Starting Quiz Web App...
echo Go to http://localhost:8080 if it doesn't open automatically.
java -cp "lib/mysql-connector-j-9.5.0.jar;." Main
pause
