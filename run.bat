@echo off
setlocal
echo ========================================================
echo   Starting SmartRail AI - Tamil Nadu Railway Control Room
echo ========================================================

powershell -ExecutionPolicy Bypass -File "%~dp0deploy.ps1"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build or deployment failed!
    pause
    exit /b %ERRORLEVEL%
)

if "%JAVA_HOME%"=="" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12"
)
set "CATALINA_HOME=%~dp0tools\apache-tomcat-9.0.95"
set "JRE_HOME=%JAVA_HOME%"

echo.
echo Launching Tomcat Server on http://localhost:8080/ ...
call "%CATALINA_HOME%\bin\startup.bat"

echo.
echo ========================================================
echo   SmartRail AI is now live!
echo   Open in browser:
echo   -> http://localhost:8080/
echo   -> http://localhost:8080/SmartRailAI/
echo ========================================================
echo.
pause
