#!/bin/bash

# =========================================================
# Get the global analysis directory
GLOBAL_DIR=$1

# Get the local analysis directory
LOCAL_DIR=$2

# Get the output prefix
OUTPUT_PREFIX=$3

# =========================================================
for INDCOUNT in 10 15 20 25 30 35 40 45 50 55 60 65 70 75 80 85 90 95 100;
do

    FORMATTEDINDCOUNT=$(printf "%04d" $INDCOUNT)

    ./analyze-success-percentages.pl \
            $OUTPUT_PREFIX-indcount-$FORMATTEDINDCOUNT-success-percentages.stats \
            $GLOBAL_DIR/*$INDCOUNT-*-success-percentages.dat \
            $LOCAL_DIR/*$INDCOUNT-*-true-success-percentages.dat \
            $LOCAL_DIR/*$INDCOUNT-*-false-success-percentages.dat

done
