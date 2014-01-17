#!/bin/sh
PATH_LIB=./lib
CLASSPATH=./resources

for jar in `ls $PATH_LIB/*.jar`
do
      CLASSPATH="$CLASSPATH:""$jar"
done

        ARGS="$ARGS"" -Djava.rmi.server.hostname="
        ARGS="$ARGS"" -Dcom.sun.management.jmxremote.port=8990"
        ARGS="$ARGS"" -Dcom.sun.management.jmxremote.authenticate=false" 
        ARGS="$ARGS"" -Dcom.sun.management.jmxremote.ssl=false" 
		java -server -Xms2046m -Xmx4096m -XX:PermSize=256m -XX:MaxPermSize=512m $ARGS -classpath "$CLASSPATH" com.yayo.warriors.YayoDaemon