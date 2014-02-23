#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1


# =========================================================
for TYPE in constant exponential linear momentum;
do
    for MULT in $BASE_DIR/$TYPE/decay-mult*;
    do

        if [ ! -d "$MULT/figures" ]; then
            mkdir -p $MULT/figures
        fi
        if [ ! -d "$MULT/analysis" ]; then
            mkdir -p $MULT/analysis
        fi

echo $MULT

        ./compare-success-percentages.pl \
                $MULT/ \
                personality \
                Personality \
                $MULT/analysis/success-comparison \
                $MULT/figures/success-comparison
#read -p "Press [Enter] to continue..."

    done
done

