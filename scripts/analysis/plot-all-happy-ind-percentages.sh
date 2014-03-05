#!/bin/bash

# =========================================================
# Get the processed data directory
PROCESSED_DIR=$1

# Get the figures directory
OUTPUT_DIR=$2

# =========================================================
for INDCOUNT in 10 20 30 40 50
do

    FORMATTED_INDCOUNT=$(printf "%03d" $INDCOUNT)

    for TYPE in split poles
    do

        echo Processing [$TYPE] [$INDCOUNT]


        ./plot-happy-ind-percentage.pl $INDCOUNT \
                10000 \
                $PROCESSED_DIR/$TYPE-no-conflict-indcount-$FORMATTED_INDCOUNT.dat \
                $PROCESSED_DIR/$TYPE-conflict-indcount-$FORMATTED_INDCOUNT.dat \
                $PROCESSED_DIR/$TYPE-single-no-conflict-indcount-$FORMATTED_INDCOUNT.dat \
                $OUTPUT_DIR/$TYPE-indcount-$FORMATTED_INDCOUNT
        

    done

    TYPE="diff-dist"
    echo Processing [$TYPE] [$INDCOUNT] [near]

    ./plot-happy-ind-percentage.pl $INDCOUNT \
            10000 \
            $PROCESSED_DIR/$TYPE-no-conflict-indcount-$FORMATTED_INDCOUNT.dat \
            $PROCESSED_DIR/$TYPE-conflict-indcount-$FORMATTED_INDCOUNT.dat \
            $PROCESSED_DIR/$TYPE-single-near-no-conflict-indcount-$FORMATTED_INDCOUNT.dat \
            $OUTPUT_DIR/$TYPE-near-indcount-$FORMATTED_INDCOUNT

    echo Processing [$TYPE] [$INDCOUNT] [far]

    ./plot-happy-ind-percentage.pl $INDCOUNT \
            10000 \
            $PROCESSED_DIR/$TYPE-no-conflict-indcount-$FORMATTED_INDCOUNT.dat \
            $PROCESSED_DIR/$TYPE-conflict-indcount-$FORMATTED_INDCOUNT.dat \
            $PROCESSED_DIR/$TYPE-single-far-no-conflict-indcount-$FORMATTED_INDCOUNT.dat \
            $OUTPUT_DIR/$TYPE-far-indcount-$FORMATTED_INDCOUNT

done

