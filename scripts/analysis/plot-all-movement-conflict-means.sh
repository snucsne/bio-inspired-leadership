#!/bin/bash

# =========================================================
# Get the processed data directory
PROCESSED_DIR=$1

# Get the figures directory
OUTPUT_DIR=$2

# =========================================================
for INDCOUNT in 010 020 030 040 050
do

    for TYPE in split poles
    do

        echo Processing [$TYPE] [$INDCOUNT]


        ./plot-movement-conflict-means.pl \
                $PROCESSED_DIR/$TYPE-conflict-indcount-$INDCOUNT.dat \
                $PROCESSED_DIR/$TYPE-no-conflict-indcount-$INDCOUNT.dat \
                $PROCESSED_DIR/$TYPE-single-no-conflict-indcount-$INDCOUNT.dat \
                $OUTPUT_DIR/$TYPE-indcount-$INDCOUNT
        

    done

    TYPE="diff-dist"
    echo Processing [$TYPE] [$INDCOUNT] [near]

    ./plot-movement-conflict-means.pl \
            $PROCESSED_DIR/$TYPE-conflict-indcount-$INDCOUNT.dat \
            $PROCESSED_DIR/$TYPE-no-conflict-indcount-$INDCOUNT.dat \
            $PROCESSED_DIR/$TYPE-single-near-no-conflict-indcount-$INDCOUNT.dat \
            $OUTPUT_DIR/$TYPE-near-indcount-$INDCOUNT

    echo Processing [$TYPE] [$INDCOUNT] [far]

    ./plot-movement-conflict-means.pl \
            $PROCESSED_DIR/$TYPE-conflict-indcount-$INDCOUNT.dat \
            $PROCESSED_DIR/$TYPE-no-conflict-indcount-$INDCOUNT.dat \
            $PROCESSED_DIR/$TYPE-single-far-no-conflict-indcount-$INDCOUNT.dat \
            $OUTPUT_DIR/$TYPE-far-indcount-$INDCOUNT

done


