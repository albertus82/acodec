@echo off
cls
call mvn verify -V -f "%~dp0pom.xml" -P common
call mvn verify -V -f "%~dp0pom.xml" -P cocoa-macosx-x86_64
call mvn verify -V -f "%~dp0pom.xml" -P gtk-linux-aarch64
call mvn verify -V -f "%~dp0pom.xml" -P gtk-linux-armhf
call mvn verify -V -f "%~dp0pom.xml" -P gtk-linux-x86
call mvn verify -V -f "%~dp0pom.xml" -P gtk-linux-x86_64
call mvn verify -V -f "%~dp0pom.xml" -P win32-win32-x86
call mvn verify -V -f "%~dp0pom.xml" -P win32-win32-x86_64
tree /f "%~dp0\."
pause