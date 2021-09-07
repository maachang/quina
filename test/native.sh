#!/bin/bash

# readJarFileName.
readJar() {
    CLASSPATH=
    for DIR in $*; do
        if [ "x`ls $DIR`" != "x" ]; then
            for JAR in ` ls $DIR`; do
                if [ -d "${DIR}/${JAR}" ]; then
                    JAR=`readJar "${DIR}/${JAR}"`
                    if [ "x$CLASSPATH" = "x" ]; then
                        CLASSPATH=${JAR}
                    else
                        CLASSPATH=$CLASSPATH:${JAR}
                    fi
                elif [ -f "${DIR}/${JAR}" ]; then
                    if [ "x$CLASSPATH" = "x" ]; then
                        CLASSPATH=${DIR}/${JAR}
                    else
                        CLASSPATH=$CLASSPATH:${DIR}/${JAR}
                    fi
                fi
            done
        fi
    done
    echo $CLASSPATH
}

MAIN_PACKAGE=quina.test.QuinaTest

NATIVE_OUT=quinaTest

THIS_JAR_DIR=./
JAR_DIR=../

## JAR_FILES=`readJar ${THIS_JAR_DIR}`:`readJar ${JAR_DIR}`
JAR_FILES=./quinaTest-0.0.1.jar:../quina-0.0.1.jar

rm -f ${NATIVE_OUT}
echo native-image -H:+ReportExceptionStackTraces -cp jar:${JAR_FILES} ${MAIN_PACKAGE} ${NATIVE_OUT}
native-image -H:+ReportExceptionStackTraces -cp jar:${JAR_FILES} ${MAIN_PACKAGE} ${NATIVE_OUT}

rm -f ${NATIVE_OUT}.build_artifacts.txt
