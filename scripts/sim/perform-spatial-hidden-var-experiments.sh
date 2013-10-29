#!/bin/bash

# =========================================================
# Get the properties file
PROPS=$1

# Get the output prefix
OUTPUT_PREFIX=$2

# =========================================================
# Build the directory if it doesn't exist
DIR=`dirname $OUTPUT_PREFIX`
if [ ! -d "$DIR" ]; then
    mkdir -p $DIR
fi

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
for INDCOUNT in 10 15 20 25 30 35 40 45 50 55 60 65 70 75 80 85 90 95 100;
do
    for USENNGROUPSIZE in "true" "false";
    do

        FORMATTEDINDCOUNT=$(printf "%03d" $INDCOUNT)
        RESULTS_FILE="$OUTPUT_PREFIX-indcount-$FORMATTEDINDCOUNT-usenn-$USENNGROUPSIZE.dat"

        echo Running with ind count [$FORMATTEDINDCOUNT] use flag [$USENNGROUPSIZE]

        # Run the simulation
        java -cp $CLASSPATH \
                -Xmx$MEMORYSIZE \
                -server \
                -Dhostname=$HOST \
                -Dlog4j.configuration=$LOG_CONFIG \
                -Dsim-properties=$PROPS \
                -Dresults-file=$RESULTS_FILE \
                -Dindividual-count=$INDCOUNT \
                -Duse-nearest-neighbor-group-size=$USENNGROUPSIZE \
                edu.snu.leader.hidden.SpatialHiddenVariablesSimulation

    done
done
