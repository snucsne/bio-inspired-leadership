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

    for USENNGROUPSIZE in "true" "false";
    do

#        for FOLLOWK in 0.0 -0.25 -0.5 -0.7 -0.8 -0.9 -0.95 -0.99
        for FOLLOWK in -0.91 -0.92 -0.93 -0.94 -0.96 -0.97 -0.98
        do

            FORMATTEDFOLLOWK=$(printf "%04.2f" $FOLLOWK)

            RESULTS_DIR="$OUTPUT_PREFIX-indcount-$FORMATTEDINDCOUNT-usenn-$USENNGROUPSIZE-follow-$FORMATTEDFOLLOWK"
            if [ ! -d "$RESULTS_DIR" ]; then
                mkdir -p $RESULTS_DIR
            fi

            echo Running with ind count [$FORMATTEDINDCOUNT] use flag [$USENNGROUPSIZE] follow [$FORMATTEDFOLLOWK]

            for RUN in {1..30}
            do
                FORMATTED_RUN=$(printf "%02d" $RUN)
                SEED=$RUN
                FORMATTED_SEED=$(printf "%05d" $SEED)
                RESULTS_FILE="$RESULTS_DIR/follow-spatial-hidden-var-$FORMATTED_RUN-seed-$FORMATTED_SEED.dat"


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
                        -Dfollow-factor-k-multiplier=$FOLLOWK \
                        edu.snu.leader.hidden.SpatialHiddenVariablesSimulation

            done
        done
    done
done

