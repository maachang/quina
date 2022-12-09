#!/bin/bash

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

MAIN_PACKAGE=quina.test.QuinaTest

NATIVE_OUT=quinaTest

THIS_JAR_DIR=.
JAR_DIR=..
LIB_DIR=./lib

# JARライブラリ群.
JAR_FILES=`readJar ${THIS_JAR_DIR}`:`readJar ${JAR_DIR}`:`readJar ${LIB_DIR}`

# gc.
OPTIONS="--gc=serial"

# ヒープメモリ.
OPTIONS="${OPTIONS} -R:MaximumHeapSizePercent=80"

# 詳細表示.
OPTIONS="${OPTIONS} --verbose"
# JDKが必要なNative化の場合エラーにする.
OPTIONS="${OPTIONS} --no-fallback"
# charset.jarを取り込む.
OPTIONS="${OPTIONS} -H:+AddAllCharsets"
# コンパイルエラー時の例外出力.
OPTIONS="${OPTIONS} -H:+ReportExceptionStackTraces"
# ビルド時にエラーを発生させないようにする.
OPTIONS="${OPTIONS} -H:+ReportUnsupportedElementsAtRuntime"
# 不完全なクラスパスでイメージをビルドできるようにします.
OPTIONS="${OPTIONS} --allow-incomplete-classpath"

# nativeImageConfigディレクトリ.
NATIVE_IMAGE_CONFIG="./nativeImageConfig"

# コンパイル定義コンフィグ.
OPTIONS="${OPTIONS} -H:ReflectionConfigurationFiles=${NATIVE_IMAGE_CONFIG}/reflection.json"
OPTIONS="${OPTIONS} -H:JNIConfigurationFiles=${NATIVE_IMAGE_CONFIG}/jni.json"
OPTIONS="${OPTIONS} -H:ResourceConfigurationFiles=${NATIVE_IMAGE_CONFIG}/resource.json"
OPTIONS="${OPTIONS} -H:DynamicProxyConfigurationFiles=${NATIVE_IMAGE_CONFIG}/proxy.json"

# Native-Imageでコンパイル時に判別する.
NATIVE_BUILDS="`cat ${NATIVE_IMAGE_CONFIG}/initializeAtBuildTime.txt`"
if [ "${NATIVE_BUILDS}" != "" ]; then
    OPTIONS="${OPTIONS} --initialize-at-build-time=${NATIVE_BUILDS}"
fi

# Native-Imageコンパイル後実行時に判別する.
NATIVE_RUNTIMES="`cat ${NATIVE_IMAGE_CONFIG}/initializeAtRunTime.txt`"
if [ "${NATIVE_RUNTIMES}" != "" ]; then
    OPTIONS="${OPTIONS} --initialize-at-run-time=${NATIVE_RUNTIMES}"
fi


rm -f ${NATIVE_OUT}
echo native-image ${OPTIONS}  -cp ${JAR_FILES} ${MAIN_PACKAGE} ${NATIVE_OUT}
native-image ${OPTIONS} -cp ${JAR_FILES} ${MAIN_PACKAGE} ${NATIVE_OUT}

rm -f ${NATIVE_OUT}.build_artifacts.txt
