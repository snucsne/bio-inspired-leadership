#!/bin/bash

# Get the host
if [ -z $HOST ]; then
  HOST=$HOSTNAME
fi

# Default values
LOG_CONFIG=log-config/log4j-config.xml
MEMORYSIZE=1024M
SEED=42

# Put all the jar files in the lib dir in the classpath
CLASSPATH=leader.jar:./:lib/:data/:cfg/
for jarFile in lib/*.jar; do
	CLASSPATH=${jarFile}:$CLASSPATH
done

# Run the application
java -cp $CLASSPATH \
        -Xmx$MEMORYSIZE \
        -server \
        -Dhostname=$HOST \
        -Dlog4j.configuration=$LOG_CONFIG \
        edu.snu.leader.hierarchy.simple.HierarchyBuildTest 

