@echo off
echo Starting Redis, Ollama AI, Backend, and Frontend in separate windows...

start "Redis" cmd /c "%~dp0run-redis.bat"
start "Ollama AI" cmd /c "%~dp0run-ollama.bat"
start "Backend (Spring Boot)" cmd /c "%~dp0run-be.bat"
start "Frontend (Nuxt)" cmd /c "%~dp0run-fe.bat"
