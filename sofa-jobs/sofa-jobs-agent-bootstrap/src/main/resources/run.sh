#/bin/bash

DIR=`dirname $0`

if [ $# -lt 1 ] ; then
  echo "run.sh  <params>"
  exit 1
fi

SERVER_NAME=sofa-jobs-agent-bootstrap


#JOB_NAME="NAME"
#RUN_ENV=""
#if [ $1 = "--run_env" ] ; then
#	RUN_ENV=$2
#else
 #   RUN_ENV=$1
#fi
echo "RUN_ENV:$RUN_ENV"

#shift
#shift
PARAMS=$*


#export BASE_DIR=/home/nuis/jobs
JAR_DIR=$APP_DIR

echo "JAR_DIR:$JAR_DIR"


export USER_MEM_ARGS="-Xms64m -Xmx512m"

export JAVA_OPTIONS="${USER_MEM_ARGS} $PARAMS -Djava.awt.headless=true -DSERVER_NAME=$SERVER_NAME -Drun_env=${RUN_ENV} \
-Dspring.profiles.active=batch,${RUN_ENV} -Djobs.log.dir=${LOG_DIR}/joblogs -Djobs.script.dir=${BIN_DIR}\
-Dspring.main.web-application-type=none -Djava.library.path=${BASE_DIR}/lib -Dlogback.configurationFile=logback_batch.xml "

#java $JAVA_OPTIONS -cp $CLASSPATH com.chinaums.netpay.jobs.Main $PARAMS

echo ">> run #: java $JAVA_OPTIONS -jar $JAR_DIR/$SERVER_NAME.jar"
# > /dev/null 2>&1 &
java $JAVA_OPTIONS -jar $JAR_DIR/$SERVER_NAME.jar
