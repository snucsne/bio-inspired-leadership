#!/bin/bash

DATA_DIR=$1
USE_NN_FLAG=$2

# Files
OUTPUT_DATA_FILE=$DATA_DIR/analysis/eig-vs-success-correlation-spearman.dat

echo "# Eigenvector centrality vs success" > $OUTPUT_DATA_FILE

for IND_COUNT in $(seq 15 5 100)
do

    FORMATTED_IND_COUNT=$(printf "%04d" $IND_COUNT)

    CORRELATION=`grep -A 22 "Correlation: eigenvector centrality vs. success" \
            $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_FLAG.stats \
            | tail -n 1`

    echo $FORMATTED_IND_COUNT   $CORRELATION >> $OUTPUT_DATA_FILE

done


