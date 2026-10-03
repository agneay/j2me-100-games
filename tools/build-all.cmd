@echo off
rem Build JAR + JAD for every game (parallel). usage: tools\build-all.cmd [--jobs N]
setlocal
set HERE=%~dp0
python "%HERE%bootstrap.py" --no-teavm || exit /b 1
call "%HERE%ant.cmd" -q -f "%HERE%..\build.xml" tools || exit /b 1
python "%HERE%build_all.py" --target dist %*
