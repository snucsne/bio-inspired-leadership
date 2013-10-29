#!/bin/bash

# =========================================================
# Get the properties file
PROPS=$1

# Get the base output directory
DIR=$2

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
LOG_CONFIG=log-config/log4j-config.xml
MEMORYSIZE=1024M

# =========================================================
# Put all the jar files in the lib dir in the classpath
CLASSPATH=leader.jar:./:lib/:data/:cfg/
for jarFile in lib/*.jar; do
	CLASSPATH=${jarFile}:$CLASSPATH
done


# =========================================================
for ASSERTIVE in 0.2 0.5 0.8
do
    for DIRECTION in 0.25 0.50 0.75 1.00
#    for DIRECTION in 0.05 0.10 0.20 0.30
    do
        for INDCOUNT in $(seq 10 20 70)
        do

            FORMATTEDINDCOUNT=$(printf "%03d" $INDCOUNT)
            SIM_COUNT=$((2000*$INDCOUNT))
            echo Running assertiveness [ $ASSERTIVE ] and direction std dev [ $DIRECTION ] with ind count [ $FORMATTEDINDCOUNT ]

            RESULTS_DIR="$DIR/assert-$ASSERTIVE-dir-std-dev-$DIRECTION/indcount-$FORMATTEDINDCOUNT"
            if [ ! -d "$RESULTS_DIR" ]; then
                mkdir -p $RESULTS_DIR
            fi

            for RUN in {1..30}
            do
                FORMATTED_RUN=$(printf "%02d" $RUN)
                SEED=$RANDOM
                FORMATTED_SEED=$(printf "%05d" $SEED)

                RESULTS_FILE="$RESULTS_DIR/assert-dir-$FORMATTED_RUN-seed-$FORMATTED_SEED.dat"

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
                    -Dmin-direction=-$DIRECTION \
                    -Dmax-direction=$DIRECTION \
                    -Dassertiveness-mean=$ASSERTIVE \
                    edu.snu.leader.hidden.SpatialHiddenVariablesSimulation > tmp.out


#                    -Ddirection-std-dev=$DIRECTION \

            done
        done
    done
done
