#!/bin/bash
#
ARGS=${*}

# readJarFileName.
readJar() {
    CLASSPATH=
    for DIR in $*; do
        if [ "x`ls $DIR`" != "x" ]; then
            for JAR in ` ls $DIR`; do
                FILEPATH="${DIR}/${JAR}"
                if [ ${FILEPATH##*.} = "jar" ]; then
                    if [ -f "${DIR}/${JAR}" ]; then
                        if [ "x$CLASSPATH" = "x" ]; then
                            CLASSPATH=${DIR}/${JAR}
                        else
                            CLASSPATH=$CLASSPATH:${DIR}/${JAR}
                        fi
                    fi
                fi
            done
        fi
    done
    echo $CLASSPATH
}

# execute class.
EXECUTE_CLAZZ=quina.test.QuinaTestLmd

# libs.
THIS_JAR_DIR=.
JAR_DIR=..
LIB_DIR=./lib
LIB_FILES=`readJar ${THIS_JAR_DIR}`:`readJar ${JAR_DIR}`:`readJar ${LIB_DIR}`

# firstMemory.
STM=128

# maxMemory.
EXM=128

# option.
OPT="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true"

# execute java.
java -Xms${STM}m -Xmx${EXM}m ${OPT} -classpath ${LIB_FILES} ${EXECUTE_CLAZZ} ${ARGS}
