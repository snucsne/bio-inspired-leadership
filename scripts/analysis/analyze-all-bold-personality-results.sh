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
for DATA_DIR in $1/*boldcount*;
do
    echo $DATA_DIR

    PREFIX=`echo $DATA_DIR | sed -E 's/.*(boldcount-[0-9])/\1/'`

    if [ ! -d "$BASE_DIR/figures/$PREFIX/" ]; then
        mkdir -p $BASE_DIR/figures/$PREFIX/
    fi

    ./analyze-personality-results.pl \
            $DATA_DIR \
            /tmp/tmp.r.out \
            $BASE_DIR/figures/$PREFIX/$PREFIX > /tmp/analyze.out


done

# =========================================================

