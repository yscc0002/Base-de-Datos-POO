$ProjectDir = Get-Location
$ToolsDir = Join-Path $ProjectDir ".tools"
$JdkDir = Join-Path $ToolsDir "jdk"
$MavenDir = Join-Path $ToolsDir "maven"

if (-not (Test-Path $ToolsDir)) {
    New-Item -ItemType Directory -Path $ToolsDir | Out-Null
}

$JavaCmd = "java"
$HasJava = [bool](Get-Command $JavaCmd -ErrorAction SilentlyContinue)

if (-not $HasJava) {
    $LocalJava = Join-Path $JdkDir "bin\java.exe"
    if (Test-Path $LocalJava) {
        $env:PATH = "$JdkDir\bin;" + $env:PATH
        $HasJava = $true
        Write-Host "Using local JDK found in $JdkDir" -ForegroundColor Green
    } else {
        Write-Host "Java is not installed on this system. Downloading a portable JDK 17 (Eclipse Temurin)..." -ForegroundColor Yellow
        $JdkZip = Join-Path $ToolsDir "jdk.zip"
        $JdkUrl = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse"
        
        Write-Host "Downloading JDK from $JdkUrl..." -ForegroundColor Cyan
        Invoke-WebRequest -Uri $JdkUrl -OutFile $JdkZip
        
        Write-Host "Extracting JDK..." -ForegroundColor Cyan
        if (-not (Test-Path $JdkDir)) { New-Item -ItemType Directory -Path $JdkDir | Out-Null }
        
        $TempExtract = Join-Path $ToolsDir "jdk_temp"
        if (Test-Path $TempExtract) { Remove-Item -Recurse -Force $TempExtract }
        New-Item -ItemType Directory -Path $TempExtract | Out-Null
        
        Expand-Archive -Path $JdkZip -DestinationPath $TempExtract
        
        $SubFolder = Get-ChildItem -Path $TempExtract -Directory | Select-Object -First 1
        if ($SubFolder) {
            Move-Item -Path "$($SubFolder.FullName)\*" -Destination $JdkDir -Force
        }
        
        Remove-Item -Path $JdkZip -Force
        Remove-Item -Recurse -Force $TempExtract
        
        $env:PATH = "$JdkDir\bin;" + $env:PATH
        $HasJava = $true
        Write-Host "JDK 17 installed locally in $JdkDir" -ForegroundColor Green
    }
} else {
    Write-Host "System Java detected." -ForegroundColor Green
}

$MvnCmd = "mvn"
$HasMvn = [bool](Get-Command $MvnCmd -ErrorAction SilentlyContinue)

if (-not $HasMvn) {
    $LocalMvn = Join-Path $MavenDir "bin\mvn.cmd"
    if (Test-Path $LocalMvn) {
        $env:PATH = "$MavenDir\bin;" + $env:PATH
        $HasMvn = $true
        Write-Host "Using local Maven found in $MavenDir" -ForegroundColor Green
    } else {
        Write-Host "Maven is not installed on this system. Downloading a portable Apache Maven 3.9.6..." -ForegroundColor Yellow
        $MvnZip = Join-Path $ToolsDir "maven.zip"
        $MvnUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
        
        Write-Host "Downloading Maven from $MvnUrl..." -ForegroundColor Cyan
        Invoke-WebRequest -Uri $MvnUrl -OutFile $MvnZip
        
        Write-Host "Extracting Maven..." -ForegroundColor Cyan
        if (-not (Test-Path $MavenDir)) { New-Item -ItemType Directory -Path $MavenDir | Out-Null }
        
        $TempExtract = Join-Path $ToolsDir "mvn_temp"
        if (Test-Path $TempExtract) { Remove-Item -Recurse -Force $TempExtract }
        New-Item -ItemType Directory -Path $TempExtract | Out-Null
        
        Expand-Archive -Path $MvnZip -DestinationPath $TempExtract
        
        $SubFolder = Get-ChildItem -Path $TempExtract -Directory | Select-Object -First 1
        if ($SubFolder) {
            Move-Item -Path "$($SubFolder.FullName)\*" -Destination $MavenDir -Force
        }
        
        Remove-Item -Path $MvnZip -Force
        Remove-Item -Recurse -Force $TempExtract
        
        $env:PATH = "$MavenDir\bin;" + $env:PATH
        $HasMvn = $true
        Write-Host "Maven installed locally in $MavenDir" -ForegroundColor Green
    }
} else {
    Write-Host "System Maven detected." -ForegroundColor Green
}

Write-Host "Starting Spring Boot Application..." -ForegroundColor Green
mvn clean spring-boot:run
