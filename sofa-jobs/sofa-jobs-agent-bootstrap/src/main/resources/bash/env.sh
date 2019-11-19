#!/bin/sh

export BASE_DIR=/home/dev_nuis/jobs
export APP_DIR=$BASE_DIR/application
export BIN_DIR=$BASE_DIR/bin
export LOG_DIR=$BASE_DIR/logs
export ETC_DIR=$BASE_DIR/etc
export LIB_DIR=$BASE_DIR/lib
export TMP_DIR=$BASE_DIR/tmp

export FILE_ENCODING=utf8
export JAVA_VENDOR=Sun
export PRODUCT_MODE=t
export RUN_ENV=dev
export ENV=dev
export TRACE_PREFIX=E0
export APP_ID=netpay
export APOLLO_META=http://144.131.254.141:9980,http://144.131.254.141:9999
export APOLLO_CACHE_DIR=$BASE_DIR/cache
export APOLLO_CLUSTER=TEST

export JAVA_HOME=/usr/local/jdk1.8.0_121
export PATH=$JAVA_HOME/bin:$PATH

JAR_FILES=`find $LIB_DIR -name "*.jar" | paste -d: -s`
export MY_CLASSPATH=$ETC_DIR:$JAR_FILES

export LOCAL_IP=`LC_ALL=C /sbin/ifconfig eth0|grep "inet addr:"|cut -d: -f2|awk '{print $1}'`
