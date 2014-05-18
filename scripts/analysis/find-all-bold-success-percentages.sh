#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

ANALYSIS_DIR="$BASE_DIR/analysis"
if [ ! -d "$ANALYSIS_DIR" ]; then
    mkdir -p $ANALYSIS_DIR
fi

# =========================================================
for DATA_DIR in $1/*boldcount*;
do

    PREFIX=`echo $DATA_DIR | sed -E 's/.*(boldcount-[0-9])/\1/'`

#    echo $PREFIX

    ./find-success-percentages.pl \
            $DATA_DIR \
            $BASE_DIR/analysis/$PREFIX-success-percentages.dat
done

