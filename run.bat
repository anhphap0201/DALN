@echo off
echo Starting Redis, Backend, and Frontend in separate windows...

start "Redis" cmd /c "%~dp0run-redis.bat"
start "Backend (Spring Boot)" cmd /c "%~dp0run-be.bat"
start "Frontend (Nuxt)" cmd /c "%~dp0run-fe.bat"
