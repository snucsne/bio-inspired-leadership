#!/bin/bash

# =========================================================
# Get the properties file
PROPS=$1

# Get the output prefix
OUTPUT_PREFIX=$2

# Get the individual count
INDIVIDUAL_COUNT=$3

# Get the motivation time increase
MOTIVATION_TIME_INCREASE=$4

# Get the motivation neighbor increase
MOTIVATION_NEIGHBOR_INCREASE=$5


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
# Run through the simulation a number of times
for SEED in {1..40};
do
    FORMATTED_SEED=$(printf "%02d" $SEED)
    OUTPUT_FILE="$OUTPUT_PREFIX-seed-$FORMATTED_SEED.dat"

    echo Running with seed [$FORMATTED_SEED]


    # Run the simulation
    java -cp $CLASSPATH \
            -Xmx$MEMORYSIZE \
            -server \
            -Dhostname=$HOST \
            -Dlog4j.configuration=$LOG_CONFIG \
            -Dsim-properties=$PROPS \
            -Drandom-seed=$SEED \
            -Dstats-file=$OUTPUT_FILE \
            -Dindividual-count=$INDIVIDUAL_COUNT \
            -Dmotivation-time-increase=$MOTIVATION_TIME_INCREASE \
            -Dmotivation-neighbor-increase=$MOTIVATION_NEIGHBOR_INCREASE \
            edu.snu.leader.hierarchy.simple.HierarchyBuildingSimulation 
    

done
