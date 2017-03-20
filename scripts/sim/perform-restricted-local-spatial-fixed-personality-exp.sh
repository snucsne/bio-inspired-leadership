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

    for MAP in {1..50}
    do
        FORMATTED_MAP=$(printf "%05d" $MAP)
        LOC_FILE="$LOC_FILE_PREFIX-$FORMATTED_IND_COUNT-seed-$FORMATTED_MAP.dat"

        RESULTS_DIR="$DIR/indcount-$FORMATTED_IND_COUNT/map-$FORMATTED_MAP"
        if [ ! -d "$RESULTS_DIR" ]; then
            mkdir -p $RESULTS_DIR
        fi
        MAX_ID=`expr $INDCOUNT - 1`

        echo IndCount [$INDCOUNT]  Map [$MAP]


        for INITIATOR in $(seq 0 $MAX_ID);      
        do
            INITIATOR_ID=$(printf "Ind%05d" $INITIATOR)
#            echo Map [$MAP]  InitiatorID [$INITIATOR_ID]

            RESULTS_FILE="$RESULTS_DIR/spatial-hidden-var-map-$FORMATTED_MAP-initiator-$INITIATOR_ID.dat"

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
                    -Doverride-personality.00.id=$INITIATOR_ID \
                    -Drun-id=default \
                    -Dlogname=restricted-fixed \
                    edu.snu.leader.hidden.LocalSpatialSimulation > tmp.out
        done
    done
done
