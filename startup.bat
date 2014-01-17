ARGS="$ARGS"" -Djava.rmi.server.hostname="
ARGS="$ARGS"" -Dcom.sun.management.jmxremote.port=8990"
ARGS="$ARGS"" -Dcom.sun.management.jmxremote.authenticate=false"
ARGS="$ARGS"" -Dcom.sun.management.jmxremote.ssl=false"
ARGS="$ARGS"" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=6600"
java -server -jar $ARGS warriors.jar
