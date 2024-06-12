@echo off
setlocal enabledelayedexpansion

:: Function to check if Docker is installed
where docker >nul 2>nul
IF %ERRORLEVEL% NEQ 0 (
    echo Docker is not installed. Please install Docker Desktop from https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

:: Function to check if Docker Desktop is running
docker info >nul 2>nul
IF %ERRORLEVEL% NEQ 0 (
    echo Docker Desktop is not running. Please start Docker Desktop.
    pause
    exit /b 1
)

:: Function to check if WSL 2 is enabled
wsl -l -v | findstr /I "WSL 2" >nul
IF %ERRORLEVEL% NEQ 0 (
    echo WSL 2 is not enabled. Please enable 'Use the WSL 2 based engine' in Docker Desktop settings.
    pause
    exit /b 1
)

:: Run Docker Compose
docker-compose up --build
pause
