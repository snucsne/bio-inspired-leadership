#!/bin/bash

DATA_DIR=$1
USE_NN_FLAG=$2

# Files
OUTPUT_DATA_FILE=$DATA_DIR/analysis/mimics-vs-success-correlation-spearman-p-values.dat

echo "# Mimicing neighbors vs success" > $OUTPUT_DATA_FILE

for IND_COUNT in $(seq 15 5 100)
do

    FORMATTED_IND_COUNT=$(printf "%03d" $IND_COUNT)

    CORRELATION=`grep -A 18 "Correlation: mimics vs. success" \
            $DATA_DIR/analysis/indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_FLAG.stats \
            | tail -n 1 | sed 's/.*p/  p/'`

    echo "$FORMATTED_IND_COUNT     $CORRELATION" >> $OUTPUT_DATA_FILE

done
