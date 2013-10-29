#!/bin/bash

DATA_DIR=$1
USE_NN_FLAG=$2



for IND_COUNT in $(seq 20 10 40)
do
    FORMATTED_IND_COUNT=$(printf "%04d" $IND_COUNT)
    OUTPUT_DATA_FILE=$DATA_DIR/analysis/follow-mimics-vs-success-correlation-spearman-indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_FLAG.dat
    echo "# Follow K vs Success" > $OUTPUT_DATA_FILE

    for FOLLOW_K in 0.00 -0.25 -0.50 -0.70 -0.80 -0.90 -0.91 -0.92 -0.93 -0.94 -0.95 -0.96 -0.97 -0.98 -0.99
    do

        FORMATTED_FOLLOW_K=$(printf "%+4.2f" $FOLLOW_K)
        CORRELATION=`grep -A 22 "Correlation: mimics vs. success" \
                $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_FLAG-follow-$FOLLOW_K.stats \
                | tail -n 1`
        FORMATTED_CORRELATION=$(printf "%+7.5f" $CORRELATION)

        echo "$FORMATTED_FOLLOW_K   $FORMATTED_CORRELATION" >> $OUTPUT_DATA_FILE

    done
done
