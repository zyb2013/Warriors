#!/bin/sh

WORK_PATH=/data/www/socket/warriors_dev

cd $WORK_PATH

JAVA_HOME=/data/java/jdk1.6.0_31

#ARGS="$ARGS"" -Djava.rmi.server.hostname=116.28.64.54"
ARGS="$ARGS"" -Dcom.sun.management.jmxremote.port=9000"
ARGS="$ARGS"" -Dcom.sun.management.jmxremote.ssl=false"
ARGS="$ARGS"" -Dcom.sun.management.jmxremote.authenticate=false"
$JAVA_HOME/bin/java -server -jar -Xmx2048m -Xms1024m -Xmn768m -Xss128k -XX:ThreadStackSize=128 -XX:PermSize=64m -XX:MaxPermSize=128m -XX:+UseParNewGC  -XX:+PrintGCDetails -XX:+UseConcMarkSweepGC -XX:MaxTenuringThreshold=5 -XX:CMSInitiatingOccupancyFraction=60 -XX:+UseCMSInitiatingOccupancyOnly -XX:+CMSParallelRemarkEnabled -XX:CMSFullGCsBeforeCompaction=5 -XX:+UseCMSCompactAtFullCollection $ARGS warriors.jar
