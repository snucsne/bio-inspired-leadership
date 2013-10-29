#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

if [ ! -d "$BASE_DIR/figures/breakpoints" ]; then
    mkdir -p $BASE_DIR/figures/breakpoints
fi

# =========================================================
for DATA_DIR in $1/*indcount*;
do

    PREFIX=`echo $DATA_DIR | sed -E 's/.*(indcount-[0-9])/\1/'`

    if [ ! -d "$BASE_DIR/figures/breakpoints/$PREFIX" ]; then
        mkdir -p $BASE_DIR/figures/breakpoints/$PREFIX
    fi
    
    echo Analyzing [$PREFIX]

    ./analyze-personality-breakpoints.pl \
            $DATA_DIR \
            $BASE_DIR/analysis/$PREFIX \
            $BASE_DIR/figures/breakpoints/$PREFIX/$PREFIX
done

# =========================================================

