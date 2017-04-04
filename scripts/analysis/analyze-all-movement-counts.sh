#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

# =========================================================
# Ensure the directories exist
if [ ! -d "$BASE_DIR/figures" ]; then
    mkdir -p $BASE_DIR/figures
fi
if [ ! -d "$BASE_DIR/analysis" ]; then
    mkdir -p $BASE_DIR/analysis
fi

# =========================================================
for DATA_DIR in $BASE_DIR/indcount*;
do
    PREFIX=`echo $DATA_DIR | sed -E 's/.*(indcount-[0-9])/\1/'`

    echo Processing [$PREFIX]

    ./analyze-movement-counts.pl \
            $DATA_DIR/ \
            $BASE_DIR/analysis/$PREFIX- \
            $BASE_DIR/figures/$PREFIX- > /tmp/analyze.out

done

