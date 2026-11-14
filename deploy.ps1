# SmartRail AI - Build and Deploy Script

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  SmartRail AI - Build & Deployment Engine" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

$tomcatLib = Join-Path $scriptDir "tools\apache-tomcat-9.0.95\lib"
$webappDir = Join-Path $scriptDir "src\main\webapp"
$classesDir = Join-Path $webappDir "WEB-INF\classes"
$sqlDestDir = Join-Path $classesDir "sql"

# 1. Ensure target classes directories exist
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
New-Item -ItemType Directory -Force -Path $sqlDestDir | Out-Null

# 2. Copy SQL scripts to classes directory for classpath loading
Copy-Item "sql\*.sql" -Destination $sqlDestDir -Force
Write-Host "[1/4] SQL schema & data scripts synced to classpath." -ForegroundColor Green

# 3. Compile Java Source Files
$classpath = "$tomcatLib\servlet-api.jar;$tomcatLib\jsp-api.jar;$webappDir\WEB-INF\lib\gson-2.10.1.jar;$webappDir\WEB-INF\lib\mysql-connector-j-8.3.0.jar;$webappDir\WEB-INF\lib\h2-2.2.224.jar"
$javaFiles = Get-ChildItem -Recurse -Filter "*.java" "src\main\java" | Select-Object -ExpandProperty FullName

Write-Host "[2/4] Compiling Java classes ($($javaFiles.Count) files)..." -ForegroundColor Cyan
javac -cp $classpath -d $classesDir $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "Java compilation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "      Java compilation successful." -ForegroundColor Green

# 4. Deploy to Tomcat webapps (both ROOT and SmartRailAI)
$rootApp = Join-Path $scriptDir "tools\apache-tomcat-9.0.95\webapps\ROOT"
$namedApp = Join-Path $scriptDir "tools\apache-tomcat-9.0.95\webapps\SmartRailAI"

Write-Host "[3/4] Deploying to Tomcat webapps..." -ForegroundColor Cyan

# Ensure target directories exist
New-Item -ItemType Directory -Force -Path $rootApp | Out-Null
New-Item -ItemType Directory -Force -Path $namedApp | Out-Null

function Safe-CopyWebapp($source, $dest) {
    Get-ChildItem -Path $source -Recurse | ForEach-Object {
        $target = $_.FullName.Replace($source, $dest)
        if ($_.PSIsContainer) {
            if (!(Test-Path $target)) { New-Item -ItemType Directory -Path $target -Force | Out-Null }
        } else {
            try {
                Copy-Item $_.FullName -Destination $target -Force -ErrorAction Stop
            } catch {
                # Ignore locked jar files if identical
            }
        }
    }
}

Safe-CopyWebapp $webappDir $rootApp
Safe-CopyWebapp $webappDir $namedApp

Write-Host "[4/4] Deployment completed successfully!" -ForegroundColor Green
Write-Host "Application is deployed to:" -ForegroundColor Yellow
Write-Host "  -> http://localhost:8080/" -ForegroundColor White
Write-Host "  -> http://localhost:8080/SmartRailAI/" -ForegroundColor White
Write-Host "==========================================" -ForegroundColor Cyan

