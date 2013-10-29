#!/bin/bash

# =========================================================
# Get the properties file
PROPS=$1

# Get the base output directory
DIR=$2

# Get the locations file prefix
LOC_FILE_PREFIX=$3

# Get the individual count
INDCOUNT=$4

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
MEMORYSIZE=1024M

# =========================================================
# Put all the jar files in the lib dir in the classpath
CLASSPATH=leader.jar:./:lib/:data/:cfg/
for jarFile in lib/*.jar; do
	CLASSPATH=${jarFile}:$CLASSPATH
done


# =========================================================
# Find the next bold count to use
FORMATTEDINDCOUNT=$(printf "%03d" $INDCOUNT)
SIM_COUNT=$((2000*$INDCOUNT))
BOLDCOUNT=`./calc-next-boldcount-to-evaluate.pl $DIR/indcount-$FORMATTEDINDCOUNT/`
while [ $BOLDCOUNT -gt 0 ]
do

    FORMATTEDBOLDCOUNT=$(printf "%03d" $BOLDCOUNT)
    echo Running bold count [$FORMATTEDBOLDCOUNT] with ind count [$FORMATTEDINDCOUNT]

    RESULTS_DIR="$DIR/indcount-$FORMATTEDINDCOUNT/boldcount-$FORMATTEDBOLDCOUNT/"
    if [ ! -d "$RESULTS_DIR" ]; then
        mkdir -p $RESULTS_DIR
#        echo $RESULTS_DIR
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
                -Dmax-personality-ind-count=$BOLDCOUNT \
                -Drun-id=default \
                -Dlogname=indcount-$FORMATTEDINDCOUNT \
                edu.snu.leader.hidden.SpatialHiddenVariablesSimulation > tmp.out

    done

    BOLDCOUNT=`./calc-next-boldcount-to-evaluate.pl $DIR/indcount-$FORMATTEDINDCOUNT/`

done
