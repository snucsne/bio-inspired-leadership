#!/bin/bash

# =========================================================
# Get the properties file
PROPS=$1

# Get the base output directory
DIR=$2

# Get the locations file prefix
#LOC_FILE_PREFIX=$3
LOC_FILE_PREFIX=cfg/sim/locations/metric/valid-metric-loc

PERSONALITY=$3

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
#LOG_CONFIG=log-config/log4j-config-spatial-debug.xml
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
#for PERSONALITY in 0.2 0.5 0.8
do
#    for INDCOUNT in 10 15 20 25 30 40 50 60 70 80 90 #100 125 150
#    for PERSONALITY in 0.2 0.5 0.8
#    do

        FORMATTEDINDCOUNT=$(printf "%03d" $INDCOUNT)
        SIM_COUNT=$((2000*$INDCOUNT))
        echo Running personality [$PERSONALITY] with ind count [$FORMATTEDINDCOUNT]

        RESULTS_DIR="$DIR/personality-$PERSONALITY/indcount-$FORMATTEDINDCOUNT"
        if [ ! -d "$RESULTS_DIR" ]; then
            mkdir -p $RESULTS_DIR
        fi

        for RUN in {1..50}
        do
            FORMATTED_RUN=$(printf "%02d" $RUN)
            SEED=$RUN
            FORMATTED_SEED=$(printf "%05d" $SEED)

            RESULTS_FILE="$RESULTS_DIR/spatial-hidden-var-$FORMATTED_RUN-seed-$FORMATTED_SEED.dat"
            LOC_FILE="$LOC_FILE_PREFIX-$FORMATTEDINDCOUNT-seed-$FORMATTED_SEED.dat"


            # Run the simulation
            java -cp $CLASSPATH \
                    -Xmx$MEMORYSIZE \
                    -server \
                    -Dhostname=$HOST \
                    -Dlog4j.configuration=$LOG_CONFIG \
                    -Drandom-seed=$SEED \
                    -Dsim-properties=$PROPS \
                    -Dresults-file=$RESULTS_FILE \
                    -Dindividual-count=$INDCOUNT \
                    -Dsimulation-count=$SIM_COUNT \
                    -Dlocations-file=$LOC_FILE \
                    -Dpersonality-mean=$PERSONALITY \
                    -Drun-id=default \
                    -Dlogname=personality-$PERSONALITY \
                    edu.snu.leader.hidden.LocalSpatialSimulation > tmp.out
        done
#    done
done
