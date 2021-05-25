JAR_VERSION="0.0.1"
rm -f quinaExit
native-image -cp jar:quinaExit-${JAR_VERSION}.jar quina.command.shutdown.Command quinaExit

rm -f quinaExit.build_artifacts.txt
