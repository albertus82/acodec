#!/bin/sh
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
mvn verify -V -f "$PRGDIR/pom.xml" -P common
mvn verify -V -f "$PRGDIR/pom.xml" -P cocoa-macosx-x86_64
mvn verify -V -f "$PRGDIR/pom.xml" -P gtk-linux-aarch64
mvn verify -V -f "$PRGDIR/pom.xml" -P gtk-linux-armhf
mvn verify -V -f "$PRGDIR/pom.xml" -P gtk-linux-x86
mvn verify -V -f "$PRGDIR/pom.xml" -P gtk-linux-x86_64
mvn verify -V -f "$PRGDIR/pom.xml" -P win32-win32-x86
mvn verify -V -f "$PRGDIR/pom.xml" -P win32-win32-x86_64
tree "$PRGDIR/."
