#!/bin/bash

# =========================================================
# Get the min neighbor separation
MINSEP=$1

# Get the max neighbor separation
MAXSEP=$2

# Get the output file prefix
PREFIX=$3

# =========================================================
# Get the host
if [ -z $HOST ]; then
  HOST=$HOSTNAME
fi

# Default values
LOG_CONFIG=log-config/log4j-config.xml
MEMORYSIZE=1024M

# =========================================================
# Put all the jar files in the lib dir in the classpath
CLASSPATH=leader.jar:./:lib/:data/:cfg/
for jarFile in lib/*.jar; do
	CLASSPATH=${jarFile}:$CLASSPATH
done

# =========================================================
for LOCCOUNT in $(seq 10 5 100)
do

    FORMATTEDLOCCOUNT=$(printf "%03d" $LOCCOUNT)
    FULLPREFIX="$PREFIX-loc-$FORMATTEDLOCCOUNT-seed-"

    # Run the builder
    java -cp $CLASSPATH \
            -Xmx$MEMORYSIZE \
            -server \
            -Dhostname=$HOST \
            -Dlog4j.configuration=$LOG_CONFIG \
            edu.snu.leader.util.LocationBuilder \
            $LOCCOUNT \
            $MINSEP \
            $MAXSEP \
            1 \
            40 \
            $FULLPREFIX 

done
