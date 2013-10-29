#!/bin/bash

DATA_DIR=$1

TYPE=cancel

for IND_COUNT in $(seq 20 10 40)
do
    FORMATTED_IND_COUNT=$(printf "%04d" $IND_COUNT)
    OUTPUT_DATA_FILE=$DATA_DIR/analysis/ind-success-percentage-indcount-$FORMATTED_IND_COUNT.dat
    echo "# CancelK  Mean      Min       Max       SD" > $OUTPUT_DATA_FILE

#    for CANCEL_K in $(seq 0.00 0.25 7.00)
    for CANCEL_K in $(seq 0 5 40)
    do

#        FORMATTED_CANCEL_K=$(printf "%+4.2f" $CANCEL_K)
        FORMATTED_CANCEL_K=$(printf "%02d" $CANCEL_K)
        MEAN=`grep summary.indsuccesspercentage.mean \
                $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-true-$TYPE-$FORMATTED_CANCEL_K.stats \
                | awk '{print $3;'}`;
        MIN=`grep summary.indsuccesspercentage.min \
                $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-true-$TYPE-$FORMATTED_CANCEL_K.stats \
                | awk '{print $3;'}`;
        MAX=`grep summary.indsuccesspercentage.max \
                $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-true-$TYPE-$FORMATTED_CANCEL_K.stats \
                | awk '{print $3;'}`;
        SD=`grep summary.indsuccesspercentage.sd \
                $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-true-$TYPE-$FORMATTED_CANCEL_K.stats \
                | awk '{print $3;'}`;

        echo "$FORMATTED_CANCEL_K         $(printf "%7.5f" $MEAN)   $(printf "%7.5f" $MIN)   $(printf "%7.5f" $MAX)   $(printf "%7.5f" $SD)" >> $OUTPUT_DATA_FILE

    done
done
