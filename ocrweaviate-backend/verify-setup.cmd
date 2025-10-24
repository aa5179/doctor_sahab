@echo off
echo ✅ OCRWeaviate Spring Boot Backend - Setup Verification
echo =======================================================
echo.

echo 📁 Checking project structure...

REM Check main application file
if exist "src\main\java\com\emulsify\ocrweaviate\OcrWeaviateApplication.java" (
    echo ✅ Main application class found
) else (
    echo ❌ Main application class missing
)

REM Check pom.xml
if exist "pom.xml" (
    echo ✅ Maven configuration found
) else (
    echo ❌ Maven configuration missing
)

REM Check properties file
if exist "src\main\resources\application.properties" (
    echo ✅ Application properties found
) else (
    echo ❌ Application properties missing
)

REM Check controller
if exist "src\main\java\com\emulsify\ocrweaviate\controller\DocumentController.java" (
    echo ✅ REST controller found
) else (
    echo ❌ REST controller missing
)

REM Check services
if exist "src\main\java\com\emulsify\ocrweaviate\service\OCRService.java" (
    echo ✅ OCR service found
) else (
    echo ❌ OCR service missing
)

if exist "src\main\java\com\emulsify\ocrweaviate\service\WeaviateService.java" (
    echo ✅ Weaviate service found
) else (
    echo ❌ Weaviate service missing
)

if exist "src\main\java\com\emulsify\ocrweaviate\service\GeminiService.java" (
    echo ✅ Gemini service found
) else (
    echo ❌ Gemini service missing
)

echo.
echo 🔧 Prerequisites Check:
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java not found - Please install Java 17+
) else (
    echo ✅ Java found
    java -version 2>&1 | findstr "version"
)

echo.
echo 📝 Next Steps:
echo 1. Install Maven: https://maven.apache.org/download.cgi
echo 2. Install Tesseract OCR: https://github.com/UB-Mannheim/tesseract/wiki
echo 3. Setup Weaviate: docker run -d -p 8080:8080 cr.weaviate.io/semitechnologies/weaviate:1.22.4
echo 4. Get Google API key: https://console.cloud.google.com/
echo 5. Copy application.properties.example to application.properties
echo 6. Update API keys in application.properties
echo 7. Run: mvn spring-boot:run
echo.
echo 🌐 Backend will be available at: http://localhost:8000
echo 🩺 Health check: http://localhost:8000/health
echo.
pause