#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

# =========================================================
for TYPE in exponential;
do
    for MULT in $BASE_DIR/$TYPE/decay-mult-*100*;
    do
        for PERSONALITY in $MULT/personality-0.2;
        do

            echo $PERSONALITY
            ./analyze-all-personality-spatial-hidden-vars-results.sh $PERSONALITY/
            cat $PERSONALITY/analysis/*success-percentage.dat > $PERSONALITY/analysis/success-percentages.dat
        done
    done
done
