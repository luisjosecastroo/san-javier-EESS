@echo off
cd /d "%~dp0"
java -jar EstacionSanJavier.jar
if errorlevel 1 pause
