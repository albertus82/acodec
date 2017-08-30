#!/bin/sh
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
if [ "$1" = "" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -DSWT_GTK3=0 -Xms8m -Xmx32m -classpath "$PRGDIR/codec.jar:$PRGDIR/lib/*" it.albertus.codec.Codec
  else java -DSWT_GTK3=0 -Xms8m -Xmx32m -classpath "$PRGDIR/codec.jar:$PRGDIR/lib/*" it.albertus.codec.Codec
  fi
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx16m -classpath "$PRGDIR/codec.jar:$PRGDIR/lib/*" it.albertus.codec.Codec $1 $2 $3 $4 $5 $6
  else java -Xms4m -Xmx16m -classpath "$PRGDIR/codec.jar:$PRGDIR/lib/*" it.albertus.codec.Codec $1 $2 $3 $4 $5 $6
  fi
fi
