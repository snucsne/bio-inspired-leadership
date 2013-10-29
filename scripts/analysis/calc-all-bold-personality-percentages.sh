#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

#echo Base dir [$BASE_DIR]

# =========================================================
for DATA_DIR in $BASE_DIR/*indcount*/;
do

    PREFIX=`echo $DATA_DIR | sed -E 's/.*(indcount-[0-9]+).*/\1/'`

    echo Processing [$PREFIX] ...
    ./calc-bold-personality-percentages.pl $DATA_DIR > $BASE_DIR/analysis/$PREFIX-bold-personality-percentages.dat

done

# =========================================================

