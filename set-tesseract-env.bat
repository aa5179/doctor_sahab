@echo off
echo Setting up Tesseract environment variables...

REM Set TESSDATA_PREFIX environment variable
setx TESSDATA_PREFIX "C:\Program Files\Tesseract-OCR\tessdata"

REM Add Tesseract to PATH if not already there
echo %PATH% | findstr /C:"Tesseract-OCR" >nul
if errorlevel 1 (
    echo Adding Tesseract to PATH...
    setx PATH "%PATH%;C:\Program Files\Tesseract-OCR"
) else (
    echo Tesseract already in PATH
)

echo.
echo Environment variables set successfully!
echo TESSDATA_PREFIX = C:\Program Files\Tesseract-OCR\tessdata
echo.
echo Please restart your command prompt and the Java application for changes to take effect.
echo.
pause