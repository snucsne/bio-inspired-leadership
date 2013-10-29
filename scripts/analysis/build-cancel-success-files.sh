#!/bin/bash

DATA_DIR=$1
USE_NN_FLAG=$2


for CANCELK in $(seq 0 0.25 6)
do

    cat $DATA_DIR/analysis/indcount-*-usenn-$USE_NN_FLAG-cancel-$CANCELK-success-percentage.dat \
            > $DATA_DIR/analysis/success-percentages-usenn-$USE_NN_FLAG-cancel-$CANCELK.dat
done

for INDCOUNT in 0020 0030 0040
do

    rm $DATA_DIR/analysis/success-percentages-indcount-$INDCOUNT-usenn-$USE_NN_FLAG.dat
    for FILE in `ls $DATA_DIR/analysis/indcount-$INDCOUNT-usenn-$USE_NN_FLAG-cancel-*-success-percentage.dat`
    do
        CANCEL=`echo $FILE | sed 's/.*cancel-//' | sed 's/-success-percentage.dat//'`
        SUCCESS=`cat $FILE  | sed 's/00.0  //'`
        echo "$CANCEL   $SUCCESS" >> $DATA_DIR/analysis/success-percentages-indcount-$INDCOUNT-usenn-$USE_NN_FLAG.dat

    done

done

