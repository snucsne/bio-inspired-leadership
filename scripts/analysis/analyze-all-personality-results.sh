#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

if [ ! -d "$BASE_DIR/figures" ]; then
    mkdir -p $BASE_DIR/figures
fi
if [ ! -d "$BASE_DIR/analysis" ]; then
    mkdir -p $BASE_DIR/analysis
fi

# =========================================================
for DATA_DIR in $1/*indcount*;
do

    PREFIX=`echo $DATA_DIR | sed -E 's/.*(indcount-[0-9])/\1/'`

    ./analyze-personality-results.pl \
            $DATA_DIR \
            /tmp/tmp.r.out \
            $BASE_DIR/figures/$PREFIX > /tmp/analyze.out

#    exit

done

# =========================================================

