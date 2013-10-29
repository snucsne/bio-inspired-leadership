#!/bin/bash

# =========================================================
# Get the properties file
PROPS=$1

# Get the output prefix
OUTPUT_PREFIX=$2

# Get the experiment type
TYPE=$3
EXP_TYPE="topo"
BUILDER="edu.snu.leader.hidden.DefaultIndividualBuilder"
if [ $TYPE == "metric" ]; then
  EXP_TYPE="metric"
  BUILDER="edu.snu.leader.hidden.MetricIndividualBuilder"
fi

# Get the locations file prefix
LOC_FILE_PREFIX=$4


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
LOG_CONFIG=log-config/log4j-config-unique.xml
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
for INDCOUNT in $(seq 10 10 100)
do

    FORMATTEDINDCOUNT=$(printf "%04d" $INDCOUNT)
    FORMATTEDINDCOUNTTHREE=$(printf "%03d" $INDCOUNT)
    SIM_COUNT=$((2000*$INDCOUNT))
    RESULTS_DIR="$OUTPUT_PREFIX-indcount-$FORMATTEDINDCOUNT-type-$EXP_TYPE"
    if [ ! -d "$RESULTS_DIR" ]; then
        mkdir -p $RESULTS_DIR
    fi

    echo Running with ind count [$FORMATTEDINDCOUNT] type [$EXP_TYPE]

    for RUN in {1..40}
    do
        FORMATTED_RUN=$(printf "%02d" $RUN)
        SEED=$RUN
        FORMATTED_SEED=$(printf "%05d" $SEED)
        RESULTS_FILE="$RESULTS_DIR/spatial-hidden-var-$FORMATTED_RUN-seed-$FORMATTED_SEED.dat"
        LOC_FILE="$LOC_FILE_PREFIX-$FORMATTEDINDCOUNTTHREE-seed-$FORMATTED_SEED.dat"

        # Run the simulation
        java -cp $CLASSPATH \
                    -Xmx$MEMORYSIZE \
                    -server \
                    -Dhostname=$HOST \
                    -Dlog4j.configuration=$LOG_CONFIG \
                    -Drandom-seed=$SEED \
                    -Dsim-properties=$PROPS \
                    -Dsimulation-count=$SIM_COUNT \
                    -Dresults-file=$RESULTS_FILE \
                    -Dindividual-count=$INDCOUNT \
                    -Dsimulation-count=$SIM_COUNT \
                    -Dindividual-builder-class=$BUILDER \
                    -Dlocations-file=$LOC_FILE \
                    -Drun-id=$EXP_TYPE-$RUN \
                    edu.snu.leader.hidden.SpatialHiddenVariablesSimulation
    done
done

