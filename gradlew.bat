@echo off
set GRADLE_VERSION=8.9
where gradle >nul 2>nul
if %errorlevel%==0 (
  gradle %*
  exit /b %errorlevel%
)
echo Gradle is not installed. Please install Gradle 8.9 or run the project with Android Studio.
exit /b 1
