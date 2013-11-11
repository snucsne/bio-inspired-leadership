#!/bin/bash

# =========================================================
# Get the properties file
PROPS=$1

# Get the base output directory
DIR=$2

# Get the initial personality
PERSONALITY=$3

# Get the decay calculator
DECAY_CALC=$4

# Get the decay time multiplier
DECAY_TIME_MULT=$5


# =========================================================
# Get the host
if [ -z $HOST ]; then
  HOST=$HOSTNAME
fi


# =========================================================
# Default values
LOG_CONFIG=log-config/log4j-oscer-config.xml
MEMORYSIZE=1024M
LOC_FILE_PREFIX=cfg/sim/locations/metric/valid-metric-loc


# =========================================================
# Use the JRE in the home directory
PATH=$HOME/java/jre/bin:$PATH
JAVA_PATH=$HOME/java/jre
JAVA_HOME=$HOME/java/jre


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
LOG_CONFIG=log-config/log4j-oscer-config.xml
#LOG_CONFIG=log-config/log4j-config-debug.xml
MEMORYSIZE=1024M

# =========================================================
# Put all the jar files in the lib dir in the classpath
CLASSPATH=leader.jar:./:lib/:data/:cfg/
for jarFile in lib/*.jar; do
	CLASSPATH=${jarFile}:$CLASSPATH
done


# =========================================================
#for PERSONALITY in 0.2 0.5 0.8
#do
    for INDCOUNT in $(seq 10 10 50)
    do

        FORMATTEDINDCOUNT=$(printf "%03d" $INDCOUNT)
        SIM_COUNT=$((2000*$INDCOUNT))
        DECAY_TIME=$(($DECAY_TIME_MULT*$INDCOUNT))
        #echo Running personality [$PERSONALITY] with ind count [$FORMATTEDINDCOUNT]

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
                    -Dlogname=decay \
                    -Dpersonality-decay-calculator=$DECAY_CALC \
                    -Ddecay-time=$DECAY_TIME \
                    edu.snu.leader.hidden.SpatialHiddenVariablesSimulation > tmp-$$.out

        done
    done
#done

