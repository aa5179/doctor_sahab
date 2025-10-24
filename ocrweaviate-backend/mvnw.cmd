@REM Maven Wrapper Script for Windows

@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0

if "%MAVEN_PROJECTBASEDIR%" == "" (
    set MAVEN_PROJECTBASEDIR=.
)

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

if exist %WRAPPER_JAR% (
    echo Found wrapper jar: %WRAPPER_JAR%
    
    if "%JAVA_HOME%" == "" (
        java -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %*
    ) else (
        "%JAVA_HOME%\bin\java" -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %*
    )
) else (
    echo Maven wrapper not found, using system Maven
    mvn %*
)

endlocal