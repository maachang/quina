JAR_VERSION="0.0.1"
native-image -cp jar:quina-${JAR_VERSION}.jar quina.QuinaTest quinaTest
