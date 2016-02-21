if [ "$1" = "" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx8m -jar codec.jar
  else java -Xms4m -Xmx8m -jar codec.jar $1 $2 $3
  fi
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms8m -Xmx32m -jar codec.jar
  else java -Xms8m -Xmx32m -jar codec.jar
  fi
fi
