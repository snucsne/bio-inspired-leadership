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
# Default run values
SIM_COUNT=20000

# =========================================================
#for INDCOUNT in 10 15 20 25 30 35 40 45 50 55 60 65 70 75 80 85 90 95 100;
for INDCOUNT in $(seq 20 10 40)
do

    FORMATTEDINDCOUNT=$(printf "%04d" $INDCOUNT)

#    for USENNGROUPSIZE in "true" "false";
#    do
        USENNGROUPSIZE="true"

#        for CANCELK in 0.0 0.25 0.5 0.75 1.0 1.25 1.5 1.75 2.0
#        for CANCELK in 0.5 1.0 1.5 2.0 2.5 3.0 3.5 4.0 4.5 5.0
        for CANCELK in $(seq 0 5 40)
        do

            FORMATTEDCANCELK=$(printf "%02d" $CANCELK)

            RESULTS_DIR="$OUTPUT_PREFIX-indcount-$FORMATTEDINDCOUNT-usenn-$USENNGROUPSIZE-cancel-$FORMATTEDCANCELK"
            if [ ! -d "$RESULTS_DIR" ]; then
                mkdir -p $RESULTS_DIR
            fi

            echo Running with ind count [$FORMATTEDINDCOUNT] use flag [$USENNGROUPSIZE] cancel [$FORMATTEDCANCELK]

            for RUN in {1..30}
            do
                FORMATTED_RUN=$(printf "%02d" $RUN)
                SEED=$RUN
                FORMATTED_SEED=$(printf "%05d" $SEED)
                RESULTS_FILE="$RESULTS_DIR/cancel-spatial-hidden-var-$FORMATTED_RUN-seed-$FORMATTED_SEED.dat"


                # Run the simulation
                java -cp $CLASSPATH \
                        -Xmx$MEMORYSIZE \
                        -server \
                        -Dhostname=$HOST \
                        -Dlog4j.configuration=$LOG_CONFIG \
                        -Drandom-seed=$SEED \
                        -Dsimulation-count=$SIM_COUNT \
                        -Dsim-properties=$PROPS \
                        -Dresults-file=$RESULTS_FILE \
                        -Dindividual-count=$INDCOUNT \
                        -Dsimulation-count=$SIM_COUNT \
                        -Duse-nearest-neighbor-group-size=$USENNGROUPSIZE \
                        -Dcancel-factor-k-multiplier=$CANCELK \
                        edu.snu.leader.hidden.SpatialHiddenVariablesSimulation

            done
        done
done

