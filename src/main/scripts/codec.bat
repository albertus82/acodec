@ECHO OFF
IF "%1" == "" GOTO GUI
:CON
IF "%JAVA_HOME%" == "" java.exe -Xms4m -Xmx8m -jar "%~dp0codec.jar" %1 %2 %3
IF NOT "%JAVA_HOME%" == "" "%JAVA_HOME%\bin\java.exe" -Xms4m -Xmx8m -jar "%~dp0codec.jar" %1 %2 %3
GOTO END
:GUI
IF "%JAVA_HOME%" == "" START "" javaw.exe -Xms4m -Xmx32m -jar "%~dp0codec.jar"
IF NOT "%JAVA_HOME%" == "" START "" "%JAVA_HOME%\bin\javaw.exe" -Xms4m -Xmx32m -jar "%~dp0codec.jar"
GOTO END
:END