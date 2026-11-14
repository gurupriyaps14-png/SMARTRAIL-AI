# ==============================================================================
# SmartRail AI - Windows Production Environment Starter
# Supports dynamic $env:PORT and persistent database storage
# ==============================================================================

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# 1. Determine Port (Default to 8080 if not defined)
$port = if ($env:PORT) { $env:PORT } else { "8080" }
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  SmartRail AI - Production Starter (Windows)" -ForegroundColor Yellow
Write-Host "  Configuring target port: $port" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan

# 2. Patch Tomcat conf/server.xml if port differs from default
$serverXml = Join-Path $scriptDir "tools\apache-tomcat-9.0.95\conf\server.xml"
if (Test-Path $serverXml) {
    $content = Get-Content $serverXml -Raw
    $updated = [System.Text.RegularExpressions.Regex]::Replace($content, 'port="\d+" protocol="HTTP/1.1"', "port=""$port"" protocol=""HTTP/1.1""")
    Set-Content $serverXml -Value $updated
}

# 3. Ensure persistent database path
if (-not $env:SMARTRAIL_H2_PATH) {
    $dataDir = Join-Path $scriptDir "smartrail_data"
    if (-not (Test-Path $dataDir)) { New-Item -ItemType Directory -Path $dataDir -Force | Out-Null }
    $env:SMARTRAIL_H2_PATH = (Join-Path $dataDir "smartrail_db")
}

# 4. Compile and deploy webapp
& "$scriptDir\deploy.ps1"

# 5. Launch Tomcat
if (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
}
$env:CATALINA_HOME = Join-Path $scriptDir "tools\apache-tomcat-9.0.95"
Write-Host "Starting SmartRail AI on http://localhost:$port/ ..." -ForegroundColor Green
& "$env:CATALINA_HOME\bin\catalina.bat" run

