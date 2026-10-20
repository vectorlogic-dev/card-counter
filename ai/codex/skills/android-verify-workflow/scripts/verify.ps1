param(
    [switch]$IncludeUnitTests,
    [switch]$IncludeLint,
    [string]$RepoRoot = "."
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path $RepoRoot
$gradlew = Join-Path $root "gradlew.bat"

if (-not (Test-Path $gradlew)) {
    throw "Could not find gradlew.bat in '$root'. Run from repo root or pass -RepoRoot."
}

$androidStudioJbr = "C:\Program Files\Android\Android Studio\jbr"
if (Test-Path $androidStudioJbr) {
    $env:JAVA_HOME = $androidStudioJbr
    $env:Path = "$androidStudioJbr\bin;$env:Path"
}

$tasks = @("assembleDebug")
if ($IncludeUnitTests) {
    $tasks += "testDebugUnitTest"
}
if ($IncludeLint) {
    $tasks += "lintDebug"
}

Write-Host "Repo root: $root"
Write-Host "Using JAVA_HOME: $env:JAVA_HOME"
Write-Host "Running tasks: $($tasks -join ', ')"

Push-Location $root
try {
    & $gradlew @tasks
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
