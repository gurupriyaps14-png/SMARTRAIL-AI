@echo off
setlocal
echo Stopping SmartRail AI Tomcat Server...
set "CATALINA_HOME=%~dp0tools\apache-tomcat-9.0.95"
call "%CATALINA_HOME%\bin\shutdown.bat"
echo Done.

