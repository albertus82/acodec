@ECHO OFF
IF "%1" == "" GOTO GUI
:CON
IF "%JAVA_HOME%" == "" java.exe -Xms@console.vm.initialHeapSize@m -Xmx@console.vm.maxHeapSize@m -jar "%~dp0@launch4j.jarName@" %1 %2 %3 %4 %5 %6
IF NOT "%JAVA_HOME%" == "" "%JAVA_HOME%\bin\java.exe" -Xms@console.vm.initialHeapSize@m -Xmx@console.vm.maxHeapSize@m -jar "%~dp0@launch4j.jarName@" %1 %2 %3 %4 %5 %6
GOTO END
:GUI
IF "%JAVA_HOME%" == "" START "" javaw.exe -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -jar "%~dp0@launch4j.jarName@"
IF NOT "%JAVA_HOME%" == "" START "" "%JAVA_HOME%\bin\javaw.exe" -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -jar "%~dp0@launch4j.jarName@"
GOTO END
:END