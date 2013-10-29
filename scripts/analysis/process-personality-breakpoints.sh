#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

# Get the data type
DATA_TYPE=$2

# =========================================================
for DATA_DIR in $1/*$2*;
do

    ANALYSIS_DIR="$DATA_DIR/analysis/"

    for IND_COUNT in `ls $ANALYSIS_DIR/*-breakpoint-analysis-01.dat  | sed -E 's/.*indcount-([0-9]+).*/\1/'`;
    do

        ./analyze-breakpoints.pl \
                $ANALYSIS_DIR \
                $IND_COUNT \
                $ANALYSIS_DIR/indcount-$IND_COUNT-breakpoint-results.dat
    done
done
