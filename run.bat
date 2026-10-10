@echo off
echo Starting Redis, Backend, and Frontend...

start "Redis" redis-server
start "Backend (Spring Boot)" cmd /k "cd /d "%~dp0BE\E-commerce-Electronic-Components-Backend" && mvnw.cmd spring-boot:run"
start "Frontend (Nuxt)" cmd /k "cd /d "%~dp0frontend" && npm run dev"
