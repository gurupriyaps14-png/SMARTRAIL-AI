# PowerShell Runner for SmartRail AI
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

& "$scriptDir\deploy.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Deployment failed!" -ForegroundColor Red
    exit 1
}

if (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
}
$env:CATALINA_HOME = "$scriptDir\tools\apache-tomcat-9.0.95"
Write-Host "Starting Tomcat on http://localhost:8080/ ..." -ForegroundColor Cyan
Start-Process "$env:CATALINA_HOME\bin\catalina.bat" -ArgumentList "run" -NoNewWindow
Write-Host "SmartRail AI is starting! Visit http://localhost:8080/" -ForegroundColor Green
