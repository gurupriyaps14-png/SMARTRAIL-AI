# Stop Tomcat Server
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$env:CATALINA_HOME = "$scriptDir\tools\apache-tomcat-9.0.95"
& "$env:CATALINA_HOME\bin\shutdown.bat"
Write-Host "SmartRail AI server shutdown complete." -ForegroundColor Yellow

