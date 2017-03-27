#!/bin/bash

# =========================================================
# Get the properties file
PROPS=$1

# Get the base output directory
DIR=$2

# Get the locations file prefix
#LOC_FILE_PREFIX=$3
LOC_FILE_PREFIX=cfg/sim/locations/metric/valid-metric-loc

# =========================================================
# Build the directory if it doesn't exist
if [ ! -d "$DIR" ]; then
    mkdir -p $DIR
fi

# Get the host
if [ -z $HOST ]; then
  HOST=$HOSTNAME
fi

# Default values
#LOG_CONFIG=log-config/log4j-config-debug.xml
LOG_CONFIG=log-config/log4j-oscer-config.xml
MEMORYSIZE=1024M

# =========================================================
# Put all the jar files in the lib dir in the classpath
CLASSPATH=leader.jar:./:lib/:data/:cfg/
for jarFile in lib/*.jar; do
	CLASSPATH=${jarFile}:$CLASSPATH
done

# =========================================================
for INDCOUNT in 20 30 40 50 #50 #15 25 100 125 150
do

    FORMATTED_IND_COUNT=$(printf "%03d" $INDCOUNT)
    RESULTS_DIR="$DIR/indcount-$FORMATTED_IND_COUNT"

    if [ ! -d "$RESULTS_DIR" ]; then
        mkdir -p $RESULTS_DIR
    fi

    for MAP in {1..50}
    do
        FORMATTED_MAP=$(printf "%05d" $MAP)
        LOC_FILE="$LOC_FILE_PREFIX-$FORMATTED_IND_COUNT-seed-$FORMATTED_MAP.dat"

#        echo IndCount [$INDCOUNT]  Map [$MAP]

        RESULTS_FILE="$RESULTS_DIR/analyze-position-map-$FORMATTED_MAP.dat"

        # Run the simulation
        java -cp $CLASSPATH \
                -Xmx$MEMORYSIZE \
                -server \
                -Dhostname=$HOST \
                -Dlog4j.configuration=$LOG_CONFIG \
                -Drandom-seed=$MAP \
                -Dsim-properties=$PROPS \
                -Dresults-file=$RESULTS_FILE \
                -Dindividual-count=$INDCOUNT \
                -Dlocations-file=$LOC_FILE \
                -Dinitiator-id=$INITIATOR_ID \
                -Drun-id=default-$$ \
                -Dlogname=restricted \
                edu.snu.leader.hidden.util.PositionAnalyzer > tmp.out

    done
done
