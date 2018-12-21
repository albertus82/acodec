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
  then "$JAVA_HOME/bin/java" -DSWT_GTK3=0 -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -classpath "$PRGDIR/@artifactId@.jar:$PRGDIR/lib/*" @mainClass@
  else java -DSWT_GTK3=0 -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -classpath "$PRGDIR/@artifactId@.jar:$PRGDIR/lib/*" @mainClass@
  fi
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms@console.vm.initialHeapSize@m -Xmx@console.vm.maxHeapSize@m -classpath "$PRGDIR/@artifactId@.jar:$PRGDIR/lib/*" @mainClass@ $1 $2 $3 $4 $5 $6
  else java -Xms@console.vm.initialHeapSize@m -Xmx@console.vm.maxHeapSize@m -classpath "$PRGDIR/@artifactId@.jar:$PRGDIR/lib/*" @mainClass@ $1 $2 $3 $4 $5 $6
  fi
fi
