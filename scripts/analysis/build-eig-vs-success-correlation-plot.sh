#!/bin/bash

DATA_DIR=$1
USE_NN_FLAG=$2

# Files
OUTPUT_DATA_FILE=$DATA_DIR/analysis/eig-vs-success-correlation.dat

echo "# Eigenvector centrality vs success" > $OUTPUT_DATA_FILE

for IND_COUNT in $(seq 15 5 100)
do

    FORMATTED_IND_COUNT=$(printf "%04d" $IND_COUNT)

    CORRELATION=`grep -A 1 "Correlation: eigenvector centrality vs. success" \
            $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_FLAG.stats \
            | sed '/\#/d' | sed -E 's/\[1\]\s+//'`

    echo $FORMATTED_IND_COUNT   $CORRELATION >> $OUTPUT_DATA_FILE

done


