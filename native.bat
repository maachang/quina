@set JAR_VERSION="0.0.1"
@del quinaTest.exe
@native-image --no-server -cp jar;quina-%JAR_VERSION%.jar quina.QuinaTest quinaTest

@del quinaTest.idb
@del quinaTest.exp
@del quinaTest.stripped.pdb
@del quinaTest.lib

