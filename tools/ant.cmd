@echo off
rem Run the pinned Apache Ant from tools\lib (bootstraps the toolchain on first use).
rem usage: tools\ant.cmd [-f build.xml] [target...]
setlocal
set HERE=%~dp0
if not exist "%HERE%lib\ant-1.10.15.jar" python "%HERE%bootstrap.py" || exit /b 1
if not exist "%HERE%lib\ecj-3.26.0.jar" python "%HERE%bootstrap.py" || exit /b 1
java -cp "%HERE%lib\ant-1.10.15.jar;%HERE%lib\ant-launcher-1.10.15.jar" org.apache.tools.ant.Main %*
