#!/bin/bash

DATA_DIR=$1

TYPE=follow

for IND_COUNT in $(seq 20 10 40)
do
    FORMATTED_IND_COUNT=$(printf "%04d" $IND_COUNT)
    OUTPUT_DATA_FILE=$DATA_DIR/analysis/ind-success-percentage-indcount-$FORMATTED_IND_COUNT.dat
    echo "# CancelK  Mean      Min       Max       SD" > $OUTPUT_DATA_FILE

    for FOLLOW_K in 0.00 -0.25 -0.50 -0.70 -0.80 -0.90 -0.91 -0.92 -0.93 -0.94 -0.95 -0.96 -0.97 -0.98 -0.99
    do

        FORMATTED_FOLLOW_K=$(printf "%+4.2f" $FOLLOW_K)
        MEAN=`grep summary.indsuccesspercentage.mean \
                $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-true-$TYPE-$FOLLOW_K.stats \
                | awk '{print $3;'}`;
        MIN=`grep summary.indsuccesspercentage.min \
                $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-true-$TYPE-$FOLLOW_K.stats \
                | awk '{print $3;'}`;
        MAX=`grep summary.indsuccesspercentage.max \
                $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-true-$TYPE-$FOLLOW_K.stats \
                | awk '{print $3;'}`;
        SD=`grep summary.indsuccesspercentage.sd \
                $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-true-$TYPE-$FOLLOW_K.stats \
                | awk '{print $3;'}`;

        echo "$FORMATTED_FOLLOW_K      $(printf "%7.5f" $MEAN)   $(printf "%7.5f" $MIN)   $(printf "%7.5f" $MAX)   $(printf "%7.5f" $SD)" >> $OUTPUT_DATA_FILE

    done
done
