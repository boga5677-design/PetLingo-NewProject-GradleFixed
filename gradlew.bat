@echo off
where gradle >nul 2>nul
if errorlevel 1 (
  echo ERROR: Gradle is not installed or not available in PATH.
  echo Install Gradle 8.10.2 or use the included GitHub Actions workflow.
  exit /b 1
)
gradle %*
