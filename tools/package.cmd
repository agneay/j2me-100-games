@echo off
rem Build every game and collect release files + the all-games ZIP into release\
setlocal
set HERE=%~dp0
call "%HERE%build-all.cmd" || exit /b 1
call "%HERE%ant.cmd" -f "%HERE%..\build.xml" -Dskip.dist=true package
