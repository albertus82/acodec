#!/bin/sh
if [ "$1" = "" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -XstartOnFirstThread -Xms4m -Xmx32m -jar `dirname $0`/codec.jar >/dev/null 2>&1 &
  else java -XstartOnFirstThread -Xms4m -Xmx32m -jar `dirname $0`/codec.jar >/dev/null 2>&1 &
  fi
  osascript -e 'tell application "Terminal" to quit' &
  exit
else
if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx8m -jar `dirname $0`/codec.jar $1 $2 $3 $4 $5 $6
  else java -Xms4m -Xmx8m -jar `dirname $0`/codec.jar $1 $2 $3 $4 $5 $6
  fi
fi
