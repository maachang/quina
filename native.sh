JAR_VERSION="0.0.1"
rm -f quinaTest
native-image --no-server -cp jar:quina-${JAR_VERSION}.jar quina.QuinaTest quinaTest
