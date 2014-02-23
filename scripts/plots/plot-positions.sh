#!/bin/bash

# =========================================================
# Default values
LOG_CONFIG=log-config/log4j-config-debug.xml
MEMORYSIZE=1024M
LOC_FILE_PREFIX=cfg/sim/locations/metric/valid-metric-loc


# =========================================================
# Use the JRE in the home directory
PATH=$HOME/java/jre/bin:$PATH
JAVA_PATH=$HOME/java/jre
JAVA_HOME=$HOME/java/jre

# =========================================================
# Get the host
if [ -z $HOST ]; then
  HOST=$HOSTNAME
fi

# =========================================================
# Put all the jar files in the lib dir in the classpath
CLASSPATH=leader.jar:./:lib/:data/:cfg/
for jarFile in lib/*.jar; do
	CLASSPATH=${jarFile}:$CLASSPATH
done


# =========================================================
java -cp ${CLASSPATH} \
    -Xmx$MEMORYSIZE \
    -server \
    -Dhostname=$HOST \
    -Dlog4j.configuration=$LOG_CONFIG \
    edu.snu.leader.util.plot.PositionPlotGenerator $*

