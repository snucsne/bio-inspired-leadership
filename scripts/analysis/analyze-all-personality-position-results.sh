#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

if [ ! -d "$BASE_DIR/analysis/" ]; then
    mkdir -p $BASE_DIR/analysis/
fi

# =========================================================
for DATA_DIR in $1/*indcount*;
do

    PREFIX=`echo $DATA_DIR | sed -E 's/.*(indcount-[0-9])/\1/'`
    INDCOUNT=`echo $PREFIX | sed -E 's/.*([1-9][0-9]+).*/\1/'`
    SIMSWITCHINDEX=$((2000*$INDCOUNT))

    echo Analyzing [$PREFIX]  IndCount=[$INDCOUNT]  SimSwitchIndex=[$SIMSWITCHINDEX]

    ./analyze-personality-position-results.pl \
            $DATA_DIR \
            $BASE_DIR/analysis/$PREFIX \
            $SIMSWITCHINDEX

done

# =========================================================

