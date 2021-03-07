JAR_VERSION="0.0.1"
native-image -cp jar:quinaExit-${JAR_VERSION}.jar quina.command.shutdown.Command quinaExit
