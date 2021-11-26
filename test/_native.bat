@set QUINA_JAR_VERSION="0.0.1"
@set QUINA_TEST_JAR_VERSION="0.0.1"
@del quinaTest.exe
@native-image --no-server -cp jar;..\quina-%QUINA_JAR_VERSION%.jar;.\quina-Test-%QUINA_TEST_JAR_VERSION%.jar quina.test.QuinaTest quinaTest

@del quinaTest.idb
@del quinaTest.exp
@del quinaTest.stripped.pdb
@del quinaTest.lib

