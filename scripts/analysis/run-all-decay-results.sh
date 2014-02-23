#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

# =========================================================
for TYPE in constant exponential linear momentum;
do
    for MULT in $BASE_DIR/$TYPE/decay-mult-*;
    do
        for PERSONALITY in $MULT/personality-0.*;
        do

            echo $PERSONALITY
            ./analyze-all-personality-spatial-hidden-vars-results.sh $PERSONALITY/
            cat $PERSONALITY/analysis/*success-percentage.dat > $PERSONALITY/analysis/success-percentages.dat
        done
    done
done
