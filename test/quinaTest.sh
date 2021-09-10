#!/bin/bash
#
ARGS=${*}

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

# project version.
VERSION=0.0.1

# project name.
PROJECT_NAME=quinaTest

# execute class.
EXECUTE_CLAZZ=quina.test.QuinaTest

# JAR name.
JAR_NAME=${PROJECT_NAME}-${VERSION}.jar


# libs.
THIS_JAR_DIR=.
JAR_DIR=..
LIB_FILES=`readJar ${THIS_JAR_DIR}`:`readJar ${JAR_DIR}`

# firstMemory.
STM=128

# maxMemory.
EXM=128

# option.
OPT="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true"

# execute java.
java -Xms${STM}m -Xmx${EXM}m ${OPT} -classpath ${JAR_NAME}:${LIB_FILES} ${EXECUTE_CLAZZ} ${ARGS}