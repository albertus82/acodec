#!/bin/sh
if [ "$1" = "" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -XstartOnFirstThread -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -jar `dirname $0`/@macos.jarFileName@ >/dev/null 2>&1 &
  else java -XstartOnFirstThread -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -jar `dirname $0`/@macos.jarFileName@ >/dev/null 2>&1 &
  fi
  osascript -e 'tell application "Terminal" to quit' &
  exit
else
if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -jar `dirname $0`/@macos.jarFileName@ $1 $2 $3 $4 $5 $6
  else java -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -jar `dirname $0`/@macos.jarFileName@ $1 $2 $3 $4 $5 $6
  fi
fi
