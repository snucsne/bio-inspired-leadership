#!/bin/bash

DATA_DIR=$1
USE_NN_FLAG=$2



for IND_COUNT in $(seq 20 10 40)
do
    FORMATTED_IND_COUNT=$(printf "%04d" $IND_COUNT)
    OUTPUT_DATA_FILE=$DATA_DIR/analysis/cancel-mimics-vs-success-correlation-spearman-indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_FLAG.dat
    echo "# Cancel K vs Success" > $OUTPUT_DATA_FILE

#    for CANCEL_K in $(seq 0.00 0.25 7.00)
    for CANCEL_K in $(seq 0 5 40)
    do

#        FORMATTED_CANCEL_K=$(printf "%+4.2f" $CANCEL_K)
        FORMATTED_CANCEL_K=$(printf "%02d" $CANCEL_K)
        CORRELATION=`grep -A 22 "Correlation: mimics vs. success" \
                $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_FLAG-cancel-$FORMATTED_CANCEL_K.stats \
                | tail -n 1`
        FORMATTED_CORRELATION=$(printf "%+7.5f" $CORRELATION)

        echo "$FORMATTED_CANCEL_K   $FORMATTED_CORRELATION" >> $OUTPUT_DATA_FILE

    done
done
