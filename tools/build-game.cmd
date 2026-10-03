@echo off
rem Build one game's JAR + JAD. usage: tools\build-game.cmd 001 [target]
setlocal
set HERE=%~dp0
if "%~1"=="" (echo usage: %~nx0 ^<game id^> [target] & exit /b 2)
set T=%~2
if "%T%"=="" set T=dist
for /d %%D in ("%HERE%..\games\%~1-*") do (
    call "%HERE%ant.cmd" -f "%%D\build.xml" %T%
    exit /b %errorlevel%
)
echo no game with id %~1
exit /b 1
