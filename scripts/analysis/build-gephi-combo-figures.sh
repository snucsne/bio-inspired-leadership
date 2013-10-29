#!/bin/bash

# =========================================================
# Get the base director
BASE_DIR=$1

if [ ! -d "$BASE_DIR/figures" ]; then
    mkdir -p $BASE_DIR/figures
fi
if [ ! -d "$BASE_DIR/analysis" ]; then
    mkdir -p $BASE_DIR/analysis
fi

# =========================================================
for IND_COUNT in 10 15 20 25 30 35 40 45 50 55 60 65 70 75 80 85 90 95 100;
do

    FORMATTED_IND_COUNT=$(printf "%03d" $IND_COUNT)

    echo Building [$FORMATTED_IND_COUNT]

    # Combine the figures
    ./combine-gephi-figures.pl \
            $BASE_DIR \
            "Individual count [$FORMATTED_IND_COUNT]" \
            $IND_COUNT 2>&1 > /tmp/test.out

done

pdftk $BASE_DIR/figures/spatial*combined-gephi.pdf cat output $BASE_DIR/figures/combined-gephi.pdf
