#!/bin/bash

# =========================================================
# Get the base director
BASE_DIR=$1

# =========================================================
for IND_COUNT in $(seq 10 5 100)
do

    FORMATTED_IND_COUNT=$(printf "%03d" $IND_COUNT)

echo Starting [$FORMATTED_IND_COUNT]...

    # Combine the figures
    ./combine-figures-and-analysis-v2.pl \
#    ./combine-failed-followers-figures.pl \
            $BASE_DIR \
            "Individual count [$FORMATTED_IND_COUNT]" \
            $IND_COUNT 2>&1 > /tmp/test.out

done


