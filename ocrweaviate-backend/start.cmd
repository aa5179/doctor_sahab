@echo off
echo 🚀 Starting OCRWeaviate Spring Boot Backend...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java not found! Please install Java 17 or higher.
    pause
    exit /b 1
)

REM Check if Maven is available
where mvn >nul 2>&1
if errorlevel 1 (
    echo 📦 Maven not found, using Maven wrapper...
    set MAVEN_CMD=mvnw.cmd
) else (
    echo 📦 Using system Maven...
    set MAVEN_CMD=mvn
)

echo.
echo 🔧 Building and starting the application...
echo.

%MAVEN_CMD% spring-boot:run

if errorlevel 1 (
    echo.
    echo ❌ Failed to start the application!
    echo.
    echo Common issues:
    echo - Make sure Java 17+ is installed
    echo - Check if ports 8000 is available
    echo - Verify Weaviate is running on port 8080
    echo - Set your Google API key in application.properties
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ Application started successfully!
echo 🌐 Backend available at: http://localhost:8000
echo 📖 API Documentation: http://localhost:8000/health
echo.
pause