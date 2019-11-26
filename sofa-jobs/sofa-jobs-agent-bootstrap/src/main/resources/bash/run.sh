#/bin/bash

DIR=`dirname $0`

if [ $# -lt 1 ] ; then
  echo "run.sh  <params>"
  exit 1
fi

SERVER_NAME=sofa-jobs-agent-bootstrap


echo "RUN_ENV:$RUN_ENV"

PARAMS=$*

JAR_DIR=$APP_DIR

echo "JAR_DIR:$JAR_DIR"

export USER_MEM_ARGS="-XX:MetaspaceSize=128M  -XX:+HeapDumpOnOutOfMemoryError -XX:+UseG1GC "

export JAVA_OPTIONS="${USER_MEM_ARGS} $PARAMS -Djava.awt.headless=true -DSERVER_NAME=$SERVER_NAME -Drun_env=${RUN_ENV} \
-Dspring.profiles.active=batch,${RUN_ENV} -Djobs.log.dir=${LOG_DIR}/joblogs -Djobs.script.dir=${BIN_DIR} \
-Dcom.alipay.sofa.rpc.virtual-host=$LOCAL_IP -Dcom.alipay.sofa.rpc.registry.address=${ZOOKEEPER_IP} \
-Dspring.main.web-application-type=none -Djava.library.path=${BASE_DIR}/lib -Dlogback.configurationFile=logback_batch.xml "

echo ">> run #: java $JAVA_OPTIONS -jar $JAR_DIR/$SERVER_NAME.jar"
java $JAVA_OPTIONS -jar $JAR_DIR/$SERVER_NAME.jar
