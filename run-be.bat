@echo off
title Backend (Spring Boot)
echo Starting Spring Boot Backend on port 8080...
cd /d "%~dp0BE\E-commerce-Electronic-Components-Backend"
call mvnw.cmd spring-boot:run
pause
