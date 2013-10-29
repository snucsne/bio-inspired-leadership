#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

# =========================================================
for DATA_DIR in $1/spatial-hidden-var-indcount*;
do

    PREFIX=`echo $DATA_DIR | sed -E 's/.*(indcount-[0-9]+-usenn-(false|true))/\1/'`

#    echo $PREFIX

    ./find-success-percentages.pl \
            $DATA_DIR \
            $BASE_DIR/analysis/$PREFIX-success-percentages.dat
done

