@echo off
title Frontend (Nuxt)
echo Starting Nuxt Frontend on port 3000...
cd /d "%~dp0frontend"
call npm run dev
pause
