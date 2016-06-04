#!/bin/sh
if [ "$1" = "" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx32m -classpath "codec.jar:lib/*" it.albertus.codec.Codec
  else java -Xms4m -Xmx32m -classpath "codec.jar:lib/*" it.albertus.codec.Codec
  fi
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx8m -classpath "codec.jar:lib/*" it.albertus.codec.Codec $1 $2 $3 $4 $5 $6
  else java -Xms4m -Xmx8m -classpath "codec.jar:lib/*" it.albertus.codec.Codec $1 $2 $3 $4 $5 $6
  fi
fi
