#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

if [ ! -d "$BASE_DIR/figures" ]; then
    mkdir -p $BASE_DIR/figures
fi

# =========================================================
for DATA_DIR in $BASE_DIR/*indcount*;
do

    # Get the individual count
    IND_COUNT=`echo $DATA_DIR | sed -E 's/.*indcount-([0-9]+).*/\1/'`
    MAX_IND_IDX=`expr $IND_COUNT - 1`
#    echo IndCount=[$IND_COUNT] max=[$MAX_IND_IDX]

    IND_COUNT_FIG_DIR="$BASE_DIR/figures/indcount-$IND_COUNT"
    if [ ! -d "$IND_COUNT_FIG_DIR" ]; then
        mkdir -p $IND_COUNT_FIG_DIR
    fi


    for MAP_DIR in $DATA_DIR/*map*;
    do

        MAP_ID=`echo $MAP_DIR | sed -E 's/.*map-([0-9]+).*/\1/'`

        # Grab any dat file (we only want the neighbor listing
        DAT_FILE=`ls $MAP_DIR/*.dat | head -n 1`;

#        echo $DAT_FILE

        for IDX in $(seq 0 $MAX_IND_IDX);
        do

            PRETTY_IDX=$(printf "%05d" $IDX)
            OUTPUT_FILE_PREFIX="$IND_COUNT_FIG_DIR/map-$MAP_ID-ind-$PRETTY_IDX"

#            echo   $OUTPUT_FILE_PREFIX

            ./create-distance-file.pl \
                    $DAT_FILE \
                    Ind$PRETTY_IDX \
                    $OUTPUT_FILE_PREFIX

        done

    done
done
