#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

#echo Base dir [$BASE_DIR]
if [ ! -d "$BASE_DIR/analysis" ]; then
    mkdir -p $BASE_DIR/analysis
fi

# =========================================================
for DATA_DIR in $BASE_DIR/*boldcount*/;
do

    PREFIX=`echo $DATA_DIR | sed -E 's/.*(boldcount-[0-9]+).*/\1/'`

    echo Processing [$PREFIX] ...
    ./calc-bold-personality-percentages.pl $DATA_DIR > $BASE_DIR/analysis/$PREFIX-bold-personality-percentages.dat

done

# =========================================================

